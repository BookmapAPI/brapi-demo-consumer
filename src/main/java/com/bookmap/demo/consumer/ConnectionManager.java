package com.bookmap.demo.consumer;

import com.bookmap.addons.broadcasting.api.view.BroadcasterConsumer;
import com.bookmap.demo.consumer.providers.Provider;
import com.bookmap.demo.consumer.providers.instruments.InstrumentsController;
import velox.api.layer1.Layer1ApiProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class ConnectionManager {

    private final Layer1ApiProvider provider;
    private final BroadcasterConsumer broadcasterConsumer;
    private final Map<Provider,Connector> connections = new ConcurrentHashMap<>();
    private final Map<Provider,InstrumentsController> instrumentsControllers = new ConcurrentHashMap<>();

    private Connector connector;
    private Provider connectedProvider = Provider.ABSORPTION_INDICATOR;


    public ConnectionManager(Layer1ApiProvider provider, BroadcasterConsumer broadcasterConsumer) {
        this.provider = provider;
        this.broadcasterConsumer = broadcasterConsumer;
        connector = connections.computeIfAbsent(connectedProvider, p -> new Connector(provider, broadcasterConsumer, connectedProvider));
    }

    public synchronized Connector getConnector() {
        return connector;
    }

    public synchronized Provider getConnectedProvider() {
        return connectedProvider;
    }

    public synchronized InstrumentsController getInstrumentController() {
        return instrumentsControllers.computeIfAbsent(connectedProvider,
                provider -> provider.getInstrumentsController(connector));
    }

    public synchronized boolean updateProvider(Provider selectedProvider) {
        if (connector == null || (!selectedProvider.equals(connectedProvider) && !connector.isConnected())) {
            connectedProvider = selectedProvider;
            connector = connections.computeIfAbsent(connectedProvider, p -> new Connector(provider, broadcasterConsumer, selectedProvider));
            return true;
        }
        return false;
    }
}
