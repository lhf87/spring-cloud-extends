package com.lhf.stream.kafka.config;

import com.lhf.stream.kafka.MessageResolver;
import com.lhf.stream.kafka.ProducerListenerBase;
import com.lhf.stream.kafka.delegate.LogSucessListener;
import com.lhf.stream.kafka.delegate.ProducerListenerDelegate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.stream.converter.CompositeMessageConverterFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.ProducerListener;

import java.util.Collection;

/**
 * Created on 2018/1/20.
 */

@Configuration
public class ProducerAutoConfigure {

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
    public MessageResolver springCloudStreamProducerListener(CompositeMessageConverterFactory converterFactory) {
        return new MessageResolver(converterFactory);
    }

    @Bean
    @ConditionalOnBean(ProducerListenerBase.class)
    @ConditionalOnProperty(value = "spring.cloud.stream.kafka.log-sucess")
    public LogSucessListener getSucessLogListener(MessageResolver messageResolver) {
        return new LogSucessListener(messageResolver);
    }
}
