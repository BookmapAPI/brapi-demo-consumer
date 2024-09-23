package com.bookmap.demo.consumer.providers.value;

import com.bookmap.addons.broadcasting.api.view.Event;
import com.bookmap.addons.broadcasting.api.view.EventFilter;
import com.bookmap.addons.broadcasting.implementations.base.CastUtilities;
import com.bookmap.addons.broadcasting.implementations.base.FailedToCastObject;
import com.bookmap.addons.marketpulse.broadcasting.*;
import com.bookmap.addons.marketpulse.broadcasting.implementations.MPDoubleBarEventV1;
import com.bookmap.addons.marketpulse.broadcasting.implementations.MPFilterV1;
import com.bookmap.addons.marketpulse.broadcasting.implementations.MPSingleBarEventV1;
import com.bookmap.addons.marketpulse.broadcasting.implementations.MPWidgetSettingsV1;
import velox.api.layer1.data.InstrumentInfo;

import java.util.LinkedList;
import java.util.List;

public class MarketPulseValueHandler implements ProviderValueHandler{

    private MPWidgetSettings widgetSettings;

    @Override
    public String[] getTextualVisualizationOfEvent(Event event, InstrumentInfo instrumentInfo) {
        if(widgetSettings != null) {
            double pips = widgetSettings.getPips();
            if (event instanceof MPDoubleBarEvent doubleBarEvent) {
                String zeroRow = "----------------------------";
                String firstRow = String.format("Buy=%s, BuyEstimate=%s, ",
                        (doubleBarEvent.getBuy() * pips), doubleBarEvent.getBuyEstimate());
                String secondRow = String.format("Sell=%s, SellEstimate=%s,",
                        (doubleBarEvent.getSell() * pips), doubleBarEvent.getSellEstimate());
                String thirdRow = String.format("MaxValue=%s;",
                        (doubleBarEvent.getMaxValue()  * pips));
                String fourthRow = "----------------------------";
                return new String[]{zeroRow, firstRow, secondRow, thirdRow, fourthRow};
            } else if (event instanceof MPSingleBarEvent circleEvent) {
                String zeroRow = "----------------------------";
                String firstRow = String.format("Value=%s, Estimate=%s, ",
                        (circleEvent.getValue()  * pips), circleEvent.getEstimate());
                String secondRow = "----------------------------";
                return new String[]{zeroRow, firstRow, secondRow};
            }
        }
        return new String[] {};
    }

    @Override
    public String getGeneratorSettingsInfo(Object settings) {
        MPWidgetSettings mpSettings = (MPWidgetSettings) settings;
        widgetSettings = mpSettings;
        double threshold = Double.parseDouble(mpSettings.getParams().get("threshold"));
        String thresholdStr = String.format("%.2f", threshold);
        return "<html>Generator information:  Threshold - " + thresholdStr + ".</html>";
    }

    @Override
    public Event castEventInOurClassLoader(Object o) {
        MPEvent mpEvent = null;
        try {
            if(o.getClass().getSimpleName().equals(MPDoubleBarEventV1.class.getSimpleName())){
                mpEvent = CastUtilities.castObject(o, MPDoubleBarEventV1.class);
            } else if(o.getClass().getSimpleName().equals(MPSingleBarEventV1.class.getSimpleName())){
                mpEvent = CastUtilities.castObject(o, MPSingleBarEventV1.class);
            }
        } catch (FailedToCastObject e) {
            throw new RuntimeException(e);
        }
        return mpEvent;
    }

    @Override
    public List<Event> castEventsInOurClassLoader(List<Object> o) {
        List<Event> result = new LinkedList<>();
        for (Object object : o) {
            Event events = castEventInOurClassLoader(object);
            result.add(events);
        }
        return result;
    }

    @Override
    public EventFilter<Event> castFilter(Object o) {
        MPFilter filter = null;
        try {
            filter = CastUtilities.castObject(o, MPFilterV1.class);
        } catch (FailedToCastObject e) {
            throw new RuntimeException(e);
        }
        return filter;
    }

    @Override
    public Object castSettings(Object o) {
        MPWidgetSettings settings = null;
        try {
            settings = CastUtilities.castObject(o, MPWidgetSettingsV1.class);
        } catch (FailedToCastObject e) {
            throw new RuntimeException(e);
        }
        return settings;
    }
}
