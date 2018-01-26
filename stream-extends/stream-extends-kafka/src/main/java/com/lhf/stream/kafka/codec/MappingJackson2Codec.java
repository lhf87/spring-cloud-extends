package com.lhf.stream.kafka.codec;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Created on 2018/1/25.
 */

public class MappingJackson2Codec implements Codec {

    private ObjectMapper objectMapper;

    public MappingJackson2Codec() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public byte[] encode(Object object) {
        try {
            return this.objectMapper.writeValueAsBytes(object);
        } catch (IOException e) {
            throw new RuntimeException("Could not write JSON: " + e.getMessage(), e);
        }
    }

    @Override
    public <T> T decode(byte[] bytes, Class<T> type) {
        try {
            return this.objectMapper.readValue(bytes, type);
        } catch (IOException e) {
            throw new RuntimeException("Could not read JSON: " + e.getMessage(), e);
        }
    }
}
