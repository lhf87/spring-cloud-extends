package com.lhf.feign.hystrix.stream.handler;

import com.lhf.feign.hystrix.stream.FallbackMessageResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created on 2018/1/9.
 */

public class MethodRetryHandler implements MessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(MethodRetryHandler.class);

    @Override
    public boolean checkMessage(FallbackMessageResolver.FallbackMessage message) {
        if(message.getArgs().length != message.getArgsTypes().length) {
            return false;
        }

        for(String argsType : message.getArgsTypes()) {
            try {
                Class.forName(argsType, false, MethodRetryHandler.class.getClassLoader());
            } catch (ClassNotFoundException e) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void handle(HandleDetail handleDetail) throws MessageHandleFailedException {
        try {
            handleDetail.getMethod().invoke(handleDetail.getInstance(), handleDetail.getArgs());
        } catch (Exception e) {
            throw new MessageHandleFailedException(handleDetail, e);
        }
        logger.info("retry method sucess: {}", handleDetail);
    }
}
