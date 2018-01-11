package com.lhf.test.feign.app1.controller;


import com.lhf.test.feign.app1.client.ConsumeClientApi;
import com.lhf.test.feign.common.TestBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created on 2018/1/10.
 */

@RestController
public class ProvideController {

    @Autowired
    ConsumeClientApi consumeClientApi;

    @RequestMapping("/hello-stream")
    public String test() {
        TestBean testBean = new TestBean();
        testBean.setId(1111);
        testBean.setName("马蓉");
        testBean.setSex(true);
        String result = consumeClientApi.hiobjStream(testBean);

        return result;
    }
}
