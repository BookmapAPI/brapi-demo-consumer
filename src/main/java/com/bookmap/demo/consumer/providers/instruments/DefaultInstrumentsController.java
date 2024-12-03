package com.bookmap.demo.consumer.providers.instruments;

import com.bookmap.demo.consumer.Connector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Most providers have one generator for each instrument, and the name of the generator is alias.
 */
public class DefaultInstrumentsController extends SingleGeneratorInstrumentsController {

    public DefaultInstrumentsController(Connector connector) {
        super(connector);
    }

    @Override
    protected Map<String, String> convertAliasToGeneratorName(List<String> generatorsNames) {
        Map<String, String> result = new HashMap<>();
        for (String generatorsName : generatorsNames) {
            result.put(generatorsName, generatorsName);
        }
        return result;
    }
}
