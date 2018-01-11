package com.lhf.test.feign.app2.controller;

import com.lhf.test.feign.common.TestBean;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created on 2017/12/25.
 */

@RestController
public class ConsumeController {

    @RequestMapping(value = "/hi-obj", method = RequestMethod.POST)
    public String hiobj(@RequestBody TestBean bean) {
        System.out.println(bean);

        return bean.toString();
    }
}
