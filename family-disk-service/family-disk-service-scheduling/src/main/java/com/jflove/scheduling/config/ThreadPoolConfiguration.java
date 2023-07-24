package com.jflove.scheduling.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import static org.springframework.scheduling.annotation.AsyncAnnotationBeanPostProcessor.DEFAULT_TASK_EXECUTOR_BEAN_NAME;

/**
 * @author: tanjun
 * @date: 2023/6/9 3:11 PM
 * @desc: @Async 注解异步执行线程池实现类
 */
@Configuration
@Slf4j
public class ThreadPoolConfiguration {

    /**
     * 工作线程池最大数量
     */
    @Value("${spring.async.maxPoolSize:50}")
    private int maxPoolSize;

    /**
     * 默认线程池
     * 通常用于执行一般的任务
     * @return
     */
    @Bean(name = DEFAULT_TASK_EXECUTOR_BEAN_NAME)
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //核心线程池大小
        executor.setCorePoolSize(10);
        //最大线程数
        executor.setMaxPoolSize(maxPoolSize);
        log.info("设置最大工作线程:{}",executor.getMaxPoolSize());
        //队列容量
        executor.setQueueCapacity(2000);
        //空闲时间
        executor.setKeepAliveSeconds(60);
        //线程名字前缀
        executor.setThreadNamePrefix(String.format("%s-",DEFAULT_TASK_EXECUTOR_BEAN_NAME));
        /**
         * 拒绝处理策略
         * CallerRunsPolicy()：交由调用方线程运行，比如 main 线程。
         * AbortPolicy()：直接抛出异常。
         * DiscardPolicy()：直接丢弃。
         * DiscardOldestPolicy()：丢弃队列中最老的任务。
         */
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        return executor;
    }
}
