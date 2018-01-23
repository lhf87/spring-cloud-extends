package com.lhf.feign.hystrix.stream.handler;

import com.lhf.feign.hystrix.stream.FallbackMessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created on 2018/1/9.
 * 只保证执行成功，不保证业务是否成功
 * 比如dao层失败，service层处理了，control层返回的是处理失败信息，对于调用层面来说是执行成功的，但是业务层面是失败的(事务不一致)
 *
 * 所以业务处理时尽可能将错误往外抛
 */

public class MethodRetryHandler implements MessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(MethodRetryHandler.class);

    @Override
    public boolean checkMessage(FallbackMessageProcessor.FallbackMessage message) {
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
