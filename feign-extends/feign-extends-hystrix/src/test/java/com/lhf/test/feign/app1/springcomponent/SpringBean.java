package com.lhf.test.feign.app1.springcomponent;

import org.springframework.stereotype.Component;

/**
 * Created on 2017/12/26.
 */

@Component
public class SpringBean {

    public String str() {
        return "hello-world";
    }

    public void print(String str) {
        System.out.println("我是一个依赖对象的print方法:" + str);
    }
}
