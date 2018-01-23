package com.lhf.feign.hystrix.stream;

import com.lhf.feign.hystrix.stream.handler.HandleDetail;
import com.lhf.feign.hystrix.stream.handler.MessageHandleFailedException;
import com.lhf.feign.hystrix.stream.handler.MessageHandler;
import com.lhf.feign.hystrix.stream.handler.MethodRetryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;

import java.util.Arrays;

/**
 * Created on 2018/1/8.
 */

public class FallbackMessageProcessor {

    private static final Logger logger = LoggerFactory.getLogger(FallbackMessageProcessor.class);

    private ApplicationContext context;

    private MessageHandler messageHandler = new MethodRetryHandler();

    public FallbackMessageProcessor(ApplicationContext applicationContext) {
        this.context = applicationContext;
    }

    public void receiveMessage(FallbackMessage message) {
        logger.info("receive fallback message: {}", message);

        if(messageHandler.checkMessage(message)) {
            HandleDetail handleDetail = MessageConvertUtils.convert(message, context);
            messageHandler.handle(handleDetail);
        } else {
            throw new MessageHandleFailedException(message);
        }
    }

    public void sendMessage(MessageChannel channel, FallbackMessage message) {
        channel.send(MessageBuilder.withPayload(message).build());
    }

    public static class FallbackMessage {

        private RequestMapping requestMapping;

        private Object[] args;

        private String[] argsTypes;

        private long timestamp;

        public RequestMapping getRequestMapping() {
            return requestMapping;
        }

        public void setRequestMapping(RequestMapping requestMapping) {
            this.requestMapping = requestMapping;
        }

        public Object[] getArgs() {
            return args;
        }

        public void setArgs(Object[] args) {
            this.args = args;
        }

        public String[] getArgsTypes() {
            return argsTypes;
        }

        public void setArgsTypes(String[] argsTypes) {
            this.argsTypes = argsTypes;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        @Override
        public String toString() {
            return "FallbackMessage{" +
                    "requestMapping=" + requestMapping +
                    ", args=" + Arrays.toString(args) +
                    ", argsTypes=" + Arrays.toString(argsTypes) +
                    ", timestamp=" + timestamp +
                    '}';
        }

        public static class RequestMapping {
            private String name;

            private String[] path;

            private String[] headers;

            private String[] method;

            private String[] produces;

            private String[] params;

            private String[] consumes;

            public String getName() {
                return name;
            }

            public RequestMapping setName(String name) {
                this.name = name;
                return this;
            }

            public String[] getPath() {
                return path;
            }

            public RequestMapping setPath(String[] path) {
                this.path = path;
                return this;
            }

            public String[] getHeaders() {
                return headers;
            }

            public RequestMapping setHeaders(String[] headers) {
                this.headers = headers;
                return this;
            }

            public String[] getMethod() {
                return method;
            }

            public RequestMapping setMethod(String[] method) {
                this.method = method;
                return this;
            }

            public String[] getProduces() {
                return produces;
            }

            public RequestMapping setProduces(String[] produces) {
                this.produces = produces;
                return this;
            }

            public String[] getParams() {
                return params;
            }

            public RequestMapping setParams(String[] params) {
                this.params = params;
                return this;
            }

            public String[] getConsumes() {
                return consumes;
            }

            public RequestMapping setConsumes(String[] consumes) {
                this.consumes = consumes;
                return this;
            }

            @Override
            public String toString() {
                return "RequestMapping{" +
                        "name='" + name + '\'' +
                        ", path=" + Arrays.toString(path) +
                        ", headers=" + Arrays.toString(headers) +
                        ", method=" + Arrays.toString(method) +
                        ", produces=" + Arrays.toString(produces) +
                        ", params=" + Arrays.toString(params) +
                        ", consumes=" + Arrays.toString(consumes) +
                        '}';
            }
        }
    }

}
