package com.bookmap.demo.consumer.providers.instruments;

import com.bookmap.demo.consumer.Connector;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This is the controller interface that responds to which generator to subscribe to.
 */
public abstract class InstrumentsController {

    public static InstrumentsController getInstance(Class<?> controller, Connector connector) {
        if (controller.equals(DefaultInstrumentsController.class)) {
            return new DefaultInstrumentsController(connector);
        } else if (controller.equals(MarketPulseInstrumentsController.class)) {
            return new MarketPulseInstrumentsController(connector);
        } else if (controller.equals(StrengthLevelInstrumentsController.class)) {
            return new StrengthLevelInstrumentsController(connector);
        } else if (controller.equals(AvwapInstrumentsController.class)) {
            return new AvwapInstrumentsController(connector);
        } else if (controller.equals(TaIndicatorInstrumentsController.class)) {
            return new TaIndicatorInstrumentsController(connector);
        } else {
            throw new IllegalArgumentException("Unknown controller class: " + controller);
        }
    }

    protected final AtomicReference<String> selectedGenerator = new AtomicReference<>();
    protected final AtomicReference<String> selectedAlias = new AtomicReference<>();
    protected final Connector connector;

    public InstrumentsController(Connector connector) {
        this.connector = connector;
    }

    public String getSelectedGeneratorName(){
        String generatorName = selectedGenerator.get();
        if(StringUtils.isEmpty(generatorName)){
            return "";
        }
        return generatorName;
    }

    public String getSelectedAlias(){
        String alias = selectedAlias.get();
        if(StringUtils.isEmpty(alias)){
            return "";
        }
        return alias;
    }

    public abstract boolean displayOneGeneratorOnAllAliases();

    public abstract JComboBox<String> getSelectionOfGenerators(final String alias);
}
