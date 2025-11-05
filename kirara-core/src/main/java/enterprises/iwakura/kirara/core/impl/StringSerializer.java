package enterprises.iwakura.kirara.core.impl;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import enterprises.iwakura.kirara.core.Serializer;

/**
 * String serializer that handles serialization and deserialization of String objects.
 */
public class StringSerializer implements Serializer {

    /**
     * The charset used for deserialization.
     */
    protected Charset charset;

    /**
     * Constructs a StringSerializer with UTF-8 charset.
     */
    public StringSerializer() {
        this(StandardCharsets.UTF_8);
    }

    /**
     * Constructs a StringSerializer with the specified charset.
     *
     * @param charset The charset to use for deserialization.
     */
    public StringSerializer(Charset charset) {
        this.charset = charset;
    }

    @Override
    public byte[] serialize(Object object) {
        if (object == null) {
            return new byte[0];
        }
        if (object instanceof String) {
            return ((String) object).getBytes();
        }
        throw new UnsupportedOperationException(
            "StringSerializer only supports serialization of String.");
    }

    @Override
    public <T> T deserialize(
        byte[] response,
        Class<T> specifiedResponseClass,
        Map<String, List<String>> responseHeaders
    ) {
        if (specifiedResponseClass == String.class) {
            return (T) new String(response, charset);
        }
        throw new IllegalArgumentException("StringSerializer can only deserialize to String");
    }
}
