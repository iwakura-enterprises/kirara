package enterprises.iwakura.kirara.core.impl;

import enterprises.iwakura.kirara.core.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class HttpUrlConnectionHttpCore extends HttpCore {

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
                HttpURLConnection connection = createConnection(url, method, headers);

                if (body != null) {
                    writeBody(kirara, connection, body);
                }

                kirara.onRequest(request);
                connection.connect();

                T response = readResponse(kirara, connection, responseClass);
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

    protected void writeBody(Kirara kirara, HttpURLConnection connection, Object body) throws IOException {
        connection.setDoOutput(true);
        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(convertBodyToBytes(kirara, body));
        }
    }

    protected <T> T readResponse(Kirara kirara, HttpURLConnection connection, Class<T> responseClass) {
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
                return convertBytesToResponse(kirara, responseBytes, responseClass, responseHeaders);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read response", e);
        }
    }
}
