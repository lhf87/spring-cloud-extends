package com.lhf.feign.hystrix.stream;

/**
 * Created on 2018/1/10.
 */
public class MessageConvertException extends RuntimeException {

    private FallbackMessageProcessor.FallbackMessage message;

    public MessageConvertException(String msg, Throwable cause, FallbackMessageProcessor.FallbackMessage message) {
        super(msg, cause);
        this.message = message;
    }

    @Override
    public String getMessage() {
        StringBuffer info = new StringBuffer();
        info.append("message args: ");
        info.append(message.getArgs());
        info.append(" args types: ");
        info.append(message.getArgsTypes());
        return info.toString();
    }
}
