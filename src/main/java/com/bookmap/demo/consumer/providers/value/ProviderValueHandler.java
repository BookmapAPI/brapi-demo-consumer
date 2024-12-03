package com.bookmap.demo.consumer.providers.value;

import com.bookmap.addons.broadcasting.api.view.BrDataStructureInterface;
import com.bookmap.addons.broadcasting.api.view.Event;
import com.bookmap.addons.broadcasting.api.view.EventFilter;
import com.bookmap.demo.consumer.DemoConsumer;
import velox.api.layer1.data.InstrumentInfo;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;

public interface ProviderValueHandler {

    //The time limit for requesting data is 3 hours.
    long timeLimitForRequestingHistoricalData = 3L * 60 * 60 * 1_000_000_000;

    /**
     * Converts time from nanoseconds to UTC date.
     */
    static String convertTime(long time){
        Instant instant = Instant.ofEpochSecond(0, time);
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
        LocalTime localTime = dateTime.toLocalTime();
        String result = localTime.toString();
        return result.split("\\.")[0];
    }

    static ProviderValueHandler getInstance(Class<?> handler) {
        if (handler.equals(AbsorptionAndSweepsValueHandler.class)) {
            return new AbsorptionAndSweepsValueHandler();
        } else if (handler.equals(SitValueHandler.class)) {
            return new SitValueHandler();
        } else if(handler.equals(MarketPulseValueHandler.class)){
            return new MarketPulseValueHandler();
        } else if(handler.equals(BreakevenPointValueHandler.class)) {
            return new BreakevenPointValueHandler();
        } else if(handler.equals(IntrinsicValueHandler.class)) {
            return new IntrinsicValueHandler();
        } else if(handler.equals(TaIndicatorAdxValueHandler.class)) {
            return new TaIndicatorAdxValueHandler();
        } else if(handler.equals(TaIndicatorMacdValueHandler.class)) {
            return new TaIndicatorMacdValueHandler();
        } else if(handler.equals(TaIndicatorObvValueHandler.class)) {
            return new TaIndicatorObvValueHandler();
        } else if(handler.equals(TaIndicatorRsiValueHandler.class)) {
            return new TaIndicatorRsiValueHandler();
        } else if(handler.equals(TaIndicatorWilliamsRValueHandler.class)) {
            return new TaIndicatorWilliamsRValueHandler();
        } else if (handler.equals(StrengthLevelValueHandler.class)) {
            return new StrengthLevelValueHandler();
        } else if (handler.equals(AvwapValueHandler.class)) {
            return new AvwapValueHandler();
        } else if (handler.equals(SimpleDemoProviderValueHandler.class)) {
            return new SimpleDemoProviderValueHandler();
        } else{
            throw new IllegalArgumentException("Unknown handler class: " + handler);
        }
    }

    String[] getTextualVisualizationOfEvent(Event event, InstrumentInfo instrumentInfo);

    String getGeneratorSettingsInfo(Object settings);

    Event castEventInOurClassLoader(Object o);

    List<Event> castEventsInOurClassLoader(List<Object> o);

    EventFilter<Event> castFilter(Object o);

    Object castSettings(Object o);

    default List<Object> requestHistoricalData(BrDataStructureInterface dataStructureInterface, String generatorName,
                                               long startTime,long endTime, String alias){
        if((endTime - startTime) > timeLimitForRequestingHistoricalData){
            startTime = endTime - timeLimitForRequestingHistoricalData;
        }
        return dataStructureInterface.get(DemoConsumer.class, generatorName,
                startTime, endTime, alias);
    }

    default List<BrDataStructureInterface.TreeResponseInterval> requestAggregatedHistoricalData(
            BrDataStructureInterface dataStructureInterface,
            String generatorName, long startTime, long endTime,
            String alias) {
        //This method is only used by providers with aggregation
        return null;
    }

    default List<Object> getListOfEventsFrom(List<BrDataStructureInterface.TreeResponseInterval> intervals) {
        //This method is only used by providers with aggregation
        return null;
    }
}
