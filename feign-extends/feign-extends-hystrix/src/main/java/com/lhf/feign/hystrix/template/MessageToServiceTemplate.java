package com.lhf.feign.hystrix.template;

import com.lhf.feign.hystrix.stream.FallbackMessageProcessor;
import com.lhf.feign.hystrix.stream.FeignHystrixStreamChannelFactory;
import com.lhf.feign.hystrix.stream.ProxyUtils;
import feign.Target;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.MessageChannel;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Created on 2018/1/8.
 * 通过消息将失败内容发送出去
 */

public class MessageToServiceTemplate extends HystrixFallbackTemplate.AbstractTemplate {

    private static final Logger logger = LoggerFactory.getLogger(MessageToServiceTemplate.class);

    private FallbackMessageProcessor messageProcessor;

    private FeignHystrixStreamChannelFactory channelFactory;

    public MessageToServiceTemplate(FallbackMessageProcessor messageProcessor,
                                    FeignHystrixStreamChannelFactory channelFactory) {
        this.messageProcessor = messageProcessor;
        this.channelFactory = channelFactory;
    }

    @Override
    public Object fallback(Object instance, Method method, Object[] args) {
        FallbackMessageProcessor.FallbackMessage message = new FallbackMessageProcessor.FallbackMessage();
        message.setArgs(args);
        message.setArgsTypes(Arrays.stream(args).map(arg -> arg.getClass().getName()).toArray(String[]::new));
        message.setTimestamp(System.currentTimeMillis());

        RequestMapping rmAnnotation = method.getAnnotation(RequestMapping.class);
        if(null == rmAnnotation) {
            throw new RuntimeException("feign method must have RequestMapping Annotation");
        }

        FallbackMessageProcessor.FallbackMessage.RequestMapping requestMapping =
            new FallbackMessageProcessor.FallbackMessage.RequestMapping()
                .setName(rmAnnotation.name())
                .setPath(rmAnnotation.path())
                .setHeaders(rmAnnotation.headers())
                .setMethod(Arrays.stream(rmAnnotation.method()).map(requestMethod -> requestMethod.name()).toArray(String[]::new))
                .setConsumes(rmAnnotation.consumes())
                .setProduces(rmAnnotation.produces())
                .setParams(rmAnnotation.params());
        message.setRequestMapping(requestMapping);

        Target target = ProxyUtils.getFeignTarget(instance);
        if(null != target) {
            MessageChannel channel = channelFactory.getOutputChannel(target.name());
            if(null == channel) {
                throw new RuntimeException("not found channel for target " + target.name());
            }
            messageProcessor.sendMessage(channel, message);
        }

        logger.info("method fallback , msg to service: {}", message);

        return return0(method.getReturnType());
    }
}
