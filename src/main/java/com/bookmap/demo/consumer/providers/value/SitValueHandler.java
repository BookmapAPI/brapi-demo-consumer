package com.bookmap.demo.consumer.providers.value;

import com.bookmap.addons.broadcasting.api.view.Event;
import com.bookmap.addons.broadcasting.api.view.EventFilter;
import com.bookmap.addons.broadcasting.implementations.base.CastUtilities;
import com.bookmap.addons.broadcasting.implementations.base.FailedToCastObject;
import velox.api.layer1.data.InstrumentInfo;
import velox.indicators.sionchart.broadcasting.EventInterface;
import velox.indicators.sionchart.broadcasting.SitSettings;
import velox.indicators.sionchart.broadcasting.implementations.IcebergEvent;
import velox.indicators.sionchart.broadcasting.implementations.StopEvent;
import velox.indicators.sionchart.broadcasting.implementations.ThresholdFilter;

import java.util.LinkedList;
import java.util.List;

public class SitValueHandler implements ProviderValueHandler{
    @Override
    public String[] getTextualVisualizationOfEvent(Event event, InstrumentInfo instrumentInfo) {
        velox.indicators.sionchart.broadcasting.EventInterface eventInterface =
                (velox.indicators.sionchart.broadcasting.EventInterface) event;

        double scale = Math.pow(10, 3);

        double price = eventInterface.getPrice() * instrumentInfo.pips;
        price = Math.ceil(price * scale) / scale;

        double size = eventInterface.getSize() / instrumentInfo.sizeMultiplier;
        size = Math.ceil(size * scale) / scale;

        String firstRow = String.format("Price=%s, Size=%s, isBid=%s,Time=%s",
                price,size,eventInterface.isBid(),
                ProviderValueHandler.convertTime(event.getTime()),eventInterface.getType());
        String secondRow = String.format(" Type=%s,TotalSize=%s;", eventInterface.getType(),
                ((EventInterface) event).getTotalSize());

        return new String[] {firstRow,secondRow};
    }

    @Override
    public String getGeneratorSettingsInfo(Object providerSettings) {
        SitSettings settings = (SitSettings) providerSettings;
        int icebergsThreshold = settings.getIcebergsThreshold();
        int stopsThreshold = settings.getStopsThreshold();

        return "Individual thresholds: icebergs - " + icebergsThreshold + ", stops - "
                + stopsThreshold;
    }

    @Override
    public Event castEventInOurClassLoader(Object o) {
        Event event = null;
        try {
            if(o.getClass().getName().equals(IcebergEvent.class.getName())) {
                event = CastUtilities.castObject(o,IcebergEvent.class);
            } else if(o.getClass().getName().equals(StopEvent.class.getName())){
                event = CastUtilities.castObject(o,StopEvent.class);
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
        try {
            return CastUtilities.castObject(o, ThresholdFilter.class);
        } catch (FailedToCastObject e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object castSettings(Object o) {
        try {
            return CastUtilities.castObject(o, SitSettings.class);
        } catch (FailedToCastObject e) {
            throw new RuntimeException(e);
        }
    }
}
