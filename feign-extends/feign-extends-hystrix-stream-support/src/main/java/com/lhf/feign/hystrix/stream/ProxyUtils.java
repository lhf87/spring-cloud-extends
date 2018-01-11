package com.lhf.feign.hystrix.stream;

import feign.Target;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * Created on 2018/1/8.
 */
public class ProxyUtils {

    // 获取feign的Target
    public static Target getFeignTarget(Object instance) {
        if(Proxy.isProxyClass(instance.getClass())) {
            try {
                Field handlerField = ReflectionUtils.findField(instance.getClass().getSuperclass(), "h");
                handlerField.setAccessible(true);
                InvocationHandler feignInvocationHandler = (InvocationHandler)handlerField.get(instance);

                Field targetField = ReflectionUtils.findField(feignInvocationHandler.getClass(), "target");
                targetField.setAccessible(true);
                Target target = (Target)targetField.get(feignInvocationHandler);

                return target;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
