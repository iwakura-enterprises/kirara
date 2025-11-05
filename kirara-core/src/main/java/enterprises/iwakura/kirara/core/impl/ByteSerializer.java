package enterprises.iwakura.kirara.core.impl;

import java.util.List;
import java.util.Map;

import enterprises.iwakura.kirara.core.Serializer;

/**
 * Byte serializer that returns the byte array response. <b>Does not support serialization</b>.
 */
public class ByteSerializer implements Serializer {

    @Override
    public byte[] serialize(Object object) {
        throw new UnsupportedOperationException("ByteSerializer does not support serialization.");
    }

    @Override
    public <T> T deserialize(
        byte[] response,
        Class<T> specifiedResponseClass,
        Map<String, List<String>> responseHeaders
    ) {
        if (specifiedResponseClass != byte[].class) {
            throw new IllegalArgumentException("ByteSerializer can only deserialize to byte[]");
        }
        return (T) response;
    }
}
