package com.bookmap.demo.consumer.providers.value;

import com.bookmap.addons.broadcasting.api.view.Event;
import com.bookmap.addons.broadcasting.api.view.EventFilter;
import com.bookmap.addons.broadcasting.implementations.base.CastUtilities;
import com.bookmap.addons.broadcasting.implementations.base.FailedToCastObject;
import com.bookmap.addons.strengthlevel.broadcasting.module.EventInterface;
import com.bookmap.addons.strengthlevel.broadcasting.module.ProviderSettings;
import com.bookmap.addons.strengthlevel.broadcasting.module.implementation.BrIcebergEvent;
import com.bookmap.addons.strengthlevel.broadcasting.module.implementation.Filter;
import com.bookmap.addons.strengthlevel.broadcasting.module.implementation.ProviderSettingsProxy;
import velox.api.layer1.data.InstrumentInfo;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class StrengthLevelValueHandler implements ProviderValueHandler {

    @Override
    public String[] getTextualVisualizationOfEvent(Event event, InstrumentInfo instrumentInfo) {
        EventInterface eventInterface = (EventInterface) event;

        String firstRow = String.format("Price = %s, Size=%s, IsBid=%s, Time=%s,",
                eventInterface.getLevel(),
                eventInterface.getSize(),
                eventInterface.isBid(),
                ProviderValueHandler.convertTime(event.getTime())
        );
        return new String[] {firstRow};
    }

    @Override
    public String getGeneratorSettingsInfo(Object providerSettings) {
        ProviderSettings settings = (ProviderSettings) providerSettings;
        return "Settings: delay - " + TimeUnit.NANOSECONDS.toMillis(settings.getMaxPostTradeIncreaseDelay()) +
                ", minSize - " + settings.getMinPostTradeIncreaseSize();
    }

    @Override
    public Event castEventInOurClassLoader(Object o) {
        BrIcebergEvent lineEvent;
        try {
            lineEvent = CastUtilities.castObject(o, BrIcebergEvent.class);
        } catch (FailedToCastObject e) {
            throw new RuntimeException(e);
        }
        return lineEvent;
    }

    @Override
    public List<Event> castEventsInOurClassLoader(List<Object> o) {
        return new LinkedList<>(CastUtilities.castObjects(o, BrIcebergEvent.class));
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
