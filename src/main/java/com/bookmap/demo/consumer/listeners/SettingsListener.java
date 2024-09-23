package com.bookmap.demo.consumer.listeners;

import com.bookmap.addons.broadcasting.api.view.listeners.UpdateSettingsListener;
import com.bookmap.demo.consumer.DebugLogger;
import com.bookmap.demo.consumer.ExecutorsUtilities;
import com.bookmap.demo.consumer.providers.Provider;
import velox.api.layer1.Layer1ApiProvider;
import velox.api.layer1.messages.Layer1ApiUserMessageReloadStrategyGui;

/**
 * The listener that will be notified by BrAPI when the provider's settings change.
 * The consumer only gets a clone of the settings and cannot change the provider's settings.
 */
public class SettingsListener implements UpdateSettingsListener {

    private final Layer1ApiProvider provider;
    private final Provider providerAddon;
    private Object settings;
    private Object rawSettings;

    public SettingsListener(Layer1ApiProvider provider, Provider providerAddon) {
        this.provider = provider;
        this.providerAddon = providerAddon;
    }

    @Override
    public void reactToSettingsUpdate(Object o) {
        if(o != null) {
            settings = providerAddon.getValueHandler().castSettings(o);
            if(rawSettings != o){
                ExecutorsUtilities.getExecutor().submit(() -> {
                    this.provider.sendUserMessage(new Layer1ApiUserMessageReloadStrategyGui());
                });
            }
            rawSettings = o;
        }
        DebugLogger.printLog(this.getClass(),"Got the new settings - " + o);
    }

    public Object getSettings() {
        return settings;
    }
}
