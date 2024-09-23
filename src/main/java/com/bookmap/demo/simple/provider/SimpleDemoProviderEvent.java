package com.bookmap.demo.simple.provider;

import com.bookmap.addons.broadcasting.api.annotations.BrApiModuleComponent;
import com.bookmap.addons.broadcasting.api.view.Event;
import velox.api.layer1.layers.strategies.interfaces.CustomGeneratedEvent;

import java.io.Serial;

@BrApiModuleComponent(version = 1, backwardCompatibility = false)
public class SimpleDemoProviderEvent implements Event, CustomGeneratedEvent {

    @Serial
    private static final long serialVersionUID = -4491750908587725046L;
    private final long time;
    private final double movingAverage;

    public SimpleDemoProviderEvent(long time, double movingAverage) {
        this.time = time;
        this.movingAverage = movingAverage;
    }

    @Override
    public long getTime() {
        return time;
    }

    public double getMovingAverage() {
        return movingAverage;
    }

    @Override
    public String toString() {
        return "SimpleDemoProviderEvent{" +
                "time=" + time +
                ", movingAverage=" + movingAverage +
                '}';
    }

    @Override
    public Object clone() {
        return new SimpleDemoProviderEvent(time, movingAverage);
    }
}
