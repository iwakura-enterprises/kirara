package enterprises.iwakura.kirara.core.impl;

import enterprises.iwakura.kirara.core.ApiRequest;
import enterprises.iwakura.kirara.core.HttpCore;
import enterprises.iwakura.kirara.core.Kirara;
import enterprises.iwakura.kirara.core.RequestHeader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of {@link HttpCore} using Java's built-in {@link HttpURLConnection}.
 */
public class HttpUrlConnectionHttpCore extends HttpCore {

    /**
     * Constructs a new HttpUrlConnectionHttpCore instance.
     */
    public HttpUrlConnectionHttpCore() {
        // Default constructor
    }

    /**
     * Returns the number of bytes to read at once from the input stream.
     *
     * @return The number of bytes to read at once.
     */
    protected int getNumberOfBytesToReadAtOnce() {
        return 1024;
    }

    @Override
    public <T> CompletableFuture<T> send(ApiRequest<T> request) {
        final Kirara kirara = request.getKirara();
        final String url = request.computeRequestUrl();
        final String method = request.getMethod();
        final List<RequestHeader> headers = request.getHeaders();
        final Object body = request.getBody();
        final Class<T> responseClass = request.getResponseClass();
        final CompletableFuture<T> future = new CompletableFuture<>();

        getExecutor().execute(() -> {
            try {
                kirara.onRequest(request);
                HttpURLConnection connection = createConnection(url, method, headers);

                if (body != null) {
                    writeBody(kirara, request, connection, body);
                }

                connection.connect();

                T response = readResponse(kirara, request, connection, responseClass);
                kirara.onResponse(request, response);

                future.complete(handleKiraraSupportedResponse(kirara, response));
            } catch (Throwable e) {
                kirara.onException(request, e);
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    @Override
    public void close() {
        // Nothing to close
    }

    /**
     * Creates a new {@link HttpURLConnection} for the given URL and method, applying the specified headers.
     *
     * @param url     the URL to connect to
     * @param method  the HTTP method to use (e.g., "GET", "POST", etc.)
     * @param headers the list of request headers to apply to the connection
     *
     * @return a new {@link HttpURLConnection} instance configured with the specified URL, method, and headers
     *
     * @throws IOException if an I/O error occurs while opening the connection
     */
    protected HttpURLConnection createConnection(String url, String method, List<RequestHeader> headers) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod(method);

        if (headers != null) {
            RequestHeader.convertToMap(headers).forEach((key, value) -> {
                if (!value.isEmpty()) {
                    connection.setRequestProperty(key, value);
                }
            });
        }

        return connection;
    }

    /**
     * Writes the body of the request to the connection's output stream.
     *
     * @param kirara      the Kirara instance used for serialization
     * @param apiRequest  the ApiRequest associated with the body
     * @param connection  the HttpURLConnection to write the body to
     * @param body        the body of the request, which can be of various types (e.g., byte[], String, or any object)
     *
     * @throws IOException if an I/O error occurs while writing to the output stream
     */
    protected void writeBody(Kirara kirara, ApiRequest<?> apiRequest, HttpURLConnection connection, Object body) throws IOException {
        connection.setDoOutput(true);
        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(convertBodyToBytes(kirara, apiRequest, body));
        }
    }

    /**
     * Reads the response from the connection and converts it to the specified response class.
     *
     * @param kirara          the Kirara instance used for deserialization
     * @param apiRequest      the ApiRequest associated with the response
     * @param connection      the HttpURLConnection to read the response from
     * @param responseClass   the class of the expected response type
     * @param <T>             the type of the response expected from the API
     *
     * @return an instance of the specified response class containing the response data
     */
    protected <T> T readResponse(Kirara kirara, ApiRequest<?> apiRequest, HttpURLConnection connection, Class<T> responseClass) {
        try {
            try (InputStream inputStream = connection.getInputStream()) {
                final Map<String, List<String>> responseHeaders = connection.getHeaderFields();
                final byte[] responseBytes;
                try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
                    byte[] data = new byte[getNumberOfBytesToReadAtOnce()];
                    int nRead;
                    while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, nRead);
                    }
                    buffer.flush();
                    responseBytes = buffer.toByteArray();
                }
                return convertBytesToResponse(kirara, apiRequest, responseBytes, responseClass, responseHeaders);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read response", e);
        }
    }
}
