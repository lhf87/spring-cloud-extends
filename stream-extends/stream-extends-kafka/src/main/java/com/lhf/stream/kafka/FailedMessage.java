package com.lhf.stream.kafka;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;

/**
 * Created on 2018/1/20.
 */

class FailedMessage implements Serializable {

    private String topic;

    private Integer partition;

    private byte[] key;

    private byte[] value;

    private ExceptionDetail reason;

    FailedMessage(String topic, Integer partition,
                  byte[] key, byte[] value,
                  Exception exception) {
        this.topic = topic;
        this.partition = partition;
        this.key = key;
        this.value = value;
        this.reason = new ExceptionDetail(exception);
    }

    @Override
    public String toString() {
        return errorInfo(topic, partition, reason);
    }

    private static String errorInfo(String topic, Integer partition, ExceptionDetail reason) {
        StringBuilder builder = new StringBuilder();

        builder.append("message send error : ")
                .append("topic = ").append(topic).append(", ")
                .append("partition = ").append(partition).append(", ")
                .append("exception type = ").append(reason.getExceptionType()).append(", ")
                .append("exception message = ").append(reason.getMessage());

        return builder.toString();
    }

    static class ExceptionDetail {
        private String exceptionType;

        private String message;

        private String stackTrace;

        public ExceptionDetail(Exception exp) {
            if(null != exp) {
                exceptionType = exp.getClass().getName();
                message = exp.getMessage();
                StringWriter writer = new StringWriter();
                PrintWriter printWriter = new PrintWriter(writer);
                exp.printStackTrace(printWriter);
                stackTrace = writer.toString();
            }
        }

        public String getExceptionType() {
            return exceptionType;
        }

        public String getMessage() {
            return message;
        }

        public String getStackTrace() {
            return stackTrace;
        }
    }
}
