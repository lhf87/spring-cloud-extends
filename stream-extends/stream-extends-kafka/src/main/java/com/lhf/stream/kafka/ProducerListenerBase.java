package com.lhf.stream.kafka;

import com.lhf.stream.kafka.delegate.DelegateException;
import com.lhf.stream.kafka.delegate.ProducerListenerDelegate;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.support.ProducerListener;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created on 2018/1/20.
 */
public class ProducerListenerBase implements ProducerListener<byte[], byte[]> {

    private static final Logger logger = LoggerFactory.getLogger(ProducerListenerBase.class);

    private List<ProducerListenerDelegate> listenerDelegates = new CopyOnWriteArrayList<>();

    @Override
    public void onSuccess(String topic, Integer partition, byte[] key, byte[] value, RecordMetadata recordMetadata) {
        for(ProducerListenerDelegate delegate : listenerDelegates) {
            if(delegate.isInterestedInSuccess()) {
                try {
                    delegate.onSuccess(topic, partition, key, value, recordMetadata);
                } catch (DelegateException e) {
                    logger.error("success listener {} exception", delegate.getClass().getName(), e);
                }
            }
        }
    }

    @Override
    public void onError(String topic, Integer partition, byte[] key, byte[] value, Exception exception) {
        int errorDelegateCnt = 0;
        for(ProducerListenerDelegate delegate : listenerDelegates) {
            if(delegate.isInterestedInError()) {
                try {
                    delegate.onError(topic, partition, key, value, exception);
                    errorDelegateCnt++;
                } catch (DelegateException e) {
                    logger.error("error listener {} exception",delegate.getClass().getName(), e);
                }
            }
        }

        if(errorDelegateCnt == 0) {
            FailedMessage failedMessage = new FailedMessage(topic, partition, key, value, exception);
            logger.error(failedMessage.toString(), exception);
        }
    }

    @Override
    public boolean isInterestedInSuccess() {
        return true;
    }

    public void registerListener(ProducerListenerDelegate delegate) {
        if(delegate == null) {
            throw new IllegalArgumentException("listener cannot be null");
        }
        listenerDelegates.add(delegate);
    }

    public List<ProducerListenerDelegate> getProducerListeners() {
        return Collections.unmodifiableList(listenerDelegates);
    }
}
