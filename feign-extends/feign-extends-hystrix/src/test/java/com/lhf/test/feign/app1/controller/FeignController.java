package com.lhf.test.feign.app1.controller;


import com.lhf.test.feign.app1.client.FeignClientApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created on 2017/12/25.
 */

@RestController
public class FeignController {

    @Autowired
    FeignClientApi feignClientApi;

    @RequestMapping("/hello-world")
    public void test() {
        feignClientApi.testVoid();

        String str = feignClientApi.testString();
        System.out.println(str);

        Integer i = feignClientApi.testArgs("wtf");
        // 基本类型的fallback 暂时是返回默认值 比如 int就是0
        System.out.println(i);

        /**
         * 对于fallback方法执行产生的异常 最终会按没有fallback的情况抛出异常
         * {@link feign.hystrix.HystrixInvocationHandler#invoke --> getFallback --> InvocationTargetException}
          */
        //feignClientApi.testException("wtf-exception");
    }
}
