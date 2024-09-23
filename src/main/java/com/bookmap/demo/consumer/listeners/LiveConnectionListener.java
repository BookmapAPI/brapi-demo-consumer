package com.bookmap.demo.consumer.listeners;

import com.bookmap.demo.consumer.DebugLogger;
import com.bookmap.demo.consumer.ExecutorsUtilities;
import com.bookmap.addons.broadcasting.api.view.listeners.LiveConnectionStatusListener;
import velox.api.layer1.Layer1ApiProvider;
import velox.api.layer1.messages.Layer1ApiUserMessageReloadStrategyGui;

/**
 * The listener that will be notified by BrAPI when the provider's live data subscription state changes.
 */
public class LiveConnectionListener implements LiveConnectionStatusListener {
    private final Layer1ApiProvider provider;
    private boolean liveConnectionStatus = false;

    public LiveConnectionListener(Layer1ApiProvider provider) {
        this.provider = provider;
    }


    @Override
    public void reactToStatusChanges(boolean status) {
        if(liveConnectionStatus != status) {
            liveConnectionStatus = status;
            ExecutorsUtilities.getExecutor().submit(() -> {
                provider.sendUserMessage(new Layer1ApiUserMessageReloadStrategyGui());
            });
            DebugLogger.printLog(this.getClass(), "Got the status - " + status);
        }
    }

    public boolean isSubscribed() {
        return liveConnectionStatus;
    }
}
