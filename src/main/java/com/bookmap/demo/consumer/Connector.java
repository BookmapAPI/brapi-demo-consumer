package com.bookmap.demo.consumer;

import com.bookmap.addons.broadcasting.api.view.BrDataStructureInterface;
import com.bookmap.addons.broadcasting.api.view.BroadcasterConsumer;
import com.bookmap.addons.broadcasting.api.view.Event;
import com.bookmap.addons.broadcasting.api.view.GeneratorInfo;
import com.bookmap.demo.consumer.GUI.PanelWithEvents;
import com.bookmap.demo.consumer.listeners.ConnectionListener;
import com.bookmap.demo.consumer.listeners.EventListener;
import com.bookmap.demo.consumer.listeners.FilterListener;
import com.bookmap.demo.consumer.listeners.LiveConnectionListener;
import com.bookmap.demo.consumer.listeners.SettingsListener;
import com.bookmap.demo.consumer.providers.Provider;
import com.bookmap.demo.consumer.providers.value.ProviderValueHandler;
import velox.api.layer1.Layer1ApiProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Facade for Broadcasting Api.
 * Responsible for communication with the Broadcasting API.
 * First, the consumer must connect to the addon provider.
 * It can then subscribe to the provider's live data by sending it a listener to which the provider will provide events.
 * Also, after connecting, the consumer can request the provider's BrDataStructureInterface and receive events from it for the time.
 * The connection is made once to the provider.
 */
public class Connector {

    private final Layer1ApiProvider provider;
    private final BroadcasterConsumer broadcasterConsumer;
    private final ConnectionListener connectionListener;
    private final Provider providerAddon;
    private final Map<String, FilterListener> filterListenersByGeneratorName = new ConcurrentHashMap<>();
    private final Map<String, SettingsListener> settingsListenersByGeneratorName = new ConcurrentHashMap<>();
    private final Map<String, LiveConnectionListener> liveSubscriptionListenersByGeneratorName = new ConcurrentHashMap<>();
    private BrDataStructureInterface dataStructureInterface;
    private com.bookmap.demo.consumer.listeners.EventListener eventListener;

    public Connector(Layer1ApiProvider provider, BroadcasterConsumer broadcasterConsumer,Provider providerAddon) {
        this.provider = provider;
        this.broadcasterConsumer = broadcasterConsumer;
        this.providerAddon = providerAddon;
        Consumer<Boolean> removeDataStructureInterface = status -> {
            if(Boolean.FALSE.equals(status)){
                dataStructureInterface = null;
            }
        };
        connectionListener = new ConnectionListener(provider,removeDataStructureInterface);
    }

    public void connect(){
        broadcasterConsumer.connectToProvider(providerAddon.getFullName(),connectionListener);
    }

    public void disconnect(){
        broadcasterConsumer.disconnectFromProvider(providerAddon.getFullName());
    }

    public boolean isConnected(){
        return connectionListener.isConnected();
    }

    public void subscribeToLiveData(String generatorName, PanelWithEvents panelWithEvents){
        if(isConnected() && !isSubscribedToLive(generatorName)) {
            Optional<GeneratorInfo> generatorInfoOptional = getGeneratorInfo(generatorName);
            generatorInfoOptional.ifPresent(generatorInfo -> {
                //Creating filter and generator settings update listeners.
                FilterListener filterListener = filterListenersByGeneratorName.computeIfAbsent(generatorName, listener ->
                        new FilterListener(providerAddon));
                SettingsListener settingsListener = settingsListenersByGeneratorName.computeIfAbsent(generatorName, listener ->
                        new SettingsListener(provider, providerAddon));
                //Creating a live event subscription status listener.
                LiveConnectionListener subscriptionListener =
                        liveSubscriptionListenersByGeneratorName.computeIfAbsent(generatorName, listener ->
                                new LiveConnectionListener(provider));

                //Creating a listener for the events themselves.
                eventListener = new EventListener(providerAddon, panelWithEvents, filterListener);

                broadcasterConsumer.setListenersForGenerator(providerAddon.getFullName(),generatorName,filterListener,settingsListener);
                //Trying to subscribe for live events.
                //Broadcasting will notify us of a successful subscription through the LiveConnectionListener.
                broadcasterConsumer.subscribeToLiveData(providerAddon.getFullName(), generatorInfo.getGeneratorName(),
                        eventListener, subscriptionListener);
            });
        }
    }

    public void unsubscribeFromLiveData(String generatorName){
        if(isConnected()) {
            Optional<GeneratorInfo> generatorInfo = getGeneratorInfo(generatorName);
            generatorInfo.ifPresent(info -> broadcasterConsumer.unsubscribeFromLiveData(providerAddon.getFullName(),
                    info.getGeneratorName()));
        }
    }

    public Optional<GeneratorInfo> getGeneratorInfo(String generatorName){
        GeneratorInfo generatorInfo = null;
        List<GeneratorInfo> generatorsInfo = broadcasterConsumer.getGeneratorsInfo(providerAddon.getFullName());
        for(GeneratorInfo generator : generatorsInfo){
            if(generator.getGeneratorName().equals(generatorName)){
                generatorInfo = generator;
            }
        }
        return Optional.ofNullable(generatorInfo);
    }

    public void addPanelToLiveListener(PanelWithEvents panelWithEvents){
        if(eventListener != null){
            eventListener.addPanel(panelWithEvents);
        }
    }

    public List<String> getGeneratorsNames(){
        List<String> result = new ArrayList<>();
        List<GeneratorInfo> generatorsInfo = broadcasterConsumer.getGeneratorsInfo(providerAddon.getFullName());
        for(GeneratorInfo generator : generatorsInfo){
            result.add(generator.getGeneratorName());
        }
        return result;
    }

    public boolean isSubscribedToLive(String generatorName){
        LiveConnectionListener listener = liveSubscriptionListenersByGeneratorName.get(generatorName);
        return listener != null && listener.isSubscribed();
    }


    public List<Event> getHistoricalData(String alias,String generatorName, long timeIntervalStartTime){
        if(dataStructureInterface == null) {
            dataStructureInterface = broadcasterConsumer.getDataStructureInterface(providerAddon.getFullName());
        }

        Optional<GeneratorInfo> generatorInfo = getGeneratorInfo(generatorName);
        if(generatorInfo.isPresent()) {
            FilterListener filterListener = filterListenersByGeneratorName.computeIfAbsent(generatorName, listener ->
                    new FilterListener(providerAddon));
            SettingsListener settingsListener = settingsListenersByGeneratorName.computeIfAbsent(generatorName, listener ->
                    new SettingsListener(provider, providerAddon));
            broadcasterConsumer.setListenersForGenerator(providerAddon.getFullName(),generatorName,filterListener,settingsListener);

            List<Event> events;
            if (providerAddon != Provider.AVWAP) {
                //The add-on makes a query to get events for the TimeIntervalStartTime interval up to the present time.
                List<Object> objects = providerAddon.getValueHandler().requestHistoricalData(dataStructureInterface, generatorName,
                        timeIntervalStartTime, provider.getCurrentTime(), alias);

                if (objects == null) {
                    objects = new ArrayList<>();
                }

                //For everything we get through Broadcasting we have to do a cast with CastUtilities (deserialize).
                events = providerAddon.getValueHandler().castEventsInOurClassLoader(objects);
            } else {
                ProviderValueHandler valueHandler = providerAddon.getValueHandler();
                List<BrDataStructureInterface.TreeResponseInterval> treeResponseIntervals =
                        valueHandler.requestAggregatedHistoricalData(dataStructureInterface,
                                generatorName, timeIntervalStartTime, provider.getCurrentTime(), alias);

                events = valueHandler.castEventsInOurClassLoader(valueHandler.getListOfEventsFrom(treeResponseIntervals));
            }
            return filterListener.toFilter(events);
        } else {
            return new ArrayList<>();
        }
    }

    public Optional<Object> getProviderSettings(String generatorName){
        if(!isConnected()){
            return Optional.empty();
        }
        List<GeneratorInfo> generatorsInfo = broadcasterConsumer.getGeneratorsInfo(providerAddon.getFullName());
        for(GeneratorInfo generator : generatorsInfo){
            if(generator.getGeneratorName().equals(generatorName)){
                Object settings = generator.getSettings();
                if(settings != null){
                    return Optional.ofNullable(providerAddon.getValueHandler().castSettings(settings));
                }
            }
        }

        SettingsListener settingsListener = settingsListenersByGeneratorName.get(generatorName);
        if(settingsListener != null){
            return Optional.ofNullable(settingsListener.getSettings());
        }
        return Optional.empty();
    }
}
