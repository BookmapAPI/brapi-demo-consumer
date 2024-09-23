package com.bookmap.demo.consumer.listeners;

import com.bookmap.demo.consumer.DebugLogger;
import com.bookmap.demo.consumer.ExecutorsUtilities;
import com.bookmap.addons.broadcasting.api.view.listeners.ConnectionStatusListener;
import velox.api.layer1.Layer1ApiProvider;
import velox.api.layer1.messages.Layer1ApiUserMessageReloadStrategyGui;

import java.util.function.Consumer;

/**
 * A listener that will be notified by BrAPI when the state of the connection to the provider changes.
 */
public class ConnectionListener implements ConnectionStatusListener {

    private final Layer1ApiProvider provider;
    private boolean connectionStatus = false;
    private final Consumer<Boolean> removeDataStructureInterface;

    public ConnectionListener(Layer1ApiProvider provider, Consumer<Boolean> removeDataStructureInterface) {
        this.provider = provider;
        this.removeDataStructureInterface = removeDataStructureInterface;
    }

    @Override
    public void reactToStatusChanges(boolean status) {
        if(connectionStatus != status) {
            connectionStatus = status;
            removeDataStructureInterface.accept(status);
            ExecutorsUtilities.getExecutor().submit(() -> {
                provider.sendUserMessage(new Layer1ApiUserMessageReloadStrategyGui());
            });
            DebugLogger.printLog(this.getClass(), "Got the status - " + status);
        }
    }

    public boolean isConnected(){
        return connectionStatus;
    }
}
