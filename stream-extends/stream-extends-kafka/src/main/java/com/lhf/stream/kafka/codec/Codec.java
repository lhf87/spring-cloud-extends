package com.lhf.stream.kafka.codec;

import java.io.IOException;

/**
 * Created on 2018/1/25.
 */

public interface Codec {

    byte[] encode(Object object);

    <T> T decode(byte[] bytes, Class<T> type);
}
