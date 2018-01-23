package com.lhf.stream.kafka.delegate;

import com.lhf.stream.kafka.MessageResolver;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created on 2018/1/20.
 */

public class LogSucessListener extends ProducerListenerDelegate<byte[], byte[]> {

    private static final Logger logger = LoggerFactory.getLogger(LogSucessListener.class);

    private MessageResolver messageResolver;

    public LogSucessListener(MessageResolver messageResolver) {
        this.messageResolver = messageResolver;
    }

    @Override
    public void onSuccess(String topic, Integer partition, byte[] key, byte[] value, RecordMetadata recordMetadata)
            throws DelegateException {
        Object payload = messageResolver.resolve(topic, value);
        if(null == payload) {
            logger.warn("message send success, but cannot resolve message");
        } else {
            logger.info("message send success, detail: {}", payload);
        }
    }

    @Override
    public boolean isInterestedInSuccess() {
        return true;
    }
}
