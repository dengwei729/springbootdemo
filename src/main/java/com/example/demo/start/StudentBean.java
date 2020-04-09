package com.example.demo.start;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class StudentBean implements InitializingBean, DisposableBean, BeanNameAware, BeanFactoryAware {
    private String name;
    private int age;

    private String beanName;//实现了BeanNameAware接口，Spring可以将BeanName注入该属性中
    private BeanFactory beanFactory;//实现了BeanFactory接口，Spring可将BeanFactory注入该属性中

    public StudentBean(){
        log.info("【构造器】调用学生类的构造器实例化");
    }

    @Override
    public String toString() {
        return "StudentBean{" +
            "name='" + name + '\'' +
            ", age=" + age +
            ", beanName='" + beanName + '\'' +
            '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        log.info("【注入属性】注入学生的name属性：" + name );
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        log.info("【注入属性】注入学生的age属性:" + age);
        this.age = age;
    }

    /**
     * BeanFactoryAware接口的方法
     * @param beanFactory
     * @throws BeansException
     */
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
        log.info("【BeanFactoryAware接口】调用BeanFactoryAware的setBeanFactory方法得到beanFactory引用");
    }

    /**
     * BeanNameAware接口的方法
     * @param name
     */
    @Override
    public void setBeanName(String name) {
        this.beanName = name;
        log.info("【BeanNameAware接口】调用BeanNameAware的setBeanName方法得到Bean的名称:" + name);
    }

    /**
     * InitializingBean接口的方法
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("【InitializingBean接口】调用InitializingBean接口的afterPropertiesSet方法");
    }

    /**
     * DisposableBean接口的方法
     * @throws Exception
     */
    @Override
    public void destroy() throws Exception {
        log.info("【DisposableBean接口】调用DisposableBean接口的destroy方法");
    }

    /**
     * 自己编写的初始化方法
     */
    public void myInit(){
        log.info("【init-method】调用init-method属性配置的初始化方法");
    }

    /**
     * 自己编写的销毁方法
     */
    public void myDestroy(){
        log.info("【destroy-method】调用destroy-method属性配置的销毁方法");
    }
}
