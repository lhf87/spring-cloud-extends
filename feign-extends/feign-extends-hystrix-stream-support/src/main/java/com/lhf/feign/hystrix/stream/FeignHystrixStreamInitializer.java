package com.lhf.feign.hystrix.stream;

import feign.Target;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cloud.stream.converter.CompositeMessageConverterFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.messaging.Message;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.util.Assert;

/**
 * Created on 2018/1/6.
 */

public class FeignHystrixStreamInitializer implements ApplicationContextAware, InitializingBean {

    private String serviceName;

    private CompositeMessageConverterFactory converterFactory;

    private FeignHystrixStreamChannelFactory channelFactory;

    private FallbackMessageResolver messageResolver;

    private ApplicationContext context;

    public FeignHystrixStreamInitializer(CompositeMessageConverterFactory converterFactory,
                                         FeignHystrixStreamChannelFactory channelFactory,
                                         FallbackMessageResolver messageResolver,
                                         String serviceName) {
        Assert.notNull(converterFactory, "converter factory cannot be null");
        Assert.notNull(channelFactory, "channel factory cannot be null");
        Assert.notNull(messageResolver, "messageResolver cannot be null");
        Assert.hasText(serviceName, "ServiceName cannot be empty");
        this.converterFactory = converterFactory;
        this.channelFactory = channelFactory;
        this.messageResolver = messageResolver;
        this.serviceName = serviceName;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 监听当前服务的fallback channel
        subscribeChannelMessage();

        // 初始化其他服务的fallback channel
        // 用到时再初始化
        /*Arrays.stream(context.getBeanNamesForAnnotation(FeignClient.class)).forEach(beanName -> {
            String sName = checkServiceName(beanName);
            if(!serviceName.equals(sName)) {
                channelFactory.getOutputChannel(sName);
            }
        });*/
    }

    private void subscribeChannelMessage() {
        SubscribableChannel channel = (SubscribableChannel)channelFactory.getInputChannel(serviceName);

        StreamListenerMessageHandler listenerMessageHandler =
                new StreamListenerMessageHandler(converterFactory, messageResolver);

        channel.subscribe(listenerMessageHandler);
    }

    private String checkServiceName(String name) {
        /**
         * controller继承自FeignClient的是cglib代理， hystrix的fallback继承自FeignClient的是jdk代理
         * {@link }
         */
        Object beanInstance = context.getBean(name);
        Target target = ProxyUtils.getFeignTarget(beanInstance);
        if(null != target) {
            return target.name();
        }

        return serviceName;
    }

    private final class StreamListenerMessageHandler extends AbstractMessageHandler {

        private CompositeMessageConverterFactory converterFactory;

        private FallbackMessageResolver messageResolver;

        public StreamListenerMessageHandler(CompositeMessageConverterFactory converterFactory,
                                            FallbackMessageResolver messageResolver) {
            this.converterFactory = converterFactory;
            this.messageResolver = messageResolver;
        }

        @Override
        protected void handleMessageInternal(Message<?> message) throws Exception {
            FallbackMessageResolver.FallbackMessage fallbackMessage =
                    (FallbackMessageResolver.FallbackMessage) converterFactory.getMessageConverterForAllRegistered()
                            .fromMessage(message, FallbackMessageResolver.FallbackMessage.class);

            if(null != fallbackMessage) {
                messageResolver.handleMessage(fallbackMessage);
            } else {
                throw new MessageConversionException(message, "Cannot convert from [" +
                        message.getPayload().getClass().getName() + "] to [" +
                        FallbackMessageResolver.FallbackMessage.class.getName() + "] for " + message);
            }
        }
    }
}
