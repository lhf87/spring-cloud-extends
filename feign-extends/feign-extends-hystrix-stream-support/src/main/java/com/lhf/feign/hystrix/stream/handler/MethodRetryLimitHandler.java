package com.lhf.feign.hystrix.stream.handler;

import com.lhf.feign.hystrix.stream.FallbackMessageProcessor;

/**
 * Created on 2018/1/9.
 * 尝试有限次数后终止 尚未实现
 */
public class MethodRetryLimitHandler implements MessageHandler {

    @Override
    public boolean checkMessage(FallbackMessageProcessor.FallbackMessage message) {
        throw new RuntimeException("not impl");
    }

    @Override
    public void handle(HandleDetail handleDetail) throws MessageHandleFailedException {
        throw new RuntimeException("not impl");
    }
}
