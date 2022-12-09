package com.jflove.test;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

/**
 * @author tanjun
 * @date 2022/12/9 17:16
 * @describe
 */
@SpringBootTest(classes = Test.class)
@Log4j2
public class FileTest {

    @Test
    void a(){
        File win = new File("C:\\");

        if (win.exists()) {

            long total = win.getTotalSpace();//返回磁盘大小字节
            log.info("磁盘总大小:{}",total /1024/1024/1024);//单位GB
            long usableSpace = win.getUsableSpace();//返回可用字节
            log.info("磁盘可用大小:{}",usableSpace /1024/1024/1024);//单位GB
        }
    }
}
