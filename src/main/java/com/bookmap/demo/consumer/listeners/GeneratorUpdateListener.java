package com.bookmap.demo.consumer.listeners;

import com.bookmap.addons.broadcasting.api.view.GeneratorInfo;
import com.bookmap.addons.broadcasting.api.view.listeners.ProviderStatusListener;
import com.bookmap.demo.consumer.ConnectionManager;
import com.bookmap.demo.consumer.DebugLogger;
import com.bookmap.demo.consumer.ExecutorsUtilities;
import velox.api.layer1.Layer1ApiProvider;
import velox.api.layer1.messages.Layer1ApiUserMessageReloadStrategyGui;

public class GeneratorUpdateListener implements ProviderStatusListener {

    private final Layer1ApiProvider provider;
    private final ConnectionManager connectionManager;

    public GeneratorUpdateListener(Layer1ApiProvider provider, ConnectionManager connectionManager) {
        this.provider = provider;
        this.connectionManager = connectionManager;
    }

    @Override
    public void providerUpdateGenerator(String provider, String providerId,  GeneratorInfo generator, boolean isOnline) {
        String providerName = connectionManager.getConnectedProvider().getFullName();
        if(providerName.equals(provider)) {
            ExecutorsUtilities.getExecutor().submit(() -> {
                this.provider.sendUserMessage(new Layer1ApiUserMessageReloadStrategyGui());
            });
        }
        DebugLogger.printLog(this.getClass(),"Got update generator, addon provider - " + provider +
                ", generator - " + generator.getGeneratorName() + ", status - " + isOnline);
    }
}
