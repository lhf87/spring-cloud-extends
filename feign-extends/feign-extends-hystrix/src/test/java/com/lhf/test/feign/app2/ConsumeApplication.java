package com.lhf.test.feign.app2;

import com.lhf.feign.hystrix.EnableFeignHystrixProxy;
import com.lhf.test.feign.common.YmlApplicationContextInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.feign.EnableFeignClients;

/**
 * Created on 2017/9/3.
 */
@SpringBootApplication
@EnableFeignClients("com.lhf.test.feign.app2.client")
@EnableFeignHystrixProxy("com.lhf.test.feign.app2.client.fallback")
public class ConsumeApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ConsumeApplication.class);
        app.addInitializers(new YmlApplicationContextInitializer("consumer-application"));
        app.run(args);
        //SpringApplication.run(ConsumeApplication.class, args);
    }
}
