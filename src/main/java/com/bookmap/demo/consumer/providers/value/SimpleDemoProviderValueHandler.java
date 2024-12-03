package com.bookmap.demo.consumer.providers.value;

import com.bookmap.addons.broadcasting.api.view.Event;
import com.bookmap.addons.broadcasting.api.view.EventFilter;
import com.bookmap.addons.broadcasting.implementations.base.CastUtilities;
import com.bookmap.addons.broadcasting.implementations.base.FailedToCastObject;
import com.bookmap.demo.simple.provider.SimpleDemoProviderEvent;
import velox.api.layer1.data.InstrumentInfo;

import java.util.List;

public class SimpleDemoProviderValueHandler implements ProviderValueHandler{
    private static final String DISPLAY_EVENT_PATTERN = "Time=%s, Moving Average=%.2f;";


    @Override
    public String[] getTextualVisualizationOfEvent(Event event, InstrumentInfo instrumentInfo) {
        SimpleDemoProviderEvent providerEvent = (SimpleDemoProviderEvent) event;
        String text = String.format(DISPLAY_EVENT_PATTERN, ProviderValueHandler.convertTime(providerEvent.getTime()),
                providerEvent.getMovingAverage());
        return new String[]{text};
    }

    @Override
    public String getGeneratorSettingsInfo(Object settings) {
        return "This provider does not broadcast any settings.";
    }

    @Override
    public Event castEventInOurClassLoader(Object o) {
        SimpleDemoProviderEvent event = null;
        try {
            event = CastUtilities.castObject(o, SimpleDemoProviderEvent.class);
        } catch (FailedToCastObject e) {
            throw new RuntimeException(e);
        }
        return event;
    }

    @Override
    public List<Event> castEventsInOurClassLoader(List<Object> o) {
        return null;
    }

    @Override
    public EventFilter<Event> castFilter(Object o) {
        return null;
    }

    @Override
    public Object castSettings(Object o) {
        return null;
    }
}
