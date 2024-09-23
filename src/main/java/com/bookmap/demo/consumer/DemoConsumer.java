package com.bookmap.demo.consumer;

import com.bookmap.demo.consumer.GUI.GuiManager;
import com.bookmap.addons.broadcasting.api.view.BroadcasterConsumer;
import com.bookmap.addons.broadcasting.implementations.view.BroadcastFactory;
import com.bookmap.demo.consumer.listeners.GeneratorUpdateListener;
import velox.api.layer1.Layer1ApiAdminAdapter;
import velox.api.layer1.Layer1ApiFinishable;
import velox.api.layer1.Layer1ApiInstrumentAdapter;
import velox.api.layer1.Layer1ApiProvider;
import velox.api.layer1.Layer1CustomPanelsGetter;
import velox.api.layer1.annotations.Layer1ApiVersion;
import velox.api.layer1.annotations.Layer1ApiVersionValue;
import velox.api.layer1.annotations.Layer1Attachable;
import velox.api.layer1.annotations.Layer1StrategyName;
import velox.api.layer1.common.ListenableHelper;
import velox.api.layer1.data.InstrumentInfo;
import velox.api.layer1.messages.Layer1ApiUserMessageReloadStrategyGui;
import velox.api.layer1.messages.UserMessageLayersChainCreatedTargeted;
import velox.gui.StrategyPanel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The demo add-on shows how to work with Broadcast API as a consumer.
 */
@Layer1Attachable
@Layer1StrategyName(DemoConsumer.ADDON_NAME)
@Layer1ApiVersion(Layer1ApiVersionValue.VERSION2)
public class DemoConsumer implements
        Layer1ApiFinishable,
        Layer1ApiAdminAdapter,
        Layer1ApiInstrumentAdapter,
        Layer1CustomPanelsGetter
{

    public static final String ADDON_NAME = "Demo Consumer";
    private final AtomicBoolean isWorking = new AtomicBoolean(false);
    private final Map<String, GuiManager> guiManagers = new ConcurrentHashMap<>();
    private final Map<String,InstrumentInfo> instrumentsInfo = new ConcurrentHashMap<>();
    private final Layer1ApiProvider provider;
    private final BroadcasterConsumer broadcaster;
    private final ConnectionManager connectionManager;


    public DemoConsumer(Layer1ApiProvider provider) {
        ListenableHelper.addListeners(provider, this);
        this.provider = provider;
        DebugLogger.setEnabled(true);
        broadcaster = BroadcastFactory.getBroadcasterConsumer(provider, ADDON_NAME, this.getClass());
        connectionManager = new ConnectionManager(provider,broadcaster);
        broadcaster.setProviderStatusListener(new GeneratorUpdateListener(provider,connectionManager));
    }

    @Override
    public void onUserMessage(Object data) {
        if (data.getClass() == UserMessageLayersChainCreatedTargeted.class) {
            UserMessageLayersChainCreatedTargeted message = (UserMessageLayersChainCreatedTargeted) data;
            if (message.targetClass == getClass()) {
                isWorking.set(true);
                ExecutorsUtilities.getExecutor().submit(() -> {
                    provider.sendUserMessage(new Layer1ApiUserMessageReloadStrategyGui());
                });
                broadcaster.start();
            }
        }
    }

    @Override
    public void onInstrumentAdded(String alias, InstrumentInfo instrumentInfo) {
        instrumentsInfo.put(alias,instrumentInfo);
    }

    @Override
    public void finish() {
        broadcaster.finish();
        ExecutorsUtilities.shutdown();
        isWorking.set(false);
    }

    @Override
    public StrategyPanel[] getCustomGuiFor(String alias, String indicatorName) {
        if(!isWorking.get()){
            return new StrategyPanel[0];
        }

        GuiManager guiManager = guiManagers.computeIfAbsent(alias, g ->
                new GuiManager(isWorking,provider, connectionManager, alias, instrumentsInfo));
        return guiManager.getPanels();
    }
}