package com.bookmap.demo.simple.cosumer;

import com.bookmap.addons.broadcasting.api.view.BroadcasterConsumer;
import com.bookmap.addons.broadcasting.api.view.listeners.ConnectionStatusListener;
import com.bookmap.addons.broadcasting.api.view.listeners.LiveEventListener;
import com.bookmap.addons.broadcasting.implementations.base.CastUtilities;
import com.bookmap.addons.broadcasting.implementations.base.FailedToCastObject;
import com.bookmap.addons.broadcasting.implementations.view.BroadcastFactory;
import com.bookmap.demo.consumer.ExecutorsUtilities;
import velox.api.layer1.*;
import velox.api.layer1.annotations.Layer1ApiVersion;
import velox.api.layer1.annotations.Layer1ApiVersionValue;
import velox.api.layer1.annotations.Layer1Attachable;
import velox.api.layer1.annotations.Layer1StrategyName;
import velox.api.layer1.common.ListenableHelper;
import velox.api.layer1.data.InstrumentInfo;
import velox.api.layer1.messages.Layer1ApiUserMessageReloadStrategyGui;
import velox.api.layer1.messages.UserMessageLayersChainCreatedTargeted;
import velox.gui.StrategyPanel;
import velox.indicators.absorption.broadcasting.module.EventInterface;
import velox.indicators.absorption.broadcasting.module.implementations.TradeEvent;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The simple demo add-on shows how to work with Broadcast API as a consumer.
 * Simple Demo Consumer is the simplest Broadcasting API usage model.
 * It displays the last 10 events received from the Absorption Indicator on the panel.
 * It only subscribes to live Absorption Indicator live events.
 * It does not use the Absorption Indicator filter, it does not request historical data, etc.
 */
@Layer1Attachable
@Layer1StrategyName(SimpleDemoConsumer.ADDON_NAME)
@Layer1ApiVersion(Layer1ApiVersionValue.VERSION2)
public class SimpleDemoConsumer implements
        Layer1ApiFinishable,
        Layer1ApiAdminAdapter,
        Layer1ApiInstrumentAdapter,
        Layer1CustomPanelsGetter
{

    public static final String ADDON_NAME = "Simple Demo Consumer";
    private static final String ADDON_PROVIDER_NAME = "velox.indicators.absorption.AbsorptionIndicator";


    private final Layer1ApiProvider provider;
    private final AtomicBoolean isWorking = new AtomicBoolean(false);
    private final BroadcasterConsumer broadcaster;
    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    private final Map<String,ArrayDeque<String>> lastTenEventsByAlias = new ConcurrentHashMap<>();
    private final List<String> aliases = new CopyOnWriteArrayList<>();


    public SimpleDemoConsumer(Layer1ApiProvider provider) {
        ListenableHelper.addListeners(provider, this);
        this.provider = provider;
        broadcaster = BroadcastFactory.getBroadcasterConsumer(provider, ADDON_NAME, this.getClass());
        connect();
    }

    private void connect(){
        broadcaster.connectToProvider(ADDON_PROVIDER_NAME,
                new ConnectionStatusListener() {
                    @Override
                    public void reactToStatusChanges(boolean status) {
                        boolean oldStatus = isConnected.getAndSet(status);
                        if(!oldStatus && status){
                            aliases.forEach(SimpleDemoConsumer.this::subscribe);
                        }
                    }
                });
    }

    private void subscribe(String alias) {
        if (isConnected.get()) {
            ArrayDeque<String> events = lastTenEventsByAlias.get(alias);

            LiveEventListener liveEventListener = new LiveEventListener() {
                @Override
                public void giveEvent(Object o) {
                    try {
                        EventInterface eventInterface = CastUtilities.castObject(o, TradeEvent.class);
                        if (events.size() > 9) {
                            events.removeFirst();
                        }
                        String event = "Price: " + eventInterface.getPrice()
                                + ", Size: " + eventInterface.getValue()
                                + ", ChainSize: " + eventInterface.getMaxChainSize();
                        events.addLast(event);
                    } catch (FailedToCastObject e) {
                        throw new RuntimeException(e);
                    }
                }
            };

            broadcaster.subscribeToLiveData(ADDON_PROVIDER_NAME, alias,
                    liveEventListener, null);
        }
    }

    @Override
    public void onInstrumentAdded(String alias, InstrumentInfo instrumentInfo) {
        aliases.add(alias);
        lastTenEventsByAlias.put(alias,new ArrayDeque<>(10));
    }

    @Override
    public void onUserMessage(Object data) {
        if (data.getClass() == UserMessageLayersChainCreatedTargeted.class) {
            UserMessageLayersChainCreatedTargeted message = (UserMessageLayersChainCreatedTargeted) data;
            if (message.targetClass == getClass()) {
                isWorking.set(true);
                broadcaster.start();
            }
        }
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
        return getGui(alias);
    }

    private StrategyPanel[] getGui(String alias){
        StrategyPanel reloader = new StrategyPanel(null);
        JButton reload = new JButton("Reload GUI");
        reload.addActionListener(e -> provider.sendUserMessage(new Layer1ApiUserMessageReloadStrategyGui()));
        reloader.add(reload);

        if(isConnected.get()){
            StrategyPanel eventsPanel = new StrategyPanel("Connected");
            JTextArea textArea = new JTextArea();
            textArea.setRows(10);
            lastTenEventsByAlias.get(alias).forEach(event -> textArea.append(event + "\n"));
            eventsPanel.add(textArea);

            return new StrategyPanel[]{eventsPanel,reloader};
        } else {
            StrategyPanel strategyPanel = new StrategyPanel("Disconnected");
            strategyPanel.add(new JLabel("Not connected to " + ADDON_PROVIDER_NAME));
            return new StrategyPanel[]{strategyPanel,reloader};
        }
    }
}