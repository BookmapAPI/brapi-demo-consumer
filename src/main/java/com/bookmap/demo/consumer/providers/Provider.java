package com.bookmap.demo.consumer.providers;

import com.bookmap.demo.consumer.Connector;
import com.bookmap.demo.consumer.providers.instruments.AvwapInstrumentsController;
import com.bookmap.demo.consumer.providers.instruments.DefaultInstrumentsController;
import com.bookmap.demo.consumer.providers.instruments.InstrumentsController;
import com.bookmap.demo.consumer.providers.instruments.MarketPulseInstrumentsController;
import com.bookmap.demo.consumer.providers.instruments.StrengthLevelInstrumentsController;
import com.bookmap.demo.consumer.providers.instruments.TaIndicatorInstrumentsController;
import com.bookmap.demo.consumer.providers.value.AbsorptionAndSweepsValueHandler;
import com.bookmap.demo.consumer.providers.value.AvwapValueHandler;
import com.bookmap.demo.consumer.providers.value.BreakevenPointValueHandler;
import com.bookmap.demo.consumer.providers.value.IntrinsicValueHandler;
import com.bookmap.demo.consumer.providers.value.MarketPulseValueHandler;
import com.bookmap.demo.consumer.providers.value.ProviderValueHandler;
import com.bookmap.demo.consumer.providers.value.SimpleDemoProviderValueHandler;
import com.bookmap.demo.consumer.providers.value.SitValueHandler;
import com.bookmap.demo.consumer.providers.value.StrengthLevelValueHandler;
import com.bookmap.demo.consumer.providers.value.TaIndicatorAdxValueHandler;
import com.bookmap.demo.consumer.providers.value.TaIndicatorMacdValueHandler;
import com.bookmap.demo.consumer.providers.value.TaIndicatorObvValueHandler;
import com.bookmap.demo.consumer.providers.value.TaIndicatorRsiValueHandler;
import com.bookmap.demo.consumer.providers.value.TaIndicatorWilliamsRValueHandler;

public enum  Provider {
    ABSORPTION_INDICATOR("Absorption indicator","velox.indicators.absorption.AbsorptionIndicator",
            AbsorptionAndSweepsValueHandler.class, DefaultInstrumentsController.class),
    SWEEPS_INDICATOR("Sweeps indicator","velox.indicators.absorption.SweepsIndicator",
            AbsorptionAndSweepsValueHandler.class, DefaultInstrumentsController.class),
    SIT_INDICATOR("Stops&Icebergs On-Chart","velox.indicators.sionchart.SitIndicator",
            SitValueHandler.class, DefaultInstrumentsController.class),
    B3_INDICATOR("B3 Icebergs On-Chart","velox.indicators.sionchart.B3Indicator",
            SitValueHandler.class, DefaultInstrumentsController.class),
    MARKET_PULSE("Market Pulse","com.bookmap.addons.marketpulse.app.MarketPulse",
            MarketPulseValueHandler.class, MarketPulseInstrumentsController.class),
    BREAKEVEN_POINT("Breakeven Point","com.bookmap.addons.breakevenpoint.BreakevenPoint",
            BreakevenPointValueHandler.class, DefaultInstrumentsController.class),
    INTRINSIC_MIDPRICE("Intrinsic Midprice","com.bookmap.addons.intrinsic.IntrinsicMidprice",
                 IntrinsicValueHandler.class, DefaultInstrumentsController.class),
    STRENGTH_LEVEL("Strength level addon", "com.bookmap.addons.strengthlevel.StrengthLevelAddon",
            StrengthLevelValueHandler.class, StrengthLevelInstrumentsController.class),
    AVWAP("Anchored VWAP","com.bookmap.addons.avwap.Avwap",
            AvwapValueHandler.class, AvwapInstrumentsController.class),
    SIMPLE_DEMO_PROVIDER("Simple Demo Provider", "com.bookmap.demo.simple.provider.SimpleDemoProvider",
            SimpleDemoProviderValueHandler.class, DefaultInstrumentsController.class),
    //technical indicators
    ADX("ADX", "com.bookmap.addons.ta.adx.Adx",
            TaIndicatorAdxValueHandler.class, TaIndicatorInstrumentsController.class),
    MACD("MACD", "com.bookmap.addons.ta.macd.Macd",
            TaIndicatorMacdValueHandler.class, TaIndicatorInstrumentsController.class),
    OBV("OBV", "com.bookmap.addons.ta.obv.Obv",
            TaIndicatorObvValueHandler.class, TaIndicatorInstrumentsController.class),
    RSI("RSI", "com.bookmap.addons.ta.rsi.Rsi",
            TaIndicatorRsiValueHandler.class, TaIndicatorInstrumentsController.class),
    WILLIAMSR("Williams%R", "com.bookmap.addons.ta.williamsr.WilliamsR",
            TaIndicatorWilliamsRValueHandler.class, TaIndicatorInstrumentsController.class);

    private final String shortName;
    private final String fullName;
    private final Class<? extends ProviderValueHandler> valueHandlerClass;
    private final Class<? extends InstrumentsController> instrumentsControllerClass;
    private ProviderValueHandler valueHandler;


    Provider(String shortName, String fullName,
             Class<? extends ProviderValueHandler> valueHandler,
             Class<? extends InstrumentsController> instrumentsController) {
        this.shortName = shortName;
        this.fullName = fullName;
        valueHandlerClass = valueHandler;
        instrumentsControllerClass = instrumentsController;
    }

    public String getShortName() {
        return shortName;
    }

    public String getFullName() {
        return fullName;
    }

    public ProviderValueHandler getValueHandler() {
        if(valueHandler != null){
            return valueHandler;
        }
        valueHandler = ProviderValueHandler.getInstance(valueHandlerClass);
        return valueHandler;
    }

    public InstrumentsController getInstrumentsController(Connector connector) {
        return InstrumentsController.getInstance(instrumentsControllerClass, connector);
    }

    public static Provider identifyProvider(String shortName){
        for(Provider provider : Provider.values()){
            if(shortName.equals(provider.getShortName())){
                return provider;
            }
        }

        throw new IllegalArgumentException("An unknown provider was selected.");
    }
}
