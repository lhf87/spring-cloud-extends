package com.lhf.feign.hystrix;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Created on 2017/12/22.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({FeignHystrixProxyRegistrar.class, FeignHystrixProxyConfiguration.class})
public @interface EnableFeignHystrixProxy {

    String[] value() default {};
}
