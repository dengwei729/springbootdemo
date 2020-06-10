package com.example.demo.start;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.example.demo.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;

import static java.lang.Boolean.TRUE;

@Slf4j
public class MyDefaultListableBeanFactory extends DefaultListableBeanFactory {
    private final String afterPropertiesSetMethodName = "afterPropertiesSet";

    private boolean contextFinished = false;

    public static List<Future<Throwable>> taskList = Collections.synchronizedList(new ArrayList<Future<Throwable>>());

    public static ExecutorService asyncInitPoll;

    public MyDefaultListableBeanFactory(BeanFactory parentBeanFactory, int poolSize) {
        super(parentBeanFactory);
        asyncInitPoll = Executors.newFixedThreadPool(poolSize);
    }

    public void makeSureAllAsyncTaskHadSuccessfulInvoked() {
        if(taskList.size()>0) {
            long start = System.currentTimeMillis();
            try {
                for (Future<Throwable> task: taskList) {
                    Throwable result = task.get();
                    if (result != null) {
                        throw result;
                    }
                }
            }catch (Throwable e) {
                if (e instanceof BeanCreationException) {
                    throw (BeanCreationException) e;
                } else {
                    throw  new BeanCreationException(e.getMessage(), e);
                }
            } finally {
                log.warn("async invoke left time:" + (System.currentTimeMillis() - start));
            }
        }
        contextFinished = true;
        asyncInitPoll.shutdown();
    }

    @Override
    protected void invokeInitMethods(String beanName, Object bean, RootBeanDefinition mbd) throws Throwable {

        if (!canAsyncInit(bean, mbd)) {
            super.invokeInitMethods(beanName, bean, mbd);
            return;
        }
        boolean isInitializingBean = (bean instanceof InitializingBean);
        final boolean needInvokeAfterPropertiesSetMethod = isInitializingBean && (mbd == null || !mbd.isExternallyManagedInitMethod(afterPropertiesSetMethodName));

        final String initMethodName = (mbd != null ? mbd.getInitMethodName() : null);
        /**
         * initMethod 与{@link afterPropertiesSetMethodName} 相同且{@link afterPropertiesSetMethodName} 方法被执行时，则不再调initMethod
         */
        final boolean needInvokeInitMethod = initMethodName != null && !(isInitializingBean && afterPropertiesSetMethodName.equals(initMethodName)) &&
            !mbd.isExternallyManagedInitMethod(initMethodName);

        if (needInvokeAfterPropertiesSetMethod || needInvokeInitMethod) {
            asyncInvoke(new BeanInitMethodsInvoker() {
                @Override
                public void invoke() throws Throwable {

                    if (needInvokeAfterPropertiesSetMethod) {
                        invokeInitMethod(beanName, bean, afterPropertiesSetMethodName, false);
                    }

                    if(needInvokeInitMethod) {
                        invokeInitMethod(beanName, bean, initMethodName, mbd.isEnforceInitMethod());
                    }
                }

                @Override
                public String getBeanName() {
                    return beanName;
                }
            });
        }


    }

    private void invokeInitMethod(String beanName, Object bean, String method, boolean enforceInitMethod) throws Throwable {
        Method initMethod = BeanUtils.findMethod(bean.getClass(), method, null);
        if (initMethod == null) {
            if (enforceInitMethod) {
                throw new NoSuchMethodException("Couldn't find an init method named '" + method +
                    "' on bean with name '" + beanName + "'");
            }
        } else {
            initMethod.setAccessible(true);
            initMethod.invoke(bean);
        }
    }

    private void asyncInvoke(final BeanInitMethodsInvoker beanInitMethodsInvoker) {
        taskList.add((asyncInitPoll.submit(new Callable<Throwable>() {
            @Override
            public Throwable call() throws Exception {
                long start = System.currentTimeMillis();
                try {
                    beanInitMethodsInvoker.invoke();
                    return  null;
                } catch (Throwable throwable) {
                    return new BeanCreationException((beanInitMethodsInvoker.getBeanName()));
                }
            }
        })));
    }

    private boolean canAsyncInit(Object bean, RootBeanDefinition mbd) {
        if (contextFinished || mbd == null || mbd.isLazyInit() || bean instanceof FactoryBean) {
            return false;
        }
        Object value = mbd.getAttribute(Constants.ASYNC_INIT);
        return TRUE.equals(value) || "true".equals(value);
    }

    private interface BeanInitMethodsInvoker {

        void invoke() throws Throwable;

        String getBeanName();
    }
}
