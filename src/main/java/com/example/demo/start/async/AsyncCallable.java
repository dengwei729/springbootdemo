package com.example.demo.start.async;

import java.util.concurrent.Callable;

import com.example.demo.start.stats.StatsManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AsyncCallable implements Callable {

    public static final int POST_CONSTRUCTOR_INIT_TYPE = 1;
    public static final int INIT_METHOD_INIT_TYPE = 2;

    public final String beanName;
    public final int initType;
    public StatsManager statsManager;
    public boolean sync = true;

    public AsyncCallable(String beanName, int initType, StatsManager statsManager) {
        this.statsManager = statsManager;
        this.beanName = beanName;
        this.initType = initType;
    }

    @Override
    public AsyncCallable call() throws Exception {
        try {
            statsManager.startInit(beanName);
            doCall();
            return this;
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            statsManager.endInit(beanName);
        }
    }

    protected abstract void doCall() throws Throwable;

    @Override
    public String toString() {
        return "AsyncCallable{" +
            "beanName:" + beanName +
            ", sync:" + sync +
            ", initType:" + initType +
            '}';
    }
}
