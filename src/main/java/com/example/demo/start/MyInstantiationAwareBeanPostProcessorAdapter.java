package com.example.demo.start;

import java.beans.PropertyDescriptor;

import com.example.demo.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MyInstantiationAwareBeanPostProcessorAdapter extends InstantiationAwareBeanPostProcessorAdapter {
    public MyInstantiationAwareBeanPostProcessorAdapter() {
        super();
        log.info("这是InstantiationAwareBeanPostProcessorAdapter实现类构造器!");
    }

    /**
     * 接口方法、实例化Bean之前调用
     */
    @Override
    public Object postProcessBeforeInstantiation(Class beanClass, String beanName) throws BeansException {
        if (beanName.equals(Constants.beanName)) {
            log.info("InstantiationAwareBeanPostProcessor调用postProcessBeforeInstantiation方法:" + beanName);
        }
        return null;
    }

    /**
     * 接口方法、实例化Bean之后调用
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)
        throws BeansException {
        if (beanName.equals(Constants.beanName)) {
            log.info("InstantiationAwareBeanPostProcessor调用postProcessAfterInitialization方法:" + beanName);
        }
        return bean;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (beanName.equals(Constants.beanName)) {
            log.info("InstantiationAwareBeanPostProcessor调用postProcessBeforeInitialization方法:" + beanName);
        }
        return super.postProcessBeforeInitialization(bean, beanName);
    }

    @Override
    public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
        if (beanName.equals(Constants.beanName)) {
            log.info("InstantiationAwareBeanPostProcessor调用postProcessAfterInstantiation方法:" + beanName);
        }
        return super.postProcessAfterInstantiation(bean, beanName);
    }

    @Override
    public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName)
        throws BeansException {
        if (beanName.equals(Constants.beanName)) {
            log.info("InstantiationAwareBeanPostProcessor调用postProcessProperties方法:" + beanName);
        }
        return super.postProcessProperties(pvs, bean, beanName);
    }
}
