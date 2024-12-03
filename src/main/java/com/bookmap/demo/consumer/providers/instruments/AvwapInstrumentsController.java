package com.bookmap.demo.consumer.providers.instruments;

import com.bookmap.addons.avwap.broadcasting.module.ProviderSettings;
import com.bookmap.addons.avwap.broadcasting.module.implementation.ProviderSettingsProxy;
import com.bookmap.demo.consumer.Connector;

import javax.swing.*;
import java.util.*;

public class AvwapInstrumentsController extends MultiGeneratorInstrumentsController{

    public AvwapInstrumentsController(Connector connector) {
        super(connector);
    }

    @Override
    protected Map<String, String> getGeneratorNameByDisplayName() {
        Map<String, String> result = new HashMap<>();

        List<String> generatorsNames = new ArrayList<>();
        if(connector.isConnected()) {
            generatorsNames = connector.getGeneratorsNames();
        }

        for (String generatorName : generatorsNames) {
            Optional<Object> providerSettings = connector.getProviderSettings(generatorName);
            if (providerSettings != null && providerSettings.isPresent()) {
                ProviderSettingsProxy settingsProxy = (ProviderSettingsProxy) providerSettings.get();
                result.put(settingsProxy.getAlias() + "::" + settingsProxy.getUsername(), generatorName);
            }
        }
        return result;
    }

    @Override
    protected Map<String, String> getAliasByGeneratorName() {
        Map<String, String> result = new HashMap<>();

        List<String> generatorsNames = new ArrayList<>();
        if(connector.isConnected()) {
            generatorsNames = connector.getGeneratorsNames();
        }

        for (String generatorName : generatorsNames) {
            Optional<Object> providerSettings = connector.getProviderSettings(generatorName);
            if (providerSettings != null && providerSettings.isPresent()) {
                ProviderSettingsProxy settingsProxy = (ProviderSettingsProxy) providerSettings.get();
                result.put(generatorName, settingsProxy.getAlias());
            }
        }

        return result;
    }
}
