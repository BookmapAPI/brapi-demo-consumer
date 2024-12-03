package com.bookmap.demo.consumer.providers.instruments;

import com.bookmap.demo.consumer.Connector;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//Base class for InstrumentsControllers providers that have one generator per alias.
public abstract class SingleGeneratorInstrumentsController extends InstrumentsController{

    public SingleGeneratorInstrumentsController(Connector connector) {
        super(connector);
    }

    @Override
    public JComboBox<String> getSelectionOfGenerators(String alias) {
        JComboBox<String> comboBox = new JComboBox<>();

        List<String> generatorsNames = new ArrayList<>();
        if(connector.isConnected()) {
            generatorsNames = connector.getGeneratorsNames();
        }

        Map<String, String> aliasToGeneratorName = convertAliasToGeneratorName(generatorsNames);
        for (Map.Entry<String, String> entry : aliasToGeneratorName.entrySet()) {
            comboBox.addItem(entry.getKey());
        }

        selectedGenerator.set(aliasToGeneratorName.get(alias));
        selectedAlias.set(alias);
        comboBox.setSelectedItem(alias);
        comboBox.setEnabled(false);
        return comboBox;
    }

    @Override
    public boolean displayOneGeneratorOnAllAliases() {
        return false;
    }

    protected abstract Map<String, String> convertAliasToGeneratorName(List<String> generatorsNames);
}
