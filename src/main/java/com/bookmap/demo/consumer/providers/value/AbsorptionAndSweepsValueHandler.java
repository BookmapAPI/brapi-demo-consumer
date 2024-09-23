package com.bookmap.demo.consumer.providers.value;

import com.bookmap.addons.broadcasting.api.view.Event;
import com.bookmap.addons.broadcasting.api.view.EventFilter;
import com.bookmap.addons.broadcasting.implementations.base.CastUtilities;
import com.bookmap.addons.broadcasting.implementations.base.FailedToCastObject;
import velox.api.layer1.data.InstrumentInfo;
import velox.indicators.absorption.broadcasting.module.EventInterface;
import velox.indicators.absorption.broadcasting.module.ProviderSettings;
import velox.indicators.absorption.broadcasting.module.implementations.Filter;
import velox.indicators.absorption.broadcasting.module.implementations.ProviderSettingsProxy;
import velox.indicators.absorption.broadcasting.module.implementations.TradeEvent;

import java.util.LinkedList;
import java.util.List;

public class AbsorptionAndSweepsValueHandler implements ProviderValueHandler{
    @Override
    public String[] getTextualVisualizationOfEvent(Event event, InstrumentInfo instrumentInfo) {
        velox.indicators.absorption.broadcasting.module.EventInterface eventInterface =
                (EventInterface) event;

        double scale = Math.pow(10, 3);

        double price = eventInterface.getPrice() * instrumentInfo.pips;
        price = Math.ceil(price * scale) / scale;

        double size = eventInterface.getValue() / instrumentInfo.sizeMultiplier;
        size = Math.ceil(size * scale) / scale;

        double chainSize = eventInterface.getMaxChainSize() / instrumentInfo.sizeMultiplier;
        chainSize = Math.ceil(chainSize * scale) / scale;

        String firstRow = String.format("Price=%s, Size=%s, isBid=%s, Time=%s,",
                price, size, eventInterface.isBid(),
                ProviderValueHandler.convertTime(event.getTime()),
                chainSize);
        String secondRow = String.format(" ChainSize=%s;", chainSize);

        return new String[] {firstRow,secondRow};
    }

    @Override
    public String getGeneratorSettingsInfo(Object providerSettings) {
        ProviderSettings settings = (ProviderSettings) providerSettings;
        double scale = Math.pow(10, 3);
        double timeLimitMillis = settings.getTimeLimitMillis();
        timeLimitMillis = Math.ceil(timeLimitMillis * scale) / scale;
        double sizeLimit = settings.getSizeLimit();
        sizeLimit = Math.ceil(sizeLimit * scale) / scale;

        return "Generator information: Time limit - " + timeLimitMillis + ", size limit - "
                + sizeLimit;
    }

    @Override
    public Event castEventInOurClassLoader(Object o) {
        TradeEvent tradeEvent = null;
        try {
            tradeEvent = CastUtilities.castObject(o, TradeEvent.class);
        } catch (FailedToCastObject e) {
            throw new RuntimeException(e);
        }
        return tradeEvent;
    }

    @Override
    public List<Event> castEventsInOurClassLoader(List<Object> o) {
        return new LinkedList<>(CastUtilities.castObjects(o, TradeEvent.class));
    }

    @Override
    public EventFilter<EventInterface> castFilter(Object o) {
        try {
            return CastUtilities.castObject(o, Filter.class);
        } catch (FailedToCastObject e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object castSettings(Object o) {
        try {
            return CastUtilities.castObject(o, ProviderSettingsProxy.class);
        } catch (FailedToCastObject e) {
            throw new RuntimeException(e);
        }
    }
}
