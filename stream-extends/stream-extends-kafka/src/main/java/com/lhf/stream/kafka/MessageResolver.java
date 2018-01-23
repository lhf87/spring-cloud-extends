package com.lhf.stream.kafka;

import org.springframework.cloud.stream.converter.CompositeMessageConverterFactory;
import org.springframework.messaging.support.GenericMessage;

/**
 * Created on 2018/1/20.
 */

public class MessageResolver {

    private CompositeMessageConverterFactory converterFactory;

    public MessageResolver(CompositeMessageConverterFactory converterFactory) {
        this.converterFactory = converterFactory;
    }

    public Object resolve(String topic, byte[] value) {
        GenericMessage<byte[]> message = new GenericMessage<>(value);
        try {
            return converterFactory.getMessageConverterForAllRegistered()
                    .fromMessage(message, Object.class);
        } catch (Exception e) {
            throw new MessageResolveException(topic, e);
        }
    }

    public static class MessageResolveException extends RuntimeException {

        public MessageResolveException(String topic, Throwable cause) {
            super("fail to resolve spring cloud stream message, topic = " + topic, cause);
        }
    }
}
