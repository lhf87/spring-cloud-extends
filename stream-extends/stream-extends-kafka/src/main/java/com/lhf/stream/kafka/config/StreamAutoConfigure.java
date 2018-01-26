package com.lhf.stream.kafka.config;

import com.lhf.stream.kafka.MessageResolver;
import com.lhf.stream.kafka.ProducerListenerBase;
import com.lhf.stream.kafka.codec.IntegrationCodecProxy;
import com.lhf.stream.kafka.delegate.LogSucessListener;
import com.lhf.stream.kafka.delegate.ProducerListenerDelegate;
import com.lhf.stream.kafka.enhance.ChannelBindingFactoryBeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.codec.Codec;
import org.springframework.kafka.support.ProducerListener;

import java.util.Collection;

/**
 * Created on 2018/1/20.
 */

@Configuration
public class StreamAutoConfigure {

    @Bean
    @ConditionalOnMissingBean(ProducerListener.class)
    public ProducerListenerBase springCloudStreamProducerListener(Collection<ProducerListenerDelegate> delegates) {
        ProducerListenerBase listenerBase = new ProducerListenerBase();
        for(ProducerListenerDelegate delegate : delegates) {
            listenerBase.registerListener(delegate);
        }
        return listenerBase;
    }

    @Bean
    @ConditionalOnBean(ProducerListenerBase.class)
    public MessageResolver springCloudStreamMessageResolver(Codec codec) {
        IntegrationCodecProxy proxyCodec = new IntegrationCodecProxy(codec);
        return new MessageResolver(proxyCodec);
    }

    @Bean
    @ConditionalOnProperty(value = "spring.cloud.stream.kafka.log-sucess", matchIfMissing = true)
    public LogSucessListener getSucessLogListener(MessageResolver messageResolver) {
        return new LogSucessListener(messageResolver);
    }

    /**
     * 对于某output channel的contentType=application/octet-stream的时候 一般是用于给非java的消费方发送字节流消息
     * 对于javabean需要进行字节转码，暂时使用jackson序列化 {@link com.lhf.stream.kafka.enhance.MessageChannelWapper#preEncodeMessage}
     * 一般情况下不需要channelWapper
     */
    //@Bean
    public ChannelBindingFactoryBeanPostProcessor getChannelBindingFactoryBeanPostProcessor() {
        return new ChannelBindingFactoryBeanPostProcessor();
    }
}
