package enterprises.iwakura.kirara.core;

import enterprises.iwakura.kirara.core.impl.CompletedApiRequest;
import lombok.Getter;
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
public abstract class Kirara implements Closeable {

    /**
     * The HTTP core used to send requests and receive responses.
     */
    protected final HttpCore httpCore;

    /**
     * The serializer used to convert objects to and from JSON or other formats.
     */
    protected final Serializer serializer;

    /**
     * The base URL of the API to which requests will be sent.
     * All API request's endpoints will be appended to this URL.
     */
    protected String apiUrl;

    /**
     * Default request headers that will be included in every API request.
     * This can be used to set common headers like "User-Agent", "Accept", etc
     */
    protected List<RequestHeader> defaultRequestHeaders;

    /**
     * Constructs a new Kirara instance with the specified HTTP core and serializer.
     *
     * @param httpCore   the HTTP core used to send requests
     * @param serializer the serializer used for request and response bodies
     */
    public Kirara(HttpCore httpCore, Serializer serializer) {
        this.httpCore = httpCore;
        this.serializer = serializer;
    }

    /**
     * Constructs a new Kirara instance with the specified HTTP core, serializer, and API URL.
     *
     * @param httpCore   the HTTP core used to send requests
     * @param serializer the serializer used for request and response bodies
     * @param apiUrl     the base URL of the API to which requests will be sent
     */
    public Kirara(HttpCore httpCore, Serializer serializer, String apiUrl) {
        this(httpCore, serializer);
        this.apiUrl = apiUrl;
    }

    /**
     * A helper method. Constructs a {@link ApiRequest} class with the specified method, endpoint, and response class.
     * This method is used to create a request that can be sent to the API. Uses {@link #getApiUrl()} as the base URL
     * and includes {@link #defaultRequestHeaders} in the request.
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
     * A helper method. Constructs a {@link CompletedApiRequest} with the specified response. This method is used to
     * create a request that is already completed with a predefined response.
     *
     * @param response the response object to be returned
     * @param <T>      the type of the response expected from the API
     *
     * @return a new instance of {@link CompletedApiRequest} with the specified response
     */
    protected <T> CompletedApiRequest<T> createCompletedRequest(T response) {
        return new CompletedApiRequest<>(this, response);
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
     * @param request   the API request that was being processed
     * @param exception the exception that occurred
     * @param <T>       the type of the response expected from the API
     */
    public <T> void onException(ApiRequest<T> request, Throwable exception) {
        // Default implementation does nothing
    }
}
