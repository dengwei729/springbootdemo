package com.example.demo.start.async;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.example.demo.common.PropertyConstants;
import com.example.demo.start.stats.StatsManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.Advised;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;

@Slf4j
public class InitManager implements ApplicationListener<ContextRefreshedEvent>, Ordered  {
    final ExecutorService executorService;
    private final StatsManager statsManager;
    final Map<String, Future<AsyncCallable>> futures;
    int waitTimeout;

    final Map<String, List<AsyncCallable>> beanNameToCallable;
    private final ConcurrentHashMap<Object, String> beanToBeanName;
    private AtomicBoolean shutdown = new AtomicBoolean(false);

    public InitManager(StatsManager statsManager) {
        int threadNum = System.getProperty(PropertyConstants.THREAD_NUM_PROPERTY) == null ?
            Runtime.getRuntime().availableProcessors() * 2 :
            Integer.parseInt(System.getProperty(PropertyConstants.THREAD_NUM_PROPERTY));
        this.executorService = Executors.newFixedThreadPool(threadNum, new AsyncThreadFactory());
        this.waitTimeout = System.getProperty(PropertyConstants.WAIT_TIMEOUT_PROPERTY) == null ?
            60 : Integer.parseInt(System.getProperty(PropertyConstants.WAIT_TIMEOUT_PROPERTY));
        this.futures = new ConcurrentHashMap<String, Future<AsyncCallable>>();
        this.beanNameToCallable = new HashMap<String, List<AsyncCallable>>();
        this.beanToBeanName = new ConcurrentHashMap<Object, String>();

        this.statsManager = statsManager;
    }
    public void asyncAppend(String beanName, Object bean, AsyncCallable callable) {
        String currentValue = beanToBeanName.putIfAbsent(bean, beanName);
        if (currentValue != null) {
            assert beanToBeanName.get(bean).equals(beanName);
        }

        if (!beanNameToCallable.containsKey(beanName)) {
            beanNameToCallable.put(beanName, new ArrayList<AsyncCallable>());
        }

        beanNameToCallable.get(beanName).add(callable);
    }

    public void asyncInit(String beanName) {
        if(beanNameToCallable.get(beanName) != null && !beanNameToCallable.get(beanName).isEmpty()) {
            final List<AsyncCallable> callables = beanNameToCallable.get(beanName);

            futures.put(beanName, executorService.submit(new Callable<AsyncCallable>() {
                @Override
                public AsyncCallable call() throws Exception {
                    Iterator<AsyncCallable> iterator = callables.iterator();
                    AsyncCallable result = null;
                    while (iterator.hasNext()) {
                        AsyncCallable item = iterator.next();
                        result = item.call();
                    }
                    return result;
                }
            }));

            beanNameToCallable.remove(beanName);
        }
    }

    public void syncInit(String beanName) throws Exception {
        assert beanNameToCallable.get(beanName) != null;
        assert !beanNameToCallable.get(beanName).isEmpty();

        final List<AsyncCallable> callableList = beanNameToCallable.get(beanName);
        Iterator<AsyncCallable> iter = callableList.iterator();
        while (iter.hasNext()) {
            AsyncCallable item = iter.next();
            try {
                statsManager.startWait(beanName);
                item.call();
            } finally {
                statsManager.endWait(beanName);
            }
        }

        beanNameToCallable.remove(beanName);
    }

    public void waitByBean(Object bean) throws Exception {
        assert bean!= null;
        Object target = bean;
        if (bean instanceof Advised) {
            target = ((Advised) bean).getTargetSource().getTarget();
        }

        String beanName = beanToBeanName.get(target);
        if (beanName == null) {
            return; // in case user specify bean has no init method
        }
        waitByBeanName(beanName);
    }

    public void waitByBeanName(String beanName) {
        Future<AsyncCallable> future = futures.get(beanName);
        if (future == null) {
            return;
        }

        try {
            doWaitFuture(beanName, future, true);
            futures.remove(beanName); // already waited, reduce cost
        } catch (Throwable e) {
            log.error(e.getMessage(), e);

            if (e instanceof TimeoutException) {
                log.error(String.format("main thread wait for %s bean's init method finish over %d seconds.", beanName, waitTimeout));
                log.error("suggestions:");
                log.error(String.format("  1. analyze thread dump. it could be a dead lock. you can solve this by disable async progress for bean %s through system property %s like -D%s=aaa&bbb&xxx", beanName, PropertyConstants.DISABLE_BEAN_NAMES_PROPERTY, PropertyConstants.DISABLE_BEAN_NAMES_PROPERTY));
                log.error(String.format("  2. analyze thread dump. it could be a normal case where bean %s's init method takes a very long time, you can adjust wait timeout through system property %s whose default value is 60(time unit is second)", beanName, PropertyConstants.WAIT_TIMEOUT_PROPERTY));
                throw new RuntimeException(e);
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    private void shutdown() {
        try {
            beforeShutdown();
            executorService.shutdown();
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void beforeShutdown() throws InterruptedException, ExecutionException, TimeoutException {
        for(Map.Entry<String, Future<AsyncCallable>> entry : futures.entrySet()) {
            doWaitFuture(entry.getKey(), entry.getValue(), false);
        }
    }

    private AsyncCallable doWaitFuture(String beanName, Future<AsyncCallable> future, boolean isTimed) throws InterruptedException, ExecutionException, TimeoutException {
        AsyncCallable callable;

        try {
            statsManager.startWait(beanName);
            callable = (isTimed && this.waitTimeout >= 0) ? future.get(this.waitTimeout, TimeUnit.SECONDS) : future.get();
        } finally {
            statsManager.endWait(beanName);
        }

        return callable;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        if (shutdown.compareAndSet(false, true)) {
            log.info("context refreshed event received. shutdown startup speedup init manager");
            this.shutdown();
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
