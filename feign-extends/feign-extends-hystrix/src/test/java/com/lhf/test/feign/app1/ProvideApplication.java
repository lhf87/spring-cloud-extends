package com.lhf.test.feign.app1;

import com.lhf.feign.hystrix.EnableFeignHystrixProxy;
import com.lhf.test.feign.common.YmlApplicationContextInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.feign.EnableFeignClients;

/**
 * Created on 2017/9/3.
 */
@SpringBootApplication
@EnableFeignClients("com.lhf.test.feign.app1.client")
@EnableFeignHystrixProxy("com.lhf.test.feign.app1.client.fallback")
public class ProvideApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ProvideApplication.class);
        app.addInitializers(new YmlApplicationContextInitializer("provider-application"));
        app.run(args);
        //SpringApplication.run(ProvideApplication.class, args);
    }
}
