package com.bookmap.demo.consumer.providers.value;

import com.bookmap.addons.broadcasting.api.view.Event;
import com.bookmap.addons.broadcasting.api.view.EventFilter;
import com.bookmap.addons.broadcasting.implementations.base.CastUtilities;
import com.bookmap.addons.broadcasting.implementations.base.FailedToCastObject;
import com.bookmap.addons.intrinsic.broadcasting.module.IntrinsicSettings;
import com.bookmap.addons.intrinsic.broadcasting.module.implementation.LineEvent;
import velox.api.layer1.data.InstrumentInfo;
import com.bookmap.addons.intrinsic.broadcasting.module.EventInterface;

import java.util.LinkedList;
import java.util.List;

public class IntrinsicValueHandler implements ProviderValueHandler {

        @Override
        public String[] getTextualVisualizationOfEvent(Event event, InstrumentInfo instrumentInfo) {
            com.bookmap.addons.intrinsic.broadcasting.module.EventInterface eventInterface =
                    (EventInterface) event;

            String firstRow = String.format("Line = %s, Price=%s, Time=%s,",
                    eventInterface.getLineType(),
                    eventInterface.getPrice(),
                    ProviderValueHandler.convertTime(event.getTime())
            );
            return new String[] {firstRow};
        }

        @Override
        public String getGeneratorSettingsInfo(Object providerSettings) {
            IntrinsicSettings settings = (IntrinsicSettings) providerSettings;
            return "Settings: intensity - " + settings.intensity + ", sensitivity - " + settings.sensitivity;
        }

        @Override
        public Event castEventInOurClassLoader(Object o) {
            LineEvent lineEvent;
            try {
                lineEvent = CastUtilities.castObject(o, LineEvent.class);
            } catch (FailedToCastObject e) {
                throw new RuntimeException(e);
            }
            return lineEvent;
        }

        @Override
        public List<Event> castEventsInOurClassLoader(List<Object> o) {
            return new LinkedList<>(CastUtilities.castObjects(o, LineEvent.class));
        }

        @Override
        public EventFilter<Event> castFilter(Object o) {
            return null;
        }

        @Override
        public Object castSettings(Object o) {
            try {
                return CastUtilities.castObject(o, IntrinsicSettings.class);
            } catch (FailedToCastObject e) {
                throw new RuntimeException(e);
            }
        }
}
