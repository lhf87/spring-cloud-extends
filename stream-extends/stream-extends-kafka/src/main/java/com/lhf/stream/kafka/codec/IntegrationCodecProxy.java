package com.lhf.stream.kafka.codec;

import java.io.IOException;

/**
 * Created on 2018/1/25.
 */

public class IntegrationCodecProxy implements Codec {

    private org.springframework.integration.codec.Codec codec;

    public IntegrationCodecProxy(org.springframework.integration.codec.Codec codec) {
        this.codec = codec;
    }

    @Override
    public byte[] encode(Object object) {
        try {
            return codec.encode(object);
        } catch (IOException e) {
            throw new RuntimeException("Could encode", e);
        }
    }

    @Override
    public <T> T decode(byte[] bytes, Class<T> type) {
        try {
            return codec.decode(bytes, type);
        } catch (IOException e) {
            throw new RuntimeException("Could decode", e);
        }
    }
}
