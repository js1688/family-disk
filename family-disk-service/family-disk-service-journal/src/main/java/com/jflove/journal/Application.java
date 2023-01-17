package com.jflove.journal;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.CountDownLatch;

/**
 * @author tanjun
 * @date 2022/01/17 9:21:30
 * @describe
 */
@EnableDubbo
@SpringBootApplication
public class Application {
    public static void main(String[] args) throws InterruptedException{
        SpringApplication.run(Application.class, args);
        new CountDownLatch(1).await();
    }
}

