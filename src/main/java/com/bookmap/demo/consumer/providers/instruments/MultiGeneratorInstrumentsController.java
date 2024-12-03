package com.bookmap.demo.consumer.providers.instruments;

import com.bookmap.demo.consumer.Connector;

import javax.swing.*;
import java.util.Map;

//Base class for InstrumentsControllers providers that have multiple generators per alias.
public abstract class MultiGeneratorInstrumentsController extends InstrumentsController{

    public MultiGeneratorInstrumentsController(Connector connector) {
        super(connector);
    }

    @Override
    public JComboBox<String> getSelectionOfGenerators(String alias) {
        Map<String, String> generatorNameByDisplayName = getGeneratorNameByDisplayName();
        Map<String, String> aliasByGeneratorName = getAliasByGeneratorName();

        JComboBox<String> comboBox = new JComboBox<>();
        generatorNameByDisplayName.forEach((nameAndModelId, generatorId) -> comboBox.addItem(nameAndModelId));

        setSelectedItem(comboBox, generatorNameByDisplayName, aliasByGeneratorName);

        comboBox.addActionListener(e -> {
            setDefaultSelectedName(comboBox, generatorNameByDisplayName, aliasByGeneratorName);
        });

        setEnable(comboBox);

        return comboBox;
    }

    private void setSelectedItem(JComboBox<String> comboBox,
                                 Map<String, String> generatorNameByDisplayName,
                                 Map<String, String> aliasByGeneratorName){
        String oldSelectedGeneratorName = selectedGenerator.get();
        if(oldSelectedGeneratorName != null) {
            if(generatorNameByDisplayName.containsValue(oldSelectedGeneratorName)) {
                String displayName = "";
                for(Map.Entry<String,String> entry : generatorNameByDisplayName.entrySet()){
                    if(entry.getValue().equals(oldSelectedGeneratorName)){
                        displayName = entry.getKey();
                    }
                }
                comboBox.setSelectedItem(displayName);
            } else {
                setDefaultSelectedName(comboBox, generatorNameByDisplayName, aliasByGeneratorName);
            }
        } else {
            setDefaultSelectedName(comboBox, generatorNameByDisplayName, aliasByGeneratorName);
        }
    }

    private void setDefaultSelectedName(JComboBox<String> comboBox,
                                 Map<String, String> generatorNameByDisplayName,
                                 Map<String, String> aliasByGeneratorName){
        String selectedName = (String) comboBox.getSelectedItem();
        if (selectedName != null) {
            String generatorName = generatorNameByDisplayName.get(selectedName);
            selectedGenerator.set(generatorName);
            selectedAlias.set(aliasByGeneratorName.get(generatorName));
        }
    }

    private void setEnable(JComboBox<String> comboBox){
        boolean isConnected = connector.isConnected();
        boolean isSubscribed = false;

        String oldSelectedGenerator = selectedGenerator.get();
        if(oldSelectedGenerator != null) {
            isSubscribed = connector.isSubscribedToLive(selectedGenerator.get());
        }

        if(isConnected && !isSubscribed){
            comboBox.setEnabled(true);
        } else if(isConnected && isSubscribed){
            comboBox.setEnabled(false);
        } else {
            comboBox.setEnabled(false);
        }
    }

    @Override
    public boolean displayOneGeneratorOnAllAliases() {
        return true;
    }

    protected abstract Map<String, String> getGeneratorNameByDisplayName();

    protected abstract Map<String, String> getAliasByGeneratorName();
}
