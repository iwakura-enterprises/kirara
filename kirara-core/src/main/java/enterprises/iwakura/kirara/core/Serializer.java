package enterprises.iwakura.kirara.core;

import java.util.List;
import java.util.Map;

/**
 * Kirara's serializer interface used to (de)serialize objects to and from byte arrays.
 */
public interface Serializer {

    /**
     * Serializes an object to a byte array.
     *
     * @param object The object to serialize.
     *
     * @return The serialized byte array.
     */
    byte[] serialize(Object object);

    /**
     * Deserializes a byte array to an object of the specified class.
     *
     * @param response               The byte array to deserialize.
     * @param specifiedResponseClass The class to deserialize into.
     * @param responseHeaders        The headers associated with the response, used for content type detection.
     * @param <T>                    The type of the object to deserialize into.
     *
     * @return The deserialized object.
     */
    <T> T deserialize(byte[] response, Class<T> specifiedResponseClass, Map<String, List<String>> responseHeaders);

}
