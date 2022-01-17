package com.nowcoder.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class LoggerTest {
    private static final Logger logger = LoggerFactory.getLogger(LoggerTest.class);
    @Test
    public void testLogger1(){
        System.out.println(logger.getName());
        logger.trace("hello trace");
        //程序调试日志
        logger.debug("hello debug");
        //普通级别日志
        logger.info("hello info");
        logger.warn("hello warn");
        //错误日志
        logger.error("hello log");
    }
}
