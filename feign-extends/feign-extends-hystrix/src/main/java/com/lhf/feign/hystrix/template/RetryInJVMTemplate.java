package com.lhf.feign.hystrix.template;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created on 2017/12/25.
 * 当前进程内重试，用于非重要接口调用的重试，不保证重试成功
 * 因为当前进程挂了，未成功的重试就不会继续执行了
 */

public class RetryInJVMTemplate extends HystrixFallbackTemplate.AbstractTemplate {

    private static final Logger logger = LoggerFactory.getLogger(RetryInJVMTemplate.class);

    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(200);

    @Override
    public Object fallback(Object instance, Method method, Object[] args) {
        executor.schedule(() -> {
            try {
                method.invoke(instance, args);
            } catch (Exception e) {
                // 执行失败后会再次重试
            }
        }, 30, TimeUnit.SECONDS);

        logger.info(
            "method fallback retry 30s later: {}.{}, args: {}"
            , method.getDeclaringClass()
            , method.getName()
            , Arrays.stream(args).collect(Collectors.toList())
        );

        return return0(method.getReturnType());
    }
}
