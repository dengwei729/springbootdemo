package com.example.demo.start.stats;
/**
 * @author dengwei
 */
public class InitStatsManager extends BaseStatsManager {
    public void startInit(String beanName) {
        start(beanName);
    }

    public void endInit(String beanName) {
        end(beanName);
    }

    public long getTotalInit() {
        return getTotal();
    }
}
