package com.example.demo.start.stats;

import java.util.List;
import java.util.Map;

import com.example.demo.common.PropertyConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.Ordered;

/**
 * @author dengwei
 */
@Slf4j
public class StatsManager implements ApplicationListener<ContextRefreshedEvent>, Ordered {
    private WaitStatsManager waitStatsManager;
    private InitStatsManager initStatsManager;
    private GenericApplicationContext context;
    private int reportNum;
    private boolean logged = false;

    public StatsManager(Thread mainThread, GenericApplicationContext context) {
        this.context = context;

        waitStatsManager = new WaitStatsManager(mainThread);
        initStatsManager = new InitStatsManager();
        String reportNumStr = System.getProperty(PropertyConstants.REPORT_NUM);
        reportNum = reportNumStr == null ? 10 : Integer.parseInt(reportNumStr);
    }

    public synchronized void startWait(String beanName) {
        waitStatsManager.startWait(beanName);
    }

    public synchronized void endWait(String beanName) {
        waitStatsManager.endWait(beanName);
    }

    public synchronized Long getWait(String beanName) {
        return waitStatsManager.get(beanName);
    }

    public synchronized void endInit(String beanName) {
        initStatsManager.endInit(beanName);
    }

    public synchronized void startInit(String beanName) {
        initStatsManager.startInit(beanName);
    }

    public synchronized Long getInit(String beanName) {
        return initStatsManager.get(beanName);
    }

    public void log() {
        log.info("wait: " + waitStatsManager.getTotalWait());
        log.info("init: " + initStatsManager.getTotalInit());

        log.info("top " + reportNum + " wait time bean:");
        for (Map.Entry<String, List<Stat>> entry : waitStatsManager.getTopN(reportNum)){
            String beanName = entry.getKey();
            long waitTime = BaseStatsManager.accumulate(entry.getValue());
            Long initTime = initStatsManager.get(beanName);
            log.info(format(beanName, initTime, waitTime));
        }

        log.info("top " + reportNum + " init time bean:");
        for (Map.Entry<String, List<Stat>> entry : initStatsManager.getTopN(reportNum)){
            String beanName = entry.getKey();
            long initTime = BaseStatsManager.accumulate(entry.getValue());
            Long waitTime = waitStatsManager.get(beanName);
            log.info(format(beanName, initTime, waitTime == null ? 0L : waitTime));
        }

        logged = true;
    }

    private String format(String beanName, long initTime, long waitTime) {
        return String.format("%s, init time: %d, wait time: %d", beanName, initTime, waitTime);
    }


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getSource().equals(context)) {
            this.log();
        }
    }

    public boolean isLogged() {
        return logged;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
