package com.lhf.test.feign.app1.client.fallback;

import com.lhf.feign.hystrix.FeignHystrixProxy;
import com.lhf.feign.hystrix.template.HystrixFallbackTemplate;
import com.lhf.test.feign.app1.client.FeignClientApi;
import com.lhf.test.feign.app1.springcomponent.SpringBean;
import com.lhf.test.feign.app1.springcomponent.SpringBeanByName;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;

/**
 * Created on 2017/12/25.
 */

@FeignHystrixProxy(template = HystrixFallbackTemplate.Default.class)
public abstract class FeignClientFallback implements FeignClientApi {

    @Autowired
    SpringBean springBean;

    @Resource(name = "springbean")
    SpringBeanByName springBeanByName;

    @Override
    public String testString() {
        String outStr = springBean.str();
        springBean.print(springBeanByName.str());
        return "fall back 了啊 -- " + outStr;
    }

    @Override
    public int testException(String name) {
        throw new RuntimeException("异常测试");
    }
}
