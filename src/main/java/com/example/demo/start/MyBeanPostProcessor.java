package com.example.demo.start;

import java.util.HashMap;
import java.util.Map;

import com.example.demo.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * @author dengwei
 */
@Component
@Slf4j
public class MyBeanPostProcessor implements BeanPostProcessor {

    private static Map<String, Long> costTimeMap  = new HashMap<>();
    private static Map<String, Long> startTimeMap = new HashMap<>();

    public MyBeanPostProcessor(){
        log.info("【BeanPostProcessor接口】调用BeanPostProcessor的构造方法");
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (beanName.equals(Constants.beanName)) {
            log.info("【BeanPostProcessor接口】调用postProcessBeforeInitialization方法，这里可对"+beanName+"的属性进行更改。");
        }
        startTimeMap.put(beanName, System.currentTimeMillis());
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (beanName.equals(Constants.beanName)) {
            log.info("【BeanPostProcessor接口】调用postProcessAfterInitialization方法，这里可对"+beanName+"的属性进行更改。");
        }

        Long start = startTimeMap.get(beanName);
        if (start != null) {
            costTimeMap.put(beanName, System.currentTimeMillis() - start);
        }
        return bean;
    }

    static void showBeanInitializationCost() {
        costTimeMap.entrySet().stream()
            .sorted(Map.Entry.comparingByValue())
            .forEachOrdered(entry -> log.error(entry.getKey() + " initialize cost=" + entry.getValue() + "ms"));
    }
}
