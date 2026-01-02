package enterprises.iwakura.kirara.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/**
 * Abstract class representing the core of Kirara's HTTP backend. Used to send the actual HTTP requests.
 * This class is designed to be extended by specific HTTP implementations (e.g., OkHttp, etc.)<br>
 * The Kirara Core implements Java 8's {@link java.net.HttpURLConnection} in
 * {@link enterprises.iwakura.kirara.core.impl.HttpUrlConnectionHttpCore}
 */
public abstract class HttpCore implements Closeable {

    /**
     * The executor to schedule API requests on.
     */
    protected Executor executor = Executors.newCachedThreadPool();

    /**
     * Default constructor for HttpCore. This can be overridden by subclasses if needed.
     */
    public HttpCore() {
        // Default constructor, can be overridden by subclasses if needed
    }

    /**
     * Sends an API request and returns a CompletableFuture that will be completed with the response.
     *
     * @param request The API request to send.
     * @param <T>     The type of the response expected from the API.
     *
     * @return A CompletableFuture that will be completed with the response.
     */
    public abstract <T> CompletableFuture<T> send(ApiRequest<T> request);

    /**
     * Closes the HTTP core, releasing any resources it holds.
     */
    public abstract void close();

    /**
     * Returns an executor that can be used to schedule API requests.
     *
     * @return An Executor that can be used to schedule API requests.
     */
    public Executor getExecutor() {
        return executor;
    }

    /**
     * Converts the body of an API request to a byte array. Default implementation handles
     * byte arrays and Strings, and uses the serializer for other object types.
     *
     * @param kirara     The Kirara instance used for serialization.
     * @param apiRequest The API request
     * @param body       The body of the API request, which can be of various types (e.g., byte[], String, or any
     *                   object).
     *
     * @return A byte array representation of the body.
     */
    protected byte[] convertBodyToBytes(Kirara kirara, ApiRequest<?> apiRequest, Object body) {
        if (body instanceof byte[]) {
            return (byte[]) body;
        } else if (body instanceof String) {
            return ((String) body).getBytes();
        } else {
            Serializer serializer = apiRequest.getSerializerOverride() != null ?
                apiRequest.getSerializerOverride() : kirara.getSerializer();
            return serializer.serialize(body);
        }
    }

    /**
     * Converts a byte array response to an object of the specified class using the Kirara serializer.
     *
     * @param kirara                 The Kirara instance used for deserialization.
     * @param apiRequest             The API request associated with the response.
     * @param response               The byte array response to convert.
     * @param specifiedResponseClass The class to deserialize into.
     * @param responseStatusCode     The HTTP status code of the response.
     * @param responseHeaders        The headers associated with the response, used for content type detection.
     * @param <T>                    The type of the object to deserialize into.
     *
     * @return The deserialized object of the specified class.
     */
    protected <T> T convertBytesToResponse(
        Kirara kirara,
        ApiRequest<?> apiRequest,
        byte[] response,
        Class<T> specifiedResponseClass,
        int responseStatusCode,
        Map<String, List<String>> responseHeaders
    ) {
        Serializer serializer = apiRequest.getSerializerOverride() != null ?
            apiRequest.getSerializerOverride() : kirara.getSerializer();
        byte[] decompressedResponse = decompressIfNeeded(response, responseHeaders);
        return serializer.deserialize(specifiedResponseClass, responseStatusCode, responseHeaders, decompressedResponse);
    }

    /**
     * Decompresses the given byte array if the headers indicate that it is compressed.
     *
     * @param data    The byte array to potentially decompress.
     * @param headers The headers associated with the response, used to check for compression.
     *
     * @return The decompressed byte array, or the original byte array if no decompression was needed.
     */
    protected byte[] decompressIfNeeded(byte[] data, Map<String, List<String>> headers) {
        List<String> contentEncoding = headers.get("Content-Encoding");
        if (contentEncoding == null || contentEncoding.isEmpty()) {
            if (data.length >= 2) {
                // Check for GZIP magic number (0x1f 0x8b)
                int gzipMagic = ((data[0] & 0xff) | ((data[1] & 0xff) << 8));
                if (gzipMagic == GZIPInputStream.GZIP_MAGIC) {
                    contentEncoding = Collections.singletonList("gzip");
                } else {
                    // Check for ZLIB header (RFC 1950)
                    // CMF (Compression Method and flags), CM = 8 (deflate)
                    // CINFO = 7 (32K window size) -> 0b01111000 = 0x78
                    // The second byte (FLG) has a checkbit requirement.
                    int cmf = data[0] & 0xff;
                    int flg = data[1] & 0xff;
                    if ((cmf & 0x0f) == 8 && (cmf * 256 + flg) % 31 == 0) {
                        contentEncoding = Collections.singletonList("deflate");
                    } else {
                        return data;
                    }
                }
            } else {
                return data;
            }
        }

        String encoding = contentEncoding.get(0).toLowerCase();
        try {
            if (encoding.contains("gzip")) {
                return decompress(new GZIPInputStream(new ByteArrayInputStream(data)));
            } else if (encoding.contains("deflate")) {
                return decompress(new InflaterInputStream(new ByteArrayInputStream(data)));
            } else if (encoding.contains("identity")) {
                return data;
            } else {
                System.err.println("[Kirara] Unsupported Content-Encoding: " + encoding);
                return data;
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to decompress response", e);
        }
    }

    /**
     * Decompresses data from the given InputStream and returns it as a byte array.
     *
     * @param inputStream The InputStream to read compressed data from.
     *
     * @return A byte array containing the decompressed data.
     *
     * @throws IOException If an I/O error occurs during decompression.
     */
    protected byte[] decompress(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, len);
        }
        return outputStream.toByteArray();
    }

    /**
     * Handles the response for Kirara-supported responses by setting the Kirara instance on the response.
     *
     * @param kirara   The Kirara instance associated with the request.
     * @param response The response to handle.
     * @param <T>      The type of the response.
     *
     * @return The response with the Kirara instance set, if applicable.
     */
    protected <T> T handleKiraraSupportedResponse(Kirara kirara, T response) {
        if (response instanceof SupportsKiraraResponse<?>) {
            ((SupportsKiraraResponse) response).setKirara(kirara);
        }
        return response;
    }
}
