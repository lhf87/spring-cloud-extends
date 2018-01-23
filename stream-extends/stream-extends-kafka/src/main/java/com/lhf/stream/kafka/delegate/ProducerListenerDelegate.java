package com.lhf.stream.kafka.delegate;

import org.apache.kafka.clients.producer.RecordMetadata;

/**
 * Created on 2018/1/20.
 */
public abstract class ProducerListenerDelegate<K, V> {

    public void onSuccess(String topic, Integer partition, K key, V value, RecordMetadata recordMetadata)
            throws DelegateException {
        throw new DelegateException("must implement this method");
    };

    public void onError(String topic, Integer partition, K key, V value, Exception exception)
            throws DelegateException {
        throw new DelegateException("must implement this method");
    };

    public boolean isInterestedInSuccess() {
        return false;
    }

    public boolean isInterestedInError() {
        return false;
    }
}
