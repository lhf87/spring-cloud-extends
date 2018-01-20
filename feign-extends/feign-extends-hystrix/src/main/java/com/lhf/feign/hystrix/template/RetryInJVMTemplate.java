package com.lhf.feign.hystrix.template;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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

    // 初始延迟重试间隔
    private static final int DELAYSECOND = 30;

    // 最大尝试次数
    private static final int TRYMAXCOUNT = 30;

    private Map<RetryInfo, RetryStatistics> retry = new ConcurrentHashMap<>();

    @Override
    public Object fallback(Object instance, Method method, Object[] args) {
        RetryInfo retryInfo = new RetryInfo()
                .setClassName(method.getDeclaringClass().getName())
                .setMethodName(method.getName())
                .setArgs(args);

        RetryStatistics statistics = retry.get(retryInfo);
        if(null == statistics) {
            statistics = new RetryStatistics(DELAYSECOND ,0);
            retry.putIfAbsent(retryInfo, statistics);
        } else if (statistics.getCount() > TRYMAXCOUNT){
            retry.remove(retryInfo);
            logger.error(
                "method retry max count: {}.{}, args: {}"
                , method.getDeclaringClass()
                , method.getName()
                , Arrays.stream(args).collect(Collectors.toList())
            );
            return return0(method.getReturnType());
        }

        executor.schedule(() -> {
            try {
                // 只保证调用成功，不保证业务是否成功
                // 比如调用成功，但是服务自身处理了业务错误，给了返回(可能是失败的信息提示，不表示业务逻辑正常)
                method.invoke(instance, args);
                retry.remove(retryInfo);
            } catch (Exception e) {
                // 执行失败后会再次进入到fallback(重试)
                RetryStatistics rs = retry.get(retryInfo);
                rs.setDelay(rs.getDelay() + DELAYSECOND);
                rs.setCount(rs.getCount() + 1);
                retry.putIfAbsent(retryInfo, rs);
            }
        }, statistics.getDelay(), TimeUnit.SECONDS);

        logger.info(
            "method fallback retry {}s later: {}.{}, args: {}"
            , statistics.getDelay()
            , method.getDeclaringClass()
            , method.getName()
            , Arrays.stream(args).collect(Collectors.toList())
        );

        return return0(method.getReturnType());
    }

    class RetryInfo {
        private String className;

        private String methodName;

        private Object[] args;

        public String getClassName() {
            return className;
        }

        public RetryInfo setClassName(String className) {
            this.className = className;
            return this;
        }

        public String getMethodName() {
            return methodName;
        }

        public RetryInfo setMethodName(String methodName) {
            this.methodName = methodName;
            return this;
        }

        public Object[] getArgs() {
            return args;
        }

        public RetryInfo setArgs(Object[] args) {
            this.args = args;
            return this;
        }
    }

    class RetryStatistics {
        private int delay;

        private int count;

        public RetryStatistics(int delay, int count) {
            this.delay = delay;
            this.count = count;
        }

        public int getDelay() {
            return delay;
        }

        public RetryStatistics setDelay(int delay) {
            this.delay = delay;
            return this;
        }

        public int getCount() {
            return count;
        }

        public RetryStatistics setCount(int count) {
            this.count = count;
            return this;
        }
    }
}
