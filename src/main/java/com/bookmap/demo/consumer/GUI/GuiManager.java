package com.bookmap.demo.consumer.GUI;

import com.bookmap.demo.consumer.ConnectionManager;
import com.bookmap.demo.consumer.DemoConsumer;
import com.bookmap.demo.consumer.providers.Provider;
import com.bookmap.addons.broadcasting.api.view.Event;
import com.bookmap.demo.consumer.providers.instruments.InstrumentsController;
import com.bookmap.demo.consumer.providers.value.ProviderValueHandler;
import org.apache.commons.lang3.StringUtils;
import velox.api.layer1.Layer1ApiProvider;
import velox.api.layer1.data.InstrumentInfo;
import velox.gui.StrategyPanel;

import javax.swing.*;
import javax.swing.text.DateFormatter;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class GuiManager {
    private final AtomicBoolean isWorking;
    private final String alias;

    private PanelWithEvents areaForHistoricalData;
    private final AtomicLong bottomTimelineOfHistoricalRequest = new AtomicLong();
    private final ConnectionManager connectionManager;
    private final Map<String, InstrumentInfo> instrumentInfo;
    private Map.Entry<String, PanelWithEvents> areaForLiveDataByAlias;

    public GuiManager(AtomicBoolean isWorking, Layer1ApiProvider provider, ConnectionManager connectionManager,
                      String alias, Map<String, InstrumentInfo> instrumentInfo) {
        this.isWorking = isWorking;
        this.alias = alias;
        this.connectionManager = connectionManager;
        this.instrumentInfo = instrumentInfo;
        bottomTimelineOfHistoricalRequest.set(provider.getCurrentTime() - 120_000_000_000L);
        areaForHistoricalData = new PanelWithEvents(instrumentInfo.get(alias));
        areaForLiveDataByAlias = new AbstractMap.SimpleEntry<>(alias, new PanelWithEvents(instrumentInfo.get(alias)));
    }

    public StrategyPanel[] getPanels(){
        StrategyPanel[] panels = new StrategyPanel[4];

        panels[0] = createConnectionPanel();
        if(!isWorking.get()){
            panels[0].setEnabled(false);
        }

        panels[1] = createLiveDataPanel();
        panels[1].setEnabled(isWorking.get() && connectionManager.getConnector().isConnected());

        panels[2] = createHistoryDataPanel();
        panels[2].setEnabled(isWorking.get() && connectionManager.getConnector().isConnected());

        panels[3] = createVersionPanel();

        return panels;
    }

    private StrategyPanel createConnectionPanel(){
        JComboBox<String> providers = new JComboBox<>();
        for(Provider provider : Provider.values()){
            providers.addItem(provider.getShortName());
        }

        providers.setSelectedItem(connectionManager.getConnectedProvider().getShortName());

        providers.setEnabled(!connectionManager.getConnector().isConnected());

        providers.addActionListener(e -> {
            Provider newProvider = Provider.identifyProvider((String) providers.getSelectedItem());
            if(connectionManager.updateProvider(newProvider)){
                areaForLiveDataByAlias.getValue().clear();
                areaForHistoricalData.clear();
            }
        });

        StrategyPanel panel = new StrategyPanel("Connection");
        panel.setLayout(new GridBagLayout());

        panel.add(getConnectButton(providers), createGrid(0, 0, 2, 1));

        panel.add(new JLabel("Provider:"), createGrid(0, 1, 1, 1));
        panel.add(providers, createGrid(1, 1, 1, 1));


        JComboBox<String> selectionOfGenerators =
                connectionManager.getInstrumentController().getSelectionOfGenerators(alias);

        panel.add(new JLabel("Generator:"), createGrid(0, 2, 1, 1));
        panel.add(selectionOfGenerators, createGrid(1, 2, 1, 1));

        return panel;
    }

    private JButton getConnectButton(JComboBox<String> providers){
        JButton button = new JButton();
        if(connectionManager.getConnector().isConnected()){
            button.setText("Disconnect");
            button.addActionListener(e -> connectionManager.getConnector().disconnect());
            providers.setEnabled(false);
        } else {
            button.setText("Connect");
            button.addActionListener(e -> connectionManager.getConnector().connect());
            providers.setEnabled(true);
            areaForLiveDataByAlias.getValue().clear();
            areaForHistoricalData.clear();
        }
        return button;
    }

    private StrategyPanel createLiveDataPanel(){
        StrategyPanel panel = new StrategyPanel("Live events");
        panel.setLayout(new GridBagLayout());

        panel.add(getButtonForSubscribeToLiveData(),createGrid(0,0,2,1));
        panel.add(getLabelWithGeneratorSettings(),createGrid(0,1,2,1));

        JScrollPane jScrollPane = new JScrollPane(areaForLiveDataByAlias.getValue());
        jScrollPane.setPreferredSize(new Dimension(400,200));
        jScrollPane.setSize(new Dimension(400,200));
        panel.add(jScrollPane,createGrid(0,2,2,2));
        return panel;
    }

    private JLabel getLabelWithGeneratorSettings(){
        String generatorName = connectionManager.getInstrumentController().getSelectedGeneratorName();
        Optional<Object> providerSettings = connectionManager.getConnector().getProviderSettings(generatorName);
        ProviderValueHandler valueHandler = connectionManager.getConnectedProvider().getValueHandler();
        if(providerSettings.isPresent()) {
            String generatorSettingsInfo = valueHandler.getGeneratorSettingsInfo(providerSettings.get());
            return new JLabel(generatorSettingsInfo);
        } else {
            return new JLabel("Information about generator settings was not found.");
        }
    }

    private JButton getButtonForSubscribeToLiveData(){
        JButton button = new JButton();

        InstrumentsController instrumentController = connectionManager.getInstrumentController();
        String generatorName = instrumentController.getSelectedGeneratorName();
        String selectedAlias = instrumentController.getSelectedAlias();

        if(connectionManager.getConnector().isSubscribedToLive(generatorName)){
            button.setText("Unsubscribe to live data");

            //InstrumentsController tells us that for a specific provider we will show one panel
            // with live events on all aliases, or panels for each alias.
            if(instrumentController.displayOneGeneratorOnAllAliases()) {
                if(!areaForLiveDataByAlias.getKey().equals(selectedAlias)) {
                    areaForLiveDataByAlias = new AbstractMap.SimpleEntry<>(selectedAlias,
                            new PanelWithEvents(instrumentInfo.get(selectedAlias)));
                }

                connectionManager.getConnector().addPanelToLiveListener(areaForLiveDataByAlias.getValue());
            }

            button.addActionListener(e -> {
                if(connectionManager.getConnector().isConnected()) {
                    String generator = connectionManager.getInstrumentController().getSelectedGeneratorName();
                    connectionManager.getConnector().unsubscribeFromLiveData(generator);
                }
            });
        } else {
            button.setText("Subscribe to live data");
            areaForLiveDataByAlias.getValue().clear();
            button.addActionListener(e -> {
                    String generator = connectionManager.getInstrumentController().getSelectedGeneratorName();
                    connectionManager.getConnector().subscribeToLiveData(generator, areaForLiveDataByAlias.getValue());
            });
        }
        return button;
    }

    private StrategyPanel createHistoryDataPanel(){
        StrategyPanel panel = new StrategyPanel("Historical events");
        panel.setLayout(new GridBagLayout());

        JLabel jLabel = new JLabel("<html>Queries historical data for a period starting at the selected time and ending at the time of the query.</html>");
        jLabel.setPreferredSize(new Dimension(200,70));
        panel.add(jLabel,createGrid(0,0,1,1));
        panel.add(createJSpinner(),createGrid(1,0,1,1));
        panel.add(createJButtonDataRequest(),createGrid(0,1,2,1));
        JScrollPane jScrollPane = new JScrollPane(areaForHistoricalData);
        jScrollPane.setPreferredSize(new Dimension(400,200));
        panel.add(jScrollPane,createGrid(0,2,2,2));

        return panel;
    }

    private JSpinner createJSpinner(){
        Date date = new Date(bottomTimelineOfHistoricalRequest.get() / 1_000_000);
        SpinnerDateModel dateModel = new SpinnerDateModel(date,null,null, Calendar.MINUTE);
        JSpinner spinner = new JSpinner(dateModel);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "HH:mm:ss");
        DateFormatter formatter = (DateFormatter)editor.getTextField().getFormatter();
        formatter.setAllowsInvalid(false);
        formatter.setOverwriteMode(true);

        spinner.addChangeListener(e -> {
            Date spinnerDate = (Date) spinner.getValue();
            bottomTimelineOfHistoricalRequest.set(spinnerDate.getTime() * 1_000_000);
        });

        return spinner;
    }

    private JButton createJButtonDataRequest() {
        JButton button = new JButton("Request data");

        areaForHistoricalData.clear();
        String selectedAlias = connectionManager.getInstrumentController().getSelectedAlias();
        String alias = StringUtils.isEmpty(selectedAlias) ? this.alias : selectedAlias;
        String generatorName = connectionManager.getInstrumentController().getSelectedGeneratorName();
        areaForHistoricalData = new PanelWithEvents(instrumentInfo.get(selectedAlias));

        button.addActionListener(e -> {
            long time = bottomTimelineOfHistoricalRequest.get();
            List<Event> historicalData = connectionManager.getConnector().getHistoricalData(alias,generatorName,time);

            if(historicalData == null){
                button.setEnabled(false);
                areaForHistoricalData.append("This provider does not support requesting historical data.");
                return;
            }

            for (Event event : historicalData) {
                areaForHistoricalData.appendEvent(event,connectionManager.getConnectedProvider());
            }
        });
        return button;
    }

    private StrategyPanel createVersionPanel(){
        StrategyPanel panel = new StrategyPanel(null);
        panel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        panel.add(new JLabel( "Broadcasting API v" + getBrApiVersion() + ", " + DemoConsumer.ADDON_NAME + " v" + getVersion()));

        int numberOfAdditionalLines = panel.getPreferredSize().width / 372 + 1;
        panel.setMinimumSize(new Dimension(panel.getMinimumSize().width, panel.getMinimumSize().height * numberOfAdditionalLines));
        panel.setPreferredSize(new Dimension(panel.getPreferredSize().width, panel.getPreferredSize().height * numberOfAdditionalLines));
        return panel;
    }

    private String getVersion(){
        String version = "";
        try (InputStream inputStream = DemoConsumer.class.getClassLoader().getResourceAsStream("application.properties")) {
            Properties properties = new Properties();
            properties.load(inputStream);
            version = properties.getProperty("version");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return version;
    }

    private String getBrApiVersion(){
        String version = "";
        try (InputStream inputStream = DemoConsumer.class.getClassLoader().getResourceAsStream("application.properties")) {
            Properties properties = new Properties();
            properties.load(inputStream);
            version = properties.getProperty("brapi.version");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return version;
    }

    private static GridBagConstraints createGrid(int x, int y, int width, int height) {
        GridBagConstraints grid = new GridBagConstraints();
        grid.weightx = 0;
        grid.weighty = 0;
        grid.gridx = x;
        grid.gridy = y;
        grid.gridwidth = width;
        grid.gridheight = height;
        grid.fill = GridBagConstraints.HORIZONTAL;
        grid.insets = new Insets(5, 5, 5, 5);
        return grid;
    }
}
