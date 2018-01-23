package com.lhf.feign.hystrix.stream.handler;

import com.lhf.feign.hystrix.stream.FallbackMessageProcessor;

/**
 * Created on 2018/1/9.
 */
public interface MessageHandler {

    boolean checkMessage(FallbackMessageProcessor.FallbackMessage message);

    void handle(HandleDetail handleDetail) throws MessageHandleFailedException;
}
