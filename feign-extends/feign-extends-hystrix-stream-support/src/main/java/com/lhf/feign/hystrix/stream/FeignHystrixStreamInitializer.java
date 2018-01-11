package com.lhf.feign.hystrix.stream;

import feign.Target;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.integration.handler.AbstractReplyProducingMessageHandler;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.core.DestinationResolver;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;
import org.springframework.messaging.handler.invocation.InvocableHandlerMethod;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * Created on 2018/1/6.
 */

public class FeignHystrixStreamInitializer implements ApplicationContextAware, InitializingBean {

    private String serviceName;

    private MessageHandlerMethodFactory messageHandlerMethodFactory;

    private DestinationResolver<MessageChannel> binderAwareChannelResolver;

    private FeignHystrixStreamChannelFactory channelFactory;

    private FallbackMessageResolver messageResolver;

    private ApplicationContext context;

    public FeignHystrixStreamInitializer(DestinationResolver<MessageChannel> binderAwareChannelResolver,
                                         MessageHandlerMethodFactory messageHandlerMethodFactory,
                                         FeignHystrixStreamChannelFactory channelFactory,
                                         FallbackMessageResolver messageResolver,
                                         String serviceName) {
        Assert.notNull(binderAwareChannelResolver, "Destination resolver cannot be null");
        Assert.notNull(messageHandlerMethodFactory, "Message handler method factory cannot be null");
        Assert.hasText(serviceName, "ServiceName cannot be empty");
        this.binderAwareChannelResolver = binderAwareChannelResolver;
        this.messageHandlerMethodFactory = messageHandlerMethodFactory;
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
        Method messageHandle = ReflectionUtils.findMethod(
            FallbackMessageResolver.class
            , "handleMessage"
            , FallbackMessageResolver.FallbackMessage.class);

        final InvocableHandlerMethod invocableHandlerMethod = messageHandlerMethodFactory
                .createInvocableHandlerMethod(messageResolver, messageHandle);

        SubscribableChannel channel =
                (SubscribableChannel)channelFactory.getInputChannel(serviceName);

        StreamListenerMessageHandler handler = new StreamListenerMessageHandler(invocableHandlerMethod);
        handler.setApplicationContext(context);
        handler.setChannelResolver(binderAwareChannelResolver);
        handler.afterPropertiesSet();
        channel.subscribe(handler);
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

    private final class StreamListenerMessageHandler extends AbstractReplyProducingMessageHandler {

        private final InvocableHandlerMethod invocableHandlerMethod;

        private StreamListenerMessageHandler(InvocableHandlerMethod invocableHandlerMethod) {
            this.invocableHandlerMethod = invocableHandlerMethod;
        }

        @Override
        protected boolean shouldCopyRequestHeaders() {
            return false;
        }

        @Override
        protected Object handleRequestMessage(Message<?> requestMessage) {
            try {
                return this.invocableHandlerMethod.invoke(requestMessage);
            }
            catch (Exception e) {
                if (e instanceof MessagingException) {
                    throw (MessagingException) e;
                }
                else {
                    throw new MessagingException(requestMessage,
                            "Exception thrown while invoking " + this.invocableHandlerMethod.getShortLogMessage(), e);
                }
            }
        }
    }
}
