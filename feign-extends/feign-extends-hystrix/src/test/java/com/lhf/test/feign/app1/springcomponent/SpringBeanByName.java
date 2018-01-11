package com.lhf.test.feign.app1.springcomponent;

import org.springframework.stereotype.Component;

/**
 * Created on 2017/12/26.
 */

@Component("springbean")
public class SpringBeanByName {

    public String str() {
        return "大吉大利 今晚吃鸡";
    }
}
