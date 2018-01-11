package com.lhf.feign.hystrix.stream;

import com.lhf.feign.hystrix.stream.handler.HandleDetail;
import org.springframework.context.ApplicationContext;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.*;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;

/**
 * Created on 2018/1/9.
 */
public class MessageConvertUtils {

    public static HandleDetail convert(FallbackMessageResolver.FallbackMessage message, Object context) throws MessageConvertException {
        if(!(context instanceof ApplicationContext)) {
            // 如果不是spring上下文也可以转换，现在暂时不实现
            return null;
        }

        FallbackMessageResolver.FallbackMessage.RequestMapping requestMapping = message.getRequestMapping();

        RequestMappingInfo.Builder builder = new RequestMappingBuilder();
        builder.mappingName(requestMapping.getName())
                .paths(requestMapping.getPath())
                .headers(requestMapping.getHeaders())
                .methods(Arrays.stream(requestMapping.getMethod()).map(requestMethod -> RequestMethod.valueOf(requestMethod)).toArray(RequestMethod[]::new))
                .consumes(requestMapping.getConsumes())
                .produces(requestMapping.getProduces())
                .params(requestMapping.getParams());

        RequestMappingInfo mappingInfo = builder.build();

        HandlerMethod handlerMethod = ((ApplicationContext) context).getBean(RequestMappingHandlerMapping.class)
                .getHandlerMethods().get(mappingInfo);

        HandleDetail detail = new HandleDetail();
        detail.setInstance(((ApplicationContext) context).getBean(handlerMethod.getBeanType()));
        detail.setMethod(handlerMethod.getMethod());

        Object[] args = new Object[message.getArgsTypes().length];
        for (int i = 0; i < message.getArgsTypes().length; i++) {
            Class typeClass = null;
            try {
                typeClass = Class.forName(message.getArgsTypes()[i]
                    , false, MessageConvertUtils.class.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw new MessageConvertException("not found class", e, message);
            }
            if(Map.class.isAssignableFrom(message.getArgs()[i].getClass()) &&
                    null != typeClass && !Map.class.isAssignableFrom(typeClass)) {
                try {
                    args[i] = convert(typeClass, (Map) message.getArgs()[i]);
                } catch (Exception e) {
                    throw new MessageConvertException("convert error", e, message);
                }
            } else {
                args[i] = message.getArgs()[i];
            }
        }
        detail.setArgs(args);

        return detail;
    }

    @SuppressWarnings("rawtypes")
    private static Object convert(Class type, Map map)
            throws IntrospectionException, IllegalAccessException, InstantiationException, InvocationTargetException {
        BeanInfo beanInfo = Introspector.getBeanInfo(type);
        Object instance = type.newInstance();

        PropertyDescriptor[] propertyDescriptors =  beanInfo.getPropertyDescriptors();
        for (int i = 0; i < propertyDescriptors.length; i++) {
            PropertyDescriptor descriptor = propertyDescriptors[i];
            String propertyName = descriptor.getName();

            if (map.containsKey(propertyName)) {
                Object value = map.get(propertyName);
                // 属性是对象的递归转换
                if(value instanceof Map) {
                    descriptor.getWriteMethod().invoke(instance, convert(descriptor.getPropertyType(), (Map)value));
                } else {
                    descriptor.getWriteMethod().invoke(instance, value);
                }
            }
        }
        return instance;
    }

    private static class RequestMappingBuilder implements RequestMappingInfo.Builder {

        private String[] paths;

        private RequestMethod[] methods;

        private String[] params;

        private String[] headers;

        private String[] consumes;

        private String[] produces;

        private String name;

        private RequestCondition<?> customCondition;

        private RequestMappingInfo.BuilderConfiguration options = new RequestMappingInfo.BuilderConfiguration();

        public RequestMappingBuilder(String... paths) {
            this.paths = paths;
        }

        @Override
        public RequestMappingInfo.Builder paths(String... paths) {
            this.paths = paths;
            return this;
        }

        @Override
        public RequestMappingBuilder methods(RequestMethod... methods) {
            this.methods = methods;
            return this;
        }

        @Override
        public RequestMappingBuilder params(String... params) {
            this.params = params;
            return this;
        }

        @Override
        public RequestMappingBuilder headers(String... headers) {
            this.headers = headers;
            return this;
        }

        @Override
        public RequestMappingBuilder consumes(String... consumes) {
            this.consumes = consumes;
            return this;
        }

        @Override
        public RequestMappingBuilder produces(String... produces) {
            this.produces = produces;
            return this;
        }

        @Override
        public RequestMappingBuilder mappingName(String name) {
            this.name = name;
            return this;
        }

        @Override
        public RequestMappingBuilder customCondition(RequestCondition<?> condition) {
            this.customCondition = condition;
            return this;
        }

        @Override
        public RequestMappingInfo.Builder options(RequestMappingInfo.BuilderConfiguration options) {
            this.options = options;
            return this;
        }

        @Override
        public RequestMappingInfo build() {
            ContentNegotiationManager manager = this.options.getContentNegotiationManager();

            PatternsRequestCondition patternsCondition = new PatternsRequestCondition(
                    this.paths, this.options.getUrlPathHelper(), this.options.getPathMatcher(),
                    this.options.useSuffixPatternMatch(), this.options.useTrailingSlashMatch(),
                    this.options.getFileExtensions());

            return new RequestMappingInfo(this.name, patternsCondition,
                    new RequestMethodsRequestCondition(methods),
                    new ParamsRequestCondition(this.params),
                    new HeadersRequestCondition(this.headers),
                    new ConsumesRequestCondition(this.consumes, this.headers),
                    new ProducesRequestCondition(this.produces, this.headers, manager),
                    this.customCondition);
        }
    }
}
