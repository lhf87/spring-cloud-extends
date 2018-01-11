package com.lhf.feign.hystrix.template;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created on 2017/12/22.
 */

public interface HystrixFallbackTemplate {

    Object fallback(Object instance, Method method, Object[] args);

    abstract class AbstractTemplate implements HystrixFallbackTemplate {
        protected Object return0(Class<?> returnType) {
            if(returnType.isPrimitive()) {
                return PRIMITIVES_DEFAULT.get(returnType);
            }
            return null;
        }
    }

    /**
     * 默认模板：打印warn日志
     */
    class Default extends AbstractTemplate {

        private static final Logger logger = LoggerFactory.getLogger(Default.class);

        @Override
        public Object fallback(Object instance, Method method, Object[] args) {
            logger.warn(
                "method fallback: {}.{}, args: {}"
                , method.getDeclaringClass()
                , method.getName()
                , Arrays.stream(args).collect(Collectors.toList())
            );

            return return0(method.getReturnType());
        }
    }

    // constant
    Map<Class<?>, Object> PRIMITIVES_DEFAULT = ImmutableMap.<Class<?>, Object>builder()
            .put(boolean.class, false)
            .put(char.class, '\u0000')
            .put(byte.class, (byte) 0)
            .put(short.class, (short) 0)
            .put(int.class, 0)
            .put(long.class, 0L)
            .put(float.class, 0F)
            .put(double.class, 0D)
            .build();
}
