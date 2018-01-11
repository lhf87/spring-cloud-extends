package com.lhf.test.feign.app1.client;

import com.lhf.test.feign.app1.client.fallback.FeignClientFallback;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created on 2017/12/25.
 */

@FeignClient(value = "no-server", fallback = FeignClientFallback.class)
//@RequestMapping("/base-url")   //不能有这个 不然会报错, 应该在实现类去加这个注解
public interface FeignClientApi {

    @RequestMapping(path = "/testVoid", method = RequestMethod.GET)
    void testVoid();

    @RequestMapping(path = "/testString", method = RequestMethod.GET)
    String testString();

    @RequestMapping(path = "/testArgs", method = RequestMethod.GET)
    int testArgs(String name);

    @RequestMapping(path = "/testException", method = RequestMethod.GET)
    int testException(String name);
}
