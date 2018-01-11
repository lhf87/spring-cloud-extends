package com.lhf.test.feign.app1.client;


import com.lhf.test.feign.app1.client.fallback.ConsumeFallback;
import com.lhf.test.feign.common.TestBean;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created on 2018/1/10.
 */

@FeignClient(value = "no-server", fallback = ConsumeFallback.class)
public interface ConsumeClientApi {

    @RequestMapping(path = "/hi-obj", method = RequestMethod.POST)
    String hiobjStream(@RequestBody TestBean bean);
}
