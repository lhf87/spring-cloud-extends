package com.lhf.feign.hystrix.stream.handler;

import com.lhf.feign.hystrix.stream.FallbackMessageProcessor;

/**
 * Created on 2018/1/9.
 */
public class MessageHandleFailedException extends RuntimeException {

    private FallbackMessageProcessor.FallbackMessage message;

    private HandleDetail detail;

    public MessageHandleFailedException(FallbackMessageProcessor.FallbackMessage message) {
        this.message = message;
    }

    public MessageHandleFailedException(HandleDetail detail, Throwable cause) {
        super(cause);
        this.detail = detail;
    }

    @Override
    public String getMessage() {
        StringBuffer info = new StringBuffer();
        info.append("handle failed :");

        if(null != message) {
            info.append(message.toString());
        }

        if(null != detail) {
            info.append(detail);
        }

        return info.toString();
    }
}
