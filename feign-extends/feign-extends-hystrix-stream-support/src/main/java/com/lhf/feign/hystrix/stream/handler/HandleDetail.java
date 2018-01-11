package com.lhf.feign.hystrix.stream.handler;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Created on 2018/1/9.
 */

public class HandleDetail {

    private Object instance;

    private Method method;

    private Object[] args;

    public Object getInstance() {
        return instance;
    }

    public void setInstance(Object instance) {
        this.instance = instance;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    @Override
    public String toString() {
        return "HandleDetail{" +
                "instance=" + instance +
                ", method=" + method +
                ", args=" + Arrays.toString(args) +
                '}';
    }
}
