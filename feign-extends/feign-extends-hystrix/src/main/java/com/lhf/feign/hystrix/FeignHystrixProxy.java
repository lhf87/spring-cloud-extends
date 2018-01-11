package com.lhf.feign.hystrix;

import com.lhf.feign.hystrix.template.HystrixFallbackTemplate;

import java.lang.annotation.*;

/**
 * Created on 2017/12/22.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface FeignHystrixProxy {

    Class<?> template() default HystrixFallbackTemplate.Default.class;
}
