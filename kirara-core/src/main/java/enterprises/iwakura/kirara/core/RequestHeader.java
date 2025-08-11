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
     * Creates a new RequestHeader instance.
     *
     * @param key   The header key.
     * @param value The header value.
     */
    public static RequestHeader of(@NonNull String key, @NonNull String value) {
        return new RequestHeader(key, value);
    }

    public static Map<String, String> convertToMap(List<RequestHeader> headers) {
        return headers.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        RequestHeader::getKey,
                        Collectors.mapping(RequestHeader::getValue, Collectors.joining(", "))
                ));
    }
}
