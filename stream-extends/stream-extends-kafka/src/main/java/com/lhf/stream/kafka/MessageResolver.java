package com.lhf.stream.kafka;

import com.lhf.stream.kafka.codec.Codec;
import org.springframework.cloud.stream.binder.EmbeddedHeadersMessageConverter;
import org.springframework.cloud.stream.binder.MessageValues;
import org.springframework.cloud.stream.converter.MessageConverterUtils;
import org.springframework.core.serializer.support.SerializationFailedException;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created on 2018/1/20.
 * todo 暂不考虑headerMode=raw && contentType=application/octet-stream 的情况
 */

public class MessageResolver {

    private EmbeddedHeadersMessageConverter embeddedHeadersMessageConverter
                = new EmbeddedHeadersMessageConverter();

    private Codec codec;

    private volatile Map<String, Class<?>> payloadTypeCache = new ConcurrentHashMap<>();

    public MessageResolver(Codec codec) {
        this.codec = codec;
    }

    public Object resolve(String topic, byte[] value) {
        GenericMessage<byte[]> message = new GenericMessage<>(value);
        MessageValues messageValues;
        try {
            messageValues = embeddedHeadersMessageConverter.extractHeaders(message, true);
        }
        catch (Exception e) {
            messageValues = new MessageValues(message);
        }

        String contentType = (String) messageValues.get(MessageHeaders.CONTENT_TYPE);
        MimeType mimeType = MessageConverterUtils.getMimeType(contentType);

        Object payload = deserializePayload((byte[]) messageValues.getPayload(), mimeType);
        return payload;
    }

    private Object deserializePayload(byte[] payload, MimeType contentType) {
        if (contentType == null || MimeTypeUtils.APPLICATION_OCTET_STREAM.equals(contentType)) {
            return payload;
        }

        if ("text".equalsIgnoreCase(contentType.getType()) || MimeTypeUtils.APPLICATION_JSON.equals(contentType)) {
            try {
                return new String(payload, "UTF-8");
            }
            catch (UnsupportedEncodingException e) {
                String errorMessage = "unable to deserialize [java.lang.String]. Encoding not supported. " + e.getMessage();
                throw new SerializationFailedException(errorMessage, e);
            }
        }
        else {
            String className = classNameFromMimeType(contentType);
            try {
                // Cache types to avoid unnecessary ClassUtils.forName calls.
                Class<?> targetType = this.payloadTypeCache.get(className);
                if (targetType == null) {
                    targetType = ClassUtils.forName(className, null);
                    this.payloadTypeCache.put(className, targetType);
                }
                return this.codec.decode(payload, targetType);
            }// catch all exceptions that could occur during de-serialization
            catch (Exception e) {
                String errorMessage = "Unable to deserialize [" + className + "] using the contentType [" + contentType + "] " + e.getMessage();
                throw new SerializationFailedException(errorMessage, e);
            }
        }
    }

    private String classNameFromMimeType(MimeType mimeType) {
        Assert.notNull(mimeType, "mimeType cannot be null.");
        String className = mimeType.getParameter("type");
        if (className == null) {
            return null;
        }
        // unwrap quotes if any
        className = className.replace("\"", "");

        // restore trailing ';'
        if (className.contains("[L")) {
            className += ";";
        }
        return className;
    }
}
