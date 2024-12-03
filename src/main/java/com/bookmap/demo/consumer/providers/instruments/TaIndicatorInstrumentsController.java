package com.bookmap.demo.consumer.providers.instruments;

import com.bookmap.demo.consumer.Connector;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class TaIndicatorInstrumentsController extends DefaultInstrumentsController{

    public TaIndicatorInstrumentsController(Connector connector) {
        super(connector);
    }

    @Override
    public JComboBox<String> getSelectionOfGenerators(String alias) {
        selectedAlias.set(alias);
        JComboBox<String> comboBox = new JComboBox<>();
        List<String> generatorsNames = new ArrayList<>();

        if(connector.isConnected()) {
            generatorsNames = connector.getGeneratorsNames();
        }
        String aliasToGeneratorName = convertAliasToGeneratorName(alias, generatorsNames);
        comboBox.addItem(aliasToGeneratorName);
        selectedGenerator.set(aliasToGeneratorName);
        comboBox.setSelectedItem(aliasToGeneratorName);
        comboBox.setEnabled(false);
        return comboBox;
    }

    private String convertAliasToGeneratorName(String alias, List<String> generatorsNames) {
        for (String generatorsName : generatorsNames) {
            if (generatorsName.contains(alias)) {
                return generatorsName;
            }
        }
        return "";
    }
}
