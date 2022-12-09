package com.jflove.email;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.CountDownLatch;

/**
 * @author tanjun
 * @date 2022/12/7 11:24
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

