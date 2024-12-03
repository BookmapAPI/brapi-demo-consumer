package com.bookmap.demo.consumer;

import velox.api.layer1.common.Log;

public class DebugLogger {
    private static boolean enabled = false;

    public static void setEnabled(boolean status){
        enabled = status;
    }

    public static void printLog(Class<?> sender, String message){
        if(enabled){
            String text = String.format("DemoConsumer: %s: %s",sender.getSimpleName(), message);
            Log.info(text);
        }
    }
}
