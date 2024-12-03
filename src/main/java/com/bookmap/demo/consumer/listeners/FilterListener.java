package com.bookmap.demo.consumer.listeners;

import com.bookmap.addons.broadcasting.api.view.Event;
import com.bookmap.addons.broadcasting.api.view.EventFilter;
import com.bookmap.addons.broadcasting.api.view.listeners.UpdateFilterListener;
import com.bookmap.demo.consumer.DebugLogger;
import com.bookmap.demo.consumer.providers.Provider;

import java.util.List;

/**
 * The listener that will be notified by BrAPI when the provider filter changes.
 * The provider generates raw events. Using the provider's filter,
 * we will get the same data that the provider itself works with.
 */
public class FilterListener implements UpdateFilterListener {

    private final Provider provider;
    private EventFilter<Event> eventFilter;

    public FilterListener(Provider provider) {
        this.provider = provider;
    }

    @Override
    public void reactToFilterUpdates(Object eventFilter) {
        if (eventFilter != null) {
            this.eventFilter = provider.getValueHandler().castFilter(eventFilter);
        }
        DebugLogger.printLog(this.getClass(),"Got the new filter - " + eventFilter);
    }

    public Event toFilter(Event event) {
        if(eventFilter != null){
            return eventFilter.toFilter(event);
        }
        return event;
    }

    public List<Event> toFilter(List<Event> events) {
        if(eventFilter != null){
            return eventFilter.toFilter(events);
        }
        return events;
    }
}
