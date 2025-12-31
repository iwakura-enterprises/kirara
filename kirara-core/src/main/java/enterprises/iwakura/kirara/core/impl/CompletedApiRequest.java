package enterprises.iwakura.kirara.core.impl;

import java.util.concurrent.CompletableFuture;

import enterprises.iwakura.kirara.core.ApiRequest;
import enterprises.iwakura.kirara.core.Kirara;

/**
 * An implementation of ApiRequest that represents a completed request with a predefined response. Useful when you
 * cache
 * API responses and want to return them without making an actual HTTP request. Overrides the {@link #send()} to return
 * completed future with the predefined response. Does not support computing a request URL as it does not hold the
 * necessary
 * information.
 *
 * @param <T> The type of the response object.
 */
public class CompletedApiRequest<T> extends ApiRequest<T> {

    /**
     * The response object to be returned.
     */
    protected final T response;

    /**
     * Constructs an ApiRequest with the specified parameters.
     *
     * @param kirara   the Kirara instance associated with this request
     * @param response the response object to be returned
     */
    public CompletedApiRequest(
        Kirara kirara,
        T response
    ) {
        //noinspection unchecked
        super(kirara, null, null, null, (Class<T>) response.getClass());
        this.response = response;
    }

    /**
     * Computing a request URL is not supported for CompletedApiRequest.
     *
     * @return Nothing, as this method always throws an exception.
     *
     * @throws IllegalStateException always thrown to indicate that this operation is not supported.
     */
    @Override
    public String computeRequestUrl() {
        throw new IllegalStateException("Completed API Request do not have a request URL");
    }

    /**
     * Returns a completed future with the predefined response.
     *
     * @return A CompletableFuture that is already completed with the predefined response.
     */
    @Override
    public CompletableFuture<T> send() {
        return CompletableFuture.completedFuture(response);
    }
}
