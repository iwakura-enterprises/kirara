package enterprises.iwakura.kirara.core;

import lombok.Data;
import lombok.NonNull;

/**
 * Represents a request query with a key and value.
 * This class is used to encapsulate HTTP request query.
 */
@Data
public class RequestQuery {

    private final String key;
    private final String value;

    /**
     * Creates a new RequestQuery instance.
     *
     * @param key   The query key.
     * @param value The query value.
     */
    public static RequestQuery of(@NonNull String key, @NonNull String value) {
        return new RequestQuery(key, value);
    }
}
