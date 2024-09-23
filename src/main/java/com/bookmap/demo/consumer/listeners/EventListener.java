package com.bookmap.demo.consumer.listeners;

import com.bookmap.demo.consumer.GUI.PanelWithEvents;
import com.bookmap.demo.consumer.providers.Provider;
import com.bookmap.addons.broadcasting.api.view.Event;
import com.bookmap.addons.broadcasting.api.view.listeners.LiveEventListener;

import java.util.*;


/**
 * The listener that is passed to BrAPI.
 * The add-on provider will broadcast its live events to this listener.
 */
public class EventListener implements LiveEventListener {

    private final Provider provider;
    private final Set<PanelWithEvents> areasForLiveData;
    private final FilterListener filterListener;
    private final LinkedList<Event> eventsCache;

    public EventListener(Provider provider, PanelWithEvents areaForLiveData, FilterListener filterListener) {
        this.provider = provider;
        this.filterListener = filterListener;
        eventsCache = new LinkedList<>();
        areasForLiveData = new HashSet<>();
        areasForLiveData.add(areaForLiveData);
    }

    @Override
    public void giveEvent(Object o) {
        if (o != null) {
            Event event = provider.getValueHandler().castEventInOurClassLoader(o);

            if (filterListener != null) {
                event = filterListener.toFilter(event);
            }

            synchronized (this) {
                for (PanelWithEvents areaForLiveData : areasForLiveData) {
                    if (areaForLiveData != null) {
                        areaForLiveData.appendEvent(event, provider);
                    }
                }

                eventsCache.add(event);
                if (eventsCache.size() > 30) {
                    eventsCache.removeFirst();
                }
            }
        }
    }

    public void addPanel(PanelWithEvents areaForLiveData){
        synchronized (this) {
            for (Event event : eventsCache) {
                areaForLiveData.appendEvent(event, provider);
            }
            areasForLiveData.add(areaForLiveData);
        }

    }
}
