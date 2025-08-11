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
     * Creates a new PathParameter instance.
     *
     * @param key   The header key.
     * @param value The header value.
     */
    public static PathParameter of(@NonNull String key, @NonNull String value) {
        return new PathParameter(key, value);
    }
}
