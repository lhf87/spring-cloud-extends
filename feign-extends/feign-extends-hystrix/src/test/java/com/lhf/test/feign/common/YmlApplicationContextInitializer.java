package com.lhf.test.feign.common;

import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;

import java.io.IOException;

/**
 * Created on 2018/1/10.
 */
public class YmlApplicationContextInitializer implements
        ApplicationContextInitializer<ConfigurableApplicationContext> {

    String ymlName;

    public YmlApplicationContextInitializer(String name) {
        this.ymlName = name;
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        Resource resource = applicationContext.getResource(
                "classpath:/" + ymlName + ".yml");
        YamlPropertySourceLoader yamlPropertySourceLoader = new YamlPropertySourceLoader();
        try {
            PropertySource<?> propertySource = yamlPropertySourceLoader
                    .load(ymlName, resource, null);
            applicationContext.getEnvironment().getPropertySources()
                    .addLast(propertySource);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
