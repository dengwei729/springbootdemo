package com.example.demo.start.stats;

public class WaitStatsManager extends BaseStatsManager {
    private Thread mainThread;


    public WaitStatsManager(Thread mainThread) {
        super();
        this.mainThread = mainThread;
    }

    public void startWait(String beanName) {
        if (Thread.currentThread() == mainThread) {
            start(beanName);
        }
    }

    public void endWait(String beanName) {
        if (Thread.currentThread() == mainThread) {
            end(beanName);
        }
    }

    public long getTotalWait() {
        return getTotal();
    }
}
