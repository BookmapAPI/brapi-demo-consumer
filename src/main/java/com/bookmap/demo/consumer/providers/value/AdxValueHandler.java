package com.bookmap.demo.consumer.providers.value;

import com.bookmap.addons.broadcasting.api.view.BrDataStructureInterface;
import com.bookmap.addons.broadcasting.api.view.Event;
import com.bookmap.addons.broadcasting.api.view.EventFilter;
import com.bookmap.addons.broadcasting.implementations.base.CastUtilities;
import com.bookmap.addons.broadcasting.implementations.base.FailedToCastObject;
import com.bookmap.broadcasting.module.BroadcastingBarSettings;
import com.bookmap.broadcasting.module.BroadcastingEventAliased;
import velox.api.layer1.data.InstrumentInfo;
import velox.indicators.sionchart.broadcasting.EventInterface;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AdxValueHandler implements ProviderValueHandler{

    BroadcastingBarSettings broadcastingBarSettings;

    @Override
    public String[] getTextualVisualizationOfEvent(Event event, InstrumentInfo instrumentInfo) {
        BroadcastingEventAliased eventInterface =
                (BroadcastingEventAliased) event;
               String s = String.format("ADX value Price=%f,Time=%s",
                       eventInterface.value, ProviderValueHandler.convertTime(eventInterface.time));
        return new String[] {s};
    }

    @Override
    public String getGeneratorSettingsInfo(Object providerSettings) {
        long nanos = ((BroadcastingBarSettings)providerSettings).barDurationNanos;
        return "ADX barDuration in min: " + TimeUnit.NANOSECONDS.toMinutes(nanos);
    }

    @Override
    public Event castEventInOurClassLoader(Object o) {
        Event event = null;
        try {
            if(o.getClass().getName().equals(BroadcastingEventAliased.class.getName())) {
                event = CastUtilities.castObject(o, BroadcastingEventAliased.class);
            }
        } catch (FailedToCastObject e) {
            throw new RuntimeException(e);
        }
        return event;
    }

    @Override
    public List<Event> castEventsInOurClassLoader(List<Object> o) {
        if(o != null && !o.isEmpty()) {
            List<Event> result = new LinkedList<>();
            for (Object object : o) {
                Event event = castEventInOurClassLoader(object);
                if(event != null){
                    result.add(event);
                }
            }
            return result;
        }
        return new LinkedList<>();
    }

    @Override
    public EventFilter<EventInterface> castFilter(Object o) {
        throw new RuntimeException("Filters are not supported by ADX");
    }

    @Override
    public Object castSettings(Object o) {
        try {
            return broadcastingBarSettings = CastUtilities.castObject(o, BroadcastingBarSettings.class);
        } catch (FailedToCastObject e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Object> requestHistoricalData(BrDataStructureInterface dataStructureInterface, String generatorName,
                                              long startTime, long endTime, String alias) {
        List<Object> result = new LinkedList<>();
        long intervalsWidth = broadcastingBarSettings.barDurationNanos;

        if (endTime - startTime > ProviderValueHandler.timeLimitForRequestingHistoricalData){
            startTime = endTime - ProviderValueHandler.timeLimitForRequestingHistoricalData;
        }

        int intervalsNumber = (int) Math.ceil((double) (endTime - startTime) / intervalsWidth);
        List<BrDataStructureInterface.TreeResponseInterval> treeResponseIntervals =
                dataStructureInterface.get(Object.class, generatorName, startTime, intervalsWidth, intervalsNumber,
                        alias, new Class<?>[]{Object.class});

        for (BrDataStructureInterface.TreeResponseInterval treeResponseInterval : treeResponseIntervals) {
            result.addAll(treeResponseInterval.events.values());
        }
        return result;
    }
}
