package com.bookmap.demo.consumer.providers.value;

import com.bookmap.addons.avwap.broadcasting.module.EventInterface;
import com.bookmap.addons.avwap.broadcasting.module.ProviderSettings;
import com.bookmap.addons.avwap.broadcasting.module.implementation.BrVwapEvent;
import com.bookmap.addons.avwap.broadcasting.module.implementation.ProviderSettingsProxy;
import com.bookmap.addons.broadcasting.api.view.BrDataStructureInterface;
import com.bookmap.addons.broadcasting.api.view.Event;
import com.bookmap.addons.broadcasting.api.view.EventFilter;
import com.bookmap.addons.broadcasting.implementations.base.CastUtilities;
import com.bookmap.addons.broadcasting.implementations.base.FailedToCastObject;
import com.bookmap.demo.consumer.DemoConsumer;
import velox.api.layer1.data.InstrumentInfo;
import velox.indicators.absorption.broadcasting.module.implementations.Filter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AvwapValueHandler implements ProviderValueHandler {
    private static final int INTERVAL_WIDTH = (int) TimeUnit.SECONDS.toNanos(1);

    @Override
    public String[] getTextualVisualizationOfEvent(Event event, InstrumentInfo instrumentInfo) {
        EventInterface eventInterface = (EventInterface) event;

        String firstRow = String.format("VWAP = %s, Std. Dev.=%s, Time=%s,",
                String.format("%.2f", eventInterface.getVwap() * instrumentInfo.pips),
                String.format("%.2f", eventInterface.getStandardDeviation() * instrumentInfo.pips),
                ProviderValueHandler.convertTime(event.getTime())
        );
        return new String[] {firstRow};
    }

    @Override
    public String getGeneratorSettingsInfo(Object providerSettings) {
        ProviderSettings settings = (ProviderSettings) providerSettings;
        return "Settings: name - " + settings.getUsername();
    }

    @Override
    public Event castEventInOurClassLoader(Object o) {
        BrVwapEvent lineEvent;
        try {
            lineEvent = CastUtilities.castObject(o, BrVwapEvent.class);
        } catch (FailedToCastObject e) {
            throw new RuntimeException(e);
        }
        return lineEvent;
    }

    @Override
    public List<Event> castEventsInOurClassLoader(List<Object> o) {
        return new LinkedList<>(CastUtilities.castObjects(o, BrVwapEvent.class));
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

    @Override
    public List<BrDataStructureInterface.TreeResponseInterval> requestAggregatedHistoricalData(
            BrDataStructureInterface dataStructureInterface, String generatorName,
            long startTime, long endTime, String alias) {
        if((endTime - startTime) > timeLimitForRequestingHistoricalData){
            startTime = endTime - timeLimitForRequestingHistoricalData;
        }
        return dataStructureInterface.get(
                DemoConsumer.class, generatorName, startTime,
                INTERVAL_WIDTH, (int) ((endTime - startTime) / INTERVAL_WIDTH), alias,
                new Class<?>[]{BrVwapEvent.class});
    }

    @Override
    public List<Object> getListOfEventsFrom(List<BrDataStructureInterface.TreeResponseInterval> intervals) {
        if (intervals == null) {
            return new ArrayList<>();
        }

        List<Object> objects = new ArrayList<>(intervals.size());

        String className = BrVwapEvent.class.toString().split(" ")[1];
        intervals.forEach(treeResponseInterval -> {
            objects.add(treeResponseInterval.events.get(className));
        });

        return objects;
    }
}
