package enterprises.iwakura.kirara.gson;

import com.google.gson.Gson;
import enterprises.iwakura.kirara.core.Serializer;

import java.util.List;
import java.util.Map;

/**
 * Implementation of the {@link Serializer} for {@link Gson} serialization.
 */
public class GsonSerializer implements Serializer {

    /**
     * The Gson instance used for serialization and deserialization.
     */
    protected Gson gson;

    /**
     * Constructs a new GsonSerializer with the specified Gson instance.
     *
     * @param gson The Gson instance to use for serialization and deserialization.
     */
    public GsonSerializer(Gson gson) {
        this.gson = gson;
    }

    @Override
    public byte[] serialize(Object object) {
        if (object == null) {
            return new byte[0];
        }

        // If the object is a String, convert it to bytes directly
        if (object instanceof String) {
            return ((String) object).getBytes();
        }

        // Otherwise, use Gson to serialize the object to JSON and then convert it to bytes
        return gson.toJson(object).getBytes();
    }

    @Override
    public <T> T deserialize(byte[] response, Class<T> specifiedResponseClass, Map<String, List<String>> responseHeaders) {
        if (response == null || response.length == 0) {
            return null; // Return null for empty responses
        }

        final List<String> contentType = responseHeaders.get("Content-Type");

        // Treat a response w/o Content-Type as JSON
        if (contentType == null || contentType.isEmpty() || contentType.contains("application/json")) {
            return gson.fromJson(new String(response), specifiedResponseClass);
        }

        throw new IllegalArgumentException("Unsupported Content-Type: " + contentType);
    }
}
