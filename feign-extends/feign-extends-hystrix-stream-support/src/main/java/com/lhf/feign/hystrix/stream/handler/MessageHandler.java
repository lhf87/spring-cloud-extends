package com.lhf.feign.hystrix.stream.handler;

import com.lhf.feign.hystrix.stream.FallbackMessageResolver;

/**
 * Created on 2018/1/9.
 */
public interface MessageHandler {

    boolean checkMessage(FallbackMessageResolver.FallbackMessage message);

    void handle(HandleDetail handleDetail) throws MessageHandleFailedException;
}
