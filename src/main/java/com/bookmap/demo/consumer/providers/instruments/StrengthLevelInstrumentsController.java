package com.bookmap.demo.consumer.providers.instruments;

import com.bookmap.demo.consumer.Connector;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StrengthLevelInstrumentsController extends SingleGeneratorInstrumentsController {

    public StrengthLevelInstrumentsController(Connector connector) {
        super(connector);
    }

    @Override
    protected Map<String, String> convertAliasToGeneratorName(List<String> generatorsNames){
        Map<String, String> result = new HashMap<>();
        for (String generatorsName : generatorsNames) {
            String alias = generatorsName.substring(generatorsName.indexOf(":") + 3);
            result.put(alias, generatorsName);
        }
        return result;
    }
}
