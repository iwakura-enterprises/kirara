package enterprises.iwakura.kirara.gson;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import enterprises.iwakura.kirara.core.Serializer;

/**
 * Implementation of the {@link Serializer} for {@link Gson} serialization. By default, accepts "application/json"
 * content type or when no content type is specified.
 */
public class GsonSerializer implements Serializer {

    /**
     * The default supported content types for deserialization.
     */
    public static final List<String> DEFAULT_SUPPORTED_CONTENT_TYPES = Collections.singletonList("application/json");

    /**
     * The Gson instance used for serialization and deserialization.
     */
    protected Gson gson;

    /**
     * The list of supported content types for deserialization.
     */
    protected List<String> supportedContentTypes;

    /**
     * Constructs a new GsonSerializer with a default Gson instance and default supported content types.
     */
    public GsonSerializer() {
        this(new Gson(), DEFAULT_SUPPORTED_CONTENT_TYPES);
    }

    /**
     * Constructs a new GsonSerializer with the specified Gson instance and default supported content types.
     *
     * @param gson The Gson instance to use for serialization and deserialization.
     */
    public GsonSerializer(Gson gson) {
        this(gson, DEFAULT_SUPPORTED_CONTENT_TYPES);
    }

    /**
     * Constructs a new GsonSerializer with the specified Gson instance and supported content types.
     *
     * @param gson                  The Gson instance to use for serialization and deserialization.
     * @param supportedContentTypes The list of supported content types for deserialization.
     */
    public GsonSerializer(Gson gson, List<String> supportedContentTypes) {
        this.gson = gson;
        this.supportedContentTypes = supportedContentTypes;
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
    public <T> T deserialize(byte[] response, Class<T> specifiedResponseClass,
        Map<String, List<String>> responseHeaders) {
        if (response == null || response.length == 0) {
            return null; // Return null for empty responses
        }

        List<String> contentType = responseHeaders.get("Content-Type");

        boolean supportsAnyContentType = contentType != null && contentType.stream().anyMatch(ct -> {
            for (String supportedContentType : supportedContentTypes) {
                if (ct.contains(supportedContentType)) {
                    return true;
                }
            }
            return false;
        });

        // Treat a response w/o Content-Type as JSON
        if (contentType == null || contentType.isEmpty() || supportsAnyContentType) {
            String stringResponse = new String(response);
            try {
                return gson.fromJson(stringResponse, specifiedResponseClass);
            } catch (Exception exception) {
                throw new IllegalArgumentException(
                    "Failed to deserialize response to " + specifiedResponseClass.getName() + ": " + stringResponse,
                    exception);
            }
        }

        throw new IllegalArgumentException("Unsupported Content-Type: " + contentType);
    }
}
