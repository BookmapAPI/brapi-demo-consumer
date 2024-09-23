package com.bookmap.demo.consumer.providers.value;

import com.bookmap.addons.breakevenpoint.module.BreakevenEvent;
import com.bookmap.addons.breakevenpoint.module.CommissionsSettings;
import com.bookmap.addons.breakevenpoint.module.PaidCommissionEvent;
import com.bookmap.addons.breakevenpoint.module.implementations.BreakevenEventV1;
import com.bookmap.addons.breakevenpoint.module.implementations.CommissionsSettingsV1;
import com.bookmap.addons.breakevenpoint.module.implementations.PaidCommissionEventV1;
import com.bookmap.addons.broadcasting.api.view.BrDataStructureInterface;
import com.bookmap.addons.broadcasting.api.view.Event;
import com.bookmap.addons.broadcasting.api.view.EventFilter;
import com.bookmap.addons.broadcasting.implementations.base.CastUtilities;
import com.bookmap.addons.broadcasting.implementations.base.FailedToCastObject;
import com.bookmap.demo.consumer.DemoConsumer;
import velox.api.layer1.data.InstrumentInfo;

import java.util.LinkedList;
import java.util.List;

public class BreakevenPointValueHandler implements ProviderValueHandler{

    @Override
    public String[] getTextualVisualizationOfEvent(Event event, InstrumentInfo instrumentInfo) {
        if(event instanceof BreakevenEvent breakevenEvent) {
            double pips = instrumentInfo.pips;
            double price = breakevenEvent.getBreakevenPrice() * pips;
            String firstRow = String.format("Price=%s; Short=%s", price, breakevenEvent.isShort());
            return new String[]{firstRow};
        } else if(event instanceof PaidCommissionEvent paidCommissionEvent){
            double commission = paidCommissionEvent.getCommission();
            String firstRow = String.format("Commission=%s;", commission);
            return new String[]{firstRow};
        }
        return new String[]{};
    }

    @Override
    public String getGeneratorSettingsInfo(Object settings) {
        CommissionsSettings commissionsSettings = (CommissionsSettings) settings;
        return "Generator information: Commission enabled - " + commissionsSettings.isCommissionEnabled();
    }

    @Override
    public Event castEventInOurClassLoader(Object o) {
        try {
            if(o.getClass().getName().equals(BreakevenEventV1.class.getName())) {
                return CastUtilities.castObject(o, BreakevenEventV1.class);
            } else if(o.getClass().getName().equals(PaidCommissionEventV1.class.getName())) {
                return CastUtilities.castObject(o, PaidCommissionEventV1.class);
            }
        } catch (FailedToCastObject e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public List<Event> castEventsInOurClassLoader(List<Object> o) {
        List<Event> result = new LinkedList<>();
        for (Object object : o) {
            Event event = castEventInOurClassLoader(object);
            result.add(event);
        }
         return result;
    }

    @Override
    public EventFilter<?> castFilter(Object o) {
        return o1 -> o1;
    }

    @Override
    public Object castSettings(Object o) {
        try {
            return CastUtilities.castObject(o, CommissionsSettingsV1.class);
        } catch (FailedToCastObject e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Object> requestHistoricalData(BrDataStructureInterface dataStructureInterface, String generatorName,
                                              long startTime, long endTime, String alias) {
        if(!validateTime(startTime,endTime)){
            startTime = endTime - timeLimitForRequestingHistoricalData;
        }

        List<Object> result = new LinkedList<>();
        int intervalsWidth = 500_000_000;
        int intervalsNumber = (int) Math.ceil((double) (endTime - startTime) / intervalsWidth);
        List<BrDataStructureInterface.TreeResponseInterval> treeResponseIntervals =
                dataStructureInterface.get(DemoConsumer.class, generatorName, startTime, intervalsWidth, intervalsNumber,
                        alias, new Class[]{BreakevenEventV1.class, PaidCommissionEventV1.class});

        for (BrDataStructureInterface.TreeResponseInterval treeResponseInterval : treeResponseIntervals) {
            result.addAll(treeResponseInterval.events.values());
        }
        return result;
    }


    private boolean validateTime(long startTime, long endTime){
        long diff = endTime - startTime;
        return diff < timeLimitForRequestingHistoricalData;
    }
}
