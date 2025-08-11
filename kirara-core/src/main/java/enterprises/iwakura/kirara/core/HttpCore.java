package enterprises.iwakura.kirara.core;

import java.io.Closeable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Abstract class representing the core of Kirara's HTTP backend. Used to send the actual HTTP requests.
 * This class is designed to be extended by specific HTTP implementations (e.g., OkHttp, etc.)<br>
 * The Kirara Core implements Java 8's {@link java.net.HttpURLConnection} in {@link enterprises.iwakura.kirara.core.impl.HttpUrlConnectionHttpCore}
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
     * @param kirara The Kirara instance used for serialization.
     * @param body   The body of the API request, which can be of various types (e.g., byte[], String, or any object).
     *
     * @return A byte array representation of the body.
     */
    protected byte[] convertBodyToBytes(Kirara kirara, Object body) {
        if (body instanceof byte[]) {
            return (byte[]) body;
        } else if (body instanceof String) {
            return ((String) body).getBytes();
        } else {
            return kirara.getSerializer().serialize(body);
        }
    }

    /**
     * Converts a byte array response to an object of the specified class using the Kirara serializer.
     *
     * @param kirara                 The Kirara instance used for deserialization.
     * @param response               The byte array response to convert.
     * @param specifiedResponseClass The class to deserialize into.
     * @param responseHeaders        The headers associated with the response, used for content type detection.
     * @param <T>                    The type of the object to deserialize into.
     *
     * @return The deserialized object of the specified class.
     */
    protected <T> T convertBytesToResponse(Kirara kirara, byte[] response, Class<T> specifiedResponseClass, Map<String, List<String>> responseHeaders) {
        return kirara.getSerializer().deserialize(response, specifiedResponseClass, responseHeaders);
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
        if (response instanceof SupportsKiraraResponse) {
            ((SupportsKiraraResponse) response).setKirara(kirara);
        }
        return response;
    }
}
