package com.bookmap.demo.simple.provider;

import velox.api.layer1.data.TradeInfo;
import velox.api.layer1.layers.strategies.interfaces.CustomGeneratedEventAliased;
import velox.api.layer1.messages.indicators.StrategyUpdateGenerator;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Generator implements StrategyUpdateGenerator {

    private static final String GENERATOR_ID_PATTERN = "%s";
    private static final long CALCULATION_PERIOD = TimeUnit.MINUTES.toNanos(10);
    private static final long MIN_INTERVAL = TimeUnit.SECONDS.toNanos(1);

    private final String currentAlias;
    private final String generatorId;
    private final Deque<Trade> trades;

    private long currentTime;
    private long timeOfLastEventSent;
    private double sumPrices;
    private Consumer<CustomGeneratedEventAliased> bookmapConsumer;

    public Generator(String alias) {
        this.currentAlias = alias;
        this.trades = new LinkedList<>();

        this.generatorId = String.format(GENERATOR_ID_PATTERN, alias);
    }

    public String getGeneratorId() {
        return generatorId;
    }

    @Override
    public void setTime(long time) {
        if(time < currentTime){
            return;
        }

        currentTime = time;

        //If we send an event every time setTime is called (usually it is called at least every 50ms)
        //   we will generate too many unnecessary events that will degrade performance.
        //Warning: You need to send events to BM only with the time you got in setTime,
        //   if you send an event with time less than this time, you will get a crash.
        if(timeOfLastEventSent + MIN_INTERVAL < currentTime) {
            SimpleDemoProviderEvent event = new SimpleDemoProviderEvent(currentTime, getAverage());
            timeOfLastEventSent = currentTime;
            sendEventToBookmap(event);
        }
    }

    @Override
    public void onTrade(String alias, double price, int size, TradeInfo tradeInfo) {
        if(!currentAlias.equals(alias)){
            return;
        }

        Trade newTrade = new Trade(currentTime, price);
        trades.addLast(newTrade);
        sumPrices += price;
        removeOldTrades(currentTime);
    }

    private void removeOldTrades(long currentTime) {
        while (!trades.isEmpty() && (currentTime - trades.peekFirst().time > CALCULATION_PERIOD)) {
            Trade oldTrade = trades.removeFirst();
            sumPrices -= oldTrade.price;
        }
    }

    private double getAverage() {
        if (trades.isEmpty()) {
            return 0.0;
        }
        return sumPrices / trades.size();
    }

    private void sendEventToBookmap(SimpleDemoProviderEvent event){
        bookmapConsumer.accept(new CustomGeneratedEventAliased(event, currentAlias));
    }

    @Override
    public void onUserMessage(Object data) {
        //Empty
    }

    @Override
    public void setGeneratedEventsConsumer(Consumer<CustomGeneratedEventAliased> consumer) {
        this.bookmapConsumer = consumer;
    }

    @Override
    public Consumer<CustomGeneratedEventAliased> getGeneratedEventsConsumer() {
        return bookmapConsumer;
    }

    private static class Trade {
        long time;
        double price;

        Trade(long time, double price) {
            this.time = time;
            this.price = price;
        }
    }
}
