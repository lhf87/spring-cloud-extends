package com.lhf.feign.hystrix;

import com.lhf.feign.hystrix.template.HystrixFallbackTemplate;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created on 2017/12/22.
 */
public class FeignHystrixProxyFactoryBean implements FactoryBean<Object>
        , InitializingBean, ApplicationContextAware {

    // 被代理的虚类fallback
    private Class<?> type;

    // 未具体实现的方法的通用调用模板
    private Class<?> fallbackTemplate;

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public Class<?> getFallbackTemplate() {
        return fallbackTemplate;
    }

    public void setFallbackTemplate(Class<?> fallbackTemplate) {
        this.fallbackTemplate = fallbackTemplate;
    }

    private ApplicationContext context;

    private List<Class> superInterfaces;

    private List<Method> interfacesMethods = new ArrayList<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(type, "type must not be null");
        Assert.isAssignable(HystrixFallbackTemplate.class, fallbackTemplate
            , "fallbackTemplate must be a sub class of HystrixFallbackTemplate");

        superInterfaces = Arrays.stream(type.getInterfaces()).collect(Collectors.toList());
        superInterfaces.stream().forEach(clazz -> {
            interfacesMethods.addAll(Arrays.stream(clazz.getDeclaredMethods()).collect(Collectors.toList()));
        });
    }

    @Override
    public Object getObject() throws Exception {
        ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(type);
        factory.setFilter(method -> interfacesMethods.contains(method));

        Proxy proxy = (Proxy)factory.createClass().newInstance();

        // 依赖注入
        Arrays.stream(type.getDeclaredFields())
            .filter(field -> field.getDeclaredAnnotations().length > 0)
                .forEach(field -> {
                    boolean inject = false;
                    Object beanInstance = null;

                    if(field.isAnnotationPresent(Autowired.class)) {
                        inject = true;
                        beanInstance = context.getBean(field.getType());
                    } else if(field.isAnnotationPresent(Resource.class)) {
                        inject = true;
                        Resource resourceAnnotation = field.getAnnotation(Resource.class);
                        if(StringUtils.isNotEmpty(resourceAnnotation.name())) {
                            beanInstance = context.getBean(resourceAnnotation.name());
                        } else {
                            beanInstance = context.getBean(resourceAnnotation.type());
                        }
                    } else if(field.isAnnotationPresent(Qualifier.class)) {
                        inject = true;
                        beanInstance = context.getBean(field.getAnnotation(Qualifier.class).value());
                    }

                    if(inject) {
                        try {
                            field.setAccessible(true);
                            field.set(proxy, beanInstance);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

        // invoke
        proxy.setHandler((obj, method, proceed, args) -> {
            if(method.getDeclaringClass().equals(type)) {
                return proceed.invoke(args);
            } else {
                HystrixFallbackTemplate template = ((HystrixFallbackTemplate)context.getBean(fallbackTemplate));
                for(Class superInterface : superInterfaces) {
                    try {
                        return template.fallback(context.getBean(superInterface), method, args);
                    } catch (Exception e) {
                        // do nothing
                    }
                }
            }

            throw new RuntimeException("fallback exception");
        });

        return proxy;
    }

    @Override
    public Class<?> getObjectType() {
        return this.type;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
