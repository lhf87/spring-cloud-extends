package com.lhf.test.feign.app1.client.fallback;

import com.lhf.feign.hystrix.FeignHystrixProxy;
import com.lhf.feign.hystrix.template.MessageToServiceTemplate;
import com.lhf.test.feign.app1.client.ConsumeClientApi;

/**
 * Created on 2018/1/10.
 */

@FeignHystrixProxy(template = MessageToServiceTemplate.class)
public abstract class ConsumeFallback implements ConsumeClientApi {

}
