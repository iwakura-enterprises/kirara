package enterprises.iwakura.kirara.core;

import lombok.Data;
import lombok.NonNull;

/**
 * Represents a path parameter in a request.
 * This class is used to encapsulate a key-value pair that can be used in URL paths
 * for constructing dynamic URLs in HTTP requests.
 */
@Data
public class PathParameter {

    private final String key;
    private final String value;

    /**
     * Constructor to create a PathParameter instance.
     *
     * @param key   The path parameter key.
     * @param value The path parameter value.
     */
    public PathParameter(String key, String value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Creates a new PathParameter instance.
     *
     * @param key   The header key.
     * @param value The header value.
     *
     * @return Non-null {@link PathParameter} instance
     */
    public static PathParameter of(@NonNull String key, @NonNull String value) {
        return new PathParameter(key, value);
    }
}
