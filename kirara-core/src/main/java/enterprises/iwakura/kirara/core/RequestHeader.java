package enterprises.iwakura.kirara.core;

import lombok.Data;
import lombok.NonNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents a request header with a key and value.
 * This class is used to encapsulate HTTP request headers.
 */
@Data
public class RequestHeader {

    private final String key;
    private final String value;

    /**
     * Constructs a new RequestHeader instance with the specified key and value.
     *
     * @param key   The header key.
     * @param value The header value.
     */
    public RequestHeader(@NonNull String key, @NonNull String value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Creates a new RequestHeader instance.
     *
     * @param key   The header key.
     * @param value The header value.
     *
     * @return A new {@link RequestHeader} instance.
     */
    public static RequestHeader of(@NonNull String key, @NonNull String value) {
        return new RequestHeader(key, value);
    }

    /**
     * Handy method converting a list of RequestHeader objects to a Map. Supports multiple values for the same key
     * by concatenating them with a comma.
     *
     * @param headers The list of RequestHeader objects to convert.
     *
     * @return A Map where the keys are the header keys and the values are the concatenated header values.
     */
    public static Map<String, String> convertToMap(List<RequestHeader> headers) {
        return headers.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        RequestHeader::getKey,
                        Collectors.mapping(RequestHeader::getValue, Collectors.joining(", "))
                ));
    }
}
