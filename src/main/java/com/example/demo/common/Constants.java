package com.example.demo.common;

/**
 * @author dengwei
 */
public class Constants {
    public final static String beanName = "studentBean";

    public static final String ASYNC_INIT = "asyncInit";

    public static final String ASYNC_INIT_POOL_SIZE = "asyncInitPoolSize";

    public static final String IS_USE_ASYNC_INIT_WEB_CONTEXT = "isUseAsyncInitWebContext";

    /**
     * 是否自动设置线程池大小
     */
    public static final String IS_AUTO_SET_POOL_SIZE = "isAutoSetPoolSize";

    /**
     * 默认为当前机器Cpu 核数
     */
    public static final int DEFAULT_POOL_SIZE = 3;
}
