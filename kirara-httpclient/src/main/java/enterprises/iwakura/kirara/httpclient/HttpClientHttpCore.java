package enterprises.iwakura.kirara.httpclient;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

import enterprises.iwakura.kirara.core.ApiRequest;
import enterprises.iwakura.kirara.core.HttpCore;
import enterprises.iwakura.kirara.core.Kirara;
import enterprises.iwakura.kirara.core.RequestHeader;
import lombok.Getter;
import lombok.Setter;

/**
 * Implementation of the {@link HttpCore} using Java 11's {@link HttpClient}.
 */
@Getter
@Setter
public class HttpClientHttpCore extends HttpCore {

    /**
     * The HttpClient instance used to send requests.
     */
    protected HttpClient httpClient;

    /**
     * Constructs a new HttpClientHttpCore with the specified HttpClient.
     *
     * @param httpClient the HttpClient to use for sending requests
     */
    public HttpClientHttpCore(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Constructs a new HttpClientHttpCore using the default HttpClient.
     * This will create a new instance of HttpClient with default settings.
     */
    public HttpClientHttpCore() {
        this.httpClient = HttpClient.newHttpClient();
    }

    /**
     * Creates a new HttpRequest.Builder instance.
     * This method can be overridden to customize the HttpRequest.Builder creation.
     *
     * @return a new HttpRequest.Builder instance
     */
    protected HttpRequest.Builder createHttpRequestBuilder() {
        return HttpRequest.newBuilder();
    }

    @Override
    public <T> CompletableFuture<T> send(ApiRequest<T> request) {
        final var kirara = request.getKirara();
        final var url = request.computeRequestUrl();
        final var method = request.getMethod();
        final var headers = request.getHeaders();
        final var body = request.getBody();
        final var responseClass = request.getResponseClass();
        final var future = new CompletableFuture<T>();
        final var httpRequestBuilder = createHttpRequestBuilder();

        getExecutor().execute(() -> {
            try {
                httpRequestBuilder.uri(new URI(url));
                httpRequestBuilder.method(method, getBodyPublisher(kirara, request, body));

                if (headers != null) {
                    RequestHeader.convertToMap(headers).forEach(httpRequestBuilder::header);
                }

                kirara.onRequest(request);
                final var httpResponse = httpClient.send(httpRequestBuilder.build(), HttpResponse.BodyHandlers.ofByteArray());
                final var responseHeaders = httpResponse.headers().map();
                final var response = convertBytesToResponse(kirara, request, httpResponse.body(), responseClass, responseHeaders);
                kirara.onResponse(request, response);

                future.complete(handleKiraraSupportedResponse(kirara, response));
            } catch (Throwable exception) {
                kirara.onException(request, exception);
                future.completeExceptionally(exception);
            }
        });

        return future;
    }

    @Override
    public void close() {
        if (httpClient != null) {
            // Java 21+ supports HttpClient as AutoCloseable
            if (httpClient instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) httpClient).close();
                } catch (Exception ignored) {
                }
            }
            httpClient = null;
        }
    }

    /**
     * Gets a body publisher for the request body.
     *
     * @param kirara  the Kirara instance associated with the request
     * @param request the API request being sent
     * @param body    the body of the request, which can be null, a byte array, a String, or any other object
     *
     * @return an HttpRequest.BodyPublisher that publishes the body as bytes
     */
    protected HttpRequest.BodyPublisher getBodyPublisher(Kirara kirara, ApiRequest<?> request, Object body) {
        if (body == null) {
            return HttpRequest.BodyPublishers.noBody();
        }

        return HttpRequest.BodyPublishers.ofByteArray(convertBodyToBytes(kirara, request, body));
    }
}
