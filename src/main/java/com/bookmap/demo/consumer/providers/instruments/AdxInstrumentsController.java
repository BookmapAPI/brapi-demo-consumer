package com.bookmap.demo.consumer.providers.instruments;

import com.bookmap.demo.consumer.Connector;

import javax.swing.*;

public class AdxInstrumentsController extends DefaultInstrumentsController{

    private JComboBox<String> selection;

    public AdxInstrumentsController(Connector connector) {
        super(connector);
    }

    @Override
    public JComboBox<String> getSelectionOfGenerators(String alias) {
        return selection = super.getSelectionOfGenerators(alias);
    }

    @Override
    public String getSelectedGeneratorName(){
        Object selectedItem = selection.getSelectedItem();
        return selectedItem == null
                ? ""
                : selectedItem.toString();
    }
}
