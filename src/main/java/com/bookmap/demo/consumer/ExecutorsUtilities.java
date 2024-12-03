package com.bookmap.demo.consumer;

import velox.api.layer1.common.helper.ExecutorsHelper;

import java.util.concurrent.*;

public class ExecutorsUtilities {
    private static final Object monitor = new Object();
    private static volatile ExecutorService executor;

    public static ExecutorService getExecutor(){
        if(executor == null){
            synchronized (monitor){
                if(executor == null){
                    executor = ExecutorsHelper.newSingleThreadExecutor("DemoConsumer-executor");
                }
            }
        }
        return executor;
    }

    public static synchronized void shutdown(){
        synchronized (monitor) {
            if(executor != null) {
                executor.shutdown();
                executor = null;
            }
        }
    }
}
