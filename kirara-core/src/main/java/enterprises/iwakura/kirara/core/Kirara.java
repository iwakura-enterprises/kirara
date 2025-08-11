package enterprises.iwakura.kirara.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

/**
 * The base class for Kirara library. Extend this class inorder to create a wrapper for a specific API.<br>
 * You may use {@link #createRequest(String, String, Class)} for creating API requests.
 *
 * @see <a href="https://docs.iwakura.enterprises/kirara">Kirara Documentation</a>
 */
@Getter
@Setter
@RequiredArgsConstructor
public abstract class Kirara implements Closeable {

    // Required fields for the Kirara class
    protected final HttpCore httpCore;
    protected final Serializer serializer;

    // Optional fields for easier API interaction
    protected String apiUrl;
    protected List<RequestHeader> defaultRequestHeaders;

    /**
     * Constructs a {@link ApiRequest} class with the specified method, endpoint, and response class.
     * This method is used to create a request that can be sent to the API.
     *
     * @param method        the HTTP method to use (e.g., "GET", "POST", etc.)
     * @param endpoint      the API endpoint to which the request will be sent
     * @param responseClass the class of the expected response type
     * @param <R>           the type of the request, extending {@link ApiRequest}
     * @param <T>           the type of the response expected from the API
     *
     * @return a new instance of {@link ApiRequest} with the specified parameters
     */
    protected <R extends ApiRequest<T>, T> R createRequest(String method, String endpoint, Class<T> responseClass) {
        return new ApiRequest<>(this, method, getApiUrl(), endpoint, responseClass)
                .withExplicitHeaders(defaultRequestHeaders);
    }

    /**
     * Closes the HTTP core.
     *
     * @throws IOException if an I/O error occurs while closing the connection
     */
    @Override
    public void close() throws IOException {
        httpCore.close();
    }

    /**
     * Invoked just before sending a request.
     *
     * @param request the API request being sent
     * @param <T>     the type of the response expected from the API
     */
    public <T> void onRequest(ApiRequest<T> request) {
        // Default implementation does nothing
    }

    /**
     * Invoked when a response is received.
     *
     * @param request  the API request that was sent
     * @param response the response received from the API
     * @param <T>      the type of the response expected from the API
     */
    public <T> void onResponse(ApiRequest<T> request, T response) {
        // Default implementation does nothing
    }

    /**
     * Invoked when an exception occurs during the request processing or response handling.
     *
     * @param request    the API request that was being processed
     * @param exception  the exception that occurred
     * @param <T>        the type of the response expected from the API
     */
    public <T> void onException(ApiRequest<T> request, Throwable exception) {
        // Default implementation does nothing
    }
}
