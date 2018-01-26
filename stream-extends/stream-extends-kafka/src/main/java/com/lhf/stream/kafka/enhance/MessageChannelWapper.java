package com.lhf.stream.kafka.enhance;

import com.lhf.stream.kafka.codec.Codec;
import com.lhf.stream.kafka.codec.MappingJackson2Codec;
import org.springframework.cloud.stream.config.BindingProperties;
import org.springframework.cloud.stream.converter.MessageConverterUtils;
import org.springframework.messaging.*;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.StringUtils;

/**
 * Created on 2018/1/24.
 * 如果某个channel的contentType为application/octet-stream, 则对消息体内对象预处理成byte[]
 */
public class MessageChannelWapper implements SubscribableChannel {

    private SubscribableChannel channel;

    private BindingProperties bindingProperties;

    private Codec codec = new MappingJackson2Codec();

    public MessageChannelWapper(SubscribableChannel subscribableChannel,
                                BindingProperties bindingProperties) {
        this.channel = subscribableChannel;
        this.bindingProperties = bindingProperties;
    }

    @Override
    public boolean send(Message<?> message) {
        return send(message, -1);
    }

    @Override
    public boolean send(Message<?> message, long timeout) {
        String contentType = bindingProperties.getContentType();
        if(StringUtils.hasText(contentType)) {
            MimeType mimeType = MessageConverterUtils.getMimeType(contentType);
            if(mimeType.equals(MimeTypeUtils.APPLICATION_OCTET_STREAM) &&
                    !(message.getPayload() instanceof byte[]) ) {
                message = MessageBuilder.createMessage(
                        preEncodeMessage(message.getPayload()), message.getHeaders());
            }
        }
        return channel.send(message, timeout);
    }

    @Override
    public boolean subscribe(MessageHandler handler) {
        return channel.subscribe(handler);
    }

    @Override
    public boolean unsubscribe(MessageHandler handler) {
        return channel.unsubscribe(handler);
    }

    private byte[] preEncodeMessage(Object payload) {
        return codec.encode(payload);
    }

    private Object preDecodeMessage(byte[] payload) {
        return null;
    }

    class MessageHandlerWapper implements MessageHandler {

        private MessageHandler handler;

        MessageHandlerWapper(MessageHandler handler) {
            this.handler = handler;
        }

        @Override
        public void handleMessage(Message<?> message) throws MessagingException {

        }
    }
}
