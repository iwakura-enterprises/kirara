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
     * @param specifiedResponseClass The class to deserialize to.
     * @param statusCode             The HTTP status code of the response.
     * @param headers                The HTTP headers of the response.
     * @param body                   The byte array to deserialize.
     * @param <T>                    The type of the object to deserialize to.
     *
     * @return The deserialized object.
     */
    <T> T deserialize(Class<T> specifiedResponseClass, int statusCode, Map<String, List<String>> headers, byte[] body);

}
