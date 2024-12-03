package com.bookmap.demo.consumer.providers.instruments;

import com.bookmap.demo.consumer.Connector;
import com.bookmap.addons.marketpulse.broadcasting.implementations.MPWidgetSettingsV1;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In Market Pulse, generators mean widgets.
 * This controller is responsible for which widget we will subscribe to.
 */
public class MarketPulseInstrumentsController extends MultiGeneratorInstrumentsController{

    public MarketPulseInstrumentsController(Connector connector) {
        super(connector);
    }

    @Override
    protected Map<String, String> getGeneratorNameByDisplayName() {
        Map<String, String> result = new HashMap<>();

        List<String> generatorsNames = new ArrayList<>();
        if(connector.isConnected()) {
            generatorsNames = connector.getGeneratorsNames();
        }

        Map<String, Integer> nameCounter = new HashMap<>();
        for (String generatorId : generatorsNames) {
            Optional<Object> providerSettings = connector.getProviderSettings(generatorId);
            if(providerSettings != null && providerSettings.isPresent()) {
                MPWidgetSettingsV1 widgetSettingsV1 = (MPWidgetSettingsV1) providerSettings.get();
                String nameAndModelId = widgetSettingsV1.getName() + "/" + widgetSettingsV1.getModelId();
                nameAndModelId = addName(nameAndModelId, nameCounter);
                result.put(nameAndModelId, generatorId);
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

        for (String generatorId : generatorsNames) {
            Optional<Object> providerSettings = connector.getProviderSettings(generatorId);
            if(providerSettings != null && providerSettings.isPresent()) {
                MPWidgetSettingsV1 widgetSettingsV1 = (MPWidgetSettingsV1) providerSettings.get();

                Optional<String> firstAlias = widgetSettingsV1.getAliases().stream().findFirst();
                if(firstAlias.isPresent()){
                    String alias = firstAlias.get();
                    result.put(generatorId, alias);
                }
            }
        }
        return result;
    }

    public String addName(String name, Map<String, Integer> nameCounter) {
        if (nameCounter.containsKey(name)) {
            int count = nameCounter.get(name) + 1;
            nameCounter.put(name, count);
            return name + " (" + count + ")";
        } else {
            nameCounter.put(name, 0);
            return name;
        }
    }
}
