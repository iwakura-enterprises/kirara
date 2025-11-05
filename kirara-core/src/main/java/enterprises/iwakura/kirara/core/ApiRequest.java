package enterprises.iwakura.kirara.core;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents an API request in Kirara.
 * This class encapsulates the details of an API request, including the HTTP method,
 * URL, endpoint, response class, headers, path parameters, request queries, and body.
 * It provides methods to set these details and compute the final request URL.
 *
 * @param <T> the type of the response expected from the API
 */
@SuppressWarnings("unchecked")
@Getter
@Setter
public class ApiRequest<T> {

    /**
     * Kirara instance associated with this request.
     */
    protected final Kirara kirara;

    /**
     * The HTTP method for this request (e.g., "GET", "POST").
     */
    protected final String method;

    /**
     * The API endpoint to which the request will be sent.
     * This is a relative path that will be appended to the base URL.
     * If url is null, the default API URL from Kirara will be used. If that will be null as well,
     * the endpoint will be used as the full URL.
     */
    protected final String endpoint;

    /**
     * The class of the expected response type.
     * This is used to deserialize the response body into the appropriate type.
     */
    protected final Class<T> responseClass;

    /**
     * The base URL for the API. If null, the default API URL from Kirara will be used.
     * If that is also null, the endpoint will be used as the full URL.
     */
    protected String url;

    /**
     * The headers to be included in this API request.
     * This can be null, in which case no headers will be set.
     */
    protected List<RequestHeader> headers;

    /**
     * The path parameters to be included in this API request.
     * This can be null, in which case no path parameters will be set.
     */
    protected Set<PathParameter> pathParameters;

    /**
     * The request queries to be included in this API request.
     * This can be null, in which case no request queries will be set.
     */
    protected Set<RequestQuery> requestQueries;

    /**
     * The body of this API request.
     * This can be null, in which case no body will be set.
     * It is typically used for requests that require a payload, such as POST or PUT requests.
     */
    protected Object body;

    /**
     * An optional serializer override for this specific request.
     * If set, this serializer will be used instead of the default Kirara serializer
     * for serializing the request body and deserializing the response.
     */
    protected Serializer serializerOverride;

    /**
     * Constructs an ApiRequest with the specified parameters.
     *
     * @param kirara        the Kirara instance associated with this request
     * @param method        the HTTP method (e.g., "GET", "POST")
     * @param url           the base URL for the API, can be null to use the default API URL
     * @param endpoint      the API endpoint to which the request will be sent
     * @param responseClass the class of the expected response type
     */
    public ApiRequest(Kirara kirara, String method, String url, String endpoint, Class<T> responseClass) {
        this.kirara = kirara;
        this.url = url;
        this.method = method;
        this.endpoint = endpoint;
        this.responseClass = responseClass;
    }

    /**
     * Sets the URL for this API request.
     *
     * @param url the URL to set for this request. If null, the default API URL from Kirara will be used.
     * @param <R> the type of the request, extending ApiRequest
     *
     * @return a reference to this ApiRequest, allowing for method chaining.
     */
    public <R extends ApiRequest<T>> R withUrl(String url) {
        this.url = url;
        return (R) this;
    }

    /**
     * Sets a serializer override for this API request.
     * If set, this serializer will be used instead of the default Kirara serializer
     * for serializing the request body and deserializing the response.
     *
     * @param serializer the serializer to use for this request.
     * @param <R>        the type of the request, extending ApiRequest
     *
     * @return a reference to this ApiRequest, allowing for method chaining.
     */
    public <R extends ApiRequest<T>> R withSerializerOverride(Serializer serializer) {
        this.serializerOverride = serializer;
        return (R) this;
    }

    /**
     * Sets headers for this API requests, discarding any previously set headers.
     *
     * @param headers the list of headers to set for this request. If null, no headers will be set.
     * @param <R>     the type of the request, extending ApiRequest
     *
     * @return a reference to this ApiRequest, allowing for method chaining.
     */
    public <R extends ApiRequest<T>> R withExplicitHeaders(List<RequestHeader> headers) {
        this.headers = headers;
        return (R) this;
    }

    /**
     * Sets path parameters for this API request, discarding any previously set path parameters.
     *
     * @param pathParameters the set of path parameters to set for this request. If null, no path parameters will be
     *                       set.
     * @param <R>            the type of the request, extending ApiRequest
     *
     * @return a reference to this ApiRequest, allowing for method chaining.
     */
    public <R extends ApiRequest<T>> R withExplicitPathParameters(Set<PathParameter> pathParameters) {
        this.pathParameters = pathParameters;
        return (R) this;
    }

    /**
     * Sets request queries for this API request, discarding any previously set request queries.
     *
     * @param requestQueries the set of request queries to set for this request. If null, no request queries will be
     *                       set.
     * @param <R>            the type of the request, extending ApiRequest
     *
     * @return a reference to this ApiRequest, allowing for method chaining.
     */
    public <R extends ApiRequest<T>> R withExplicitRequestQueries(Set<RequestQuery> requestQueries) {
        this.requestQueries = requestQueries;
        return (R) this;
    }

    /**
     * Adds a request header to this API request.
     * If headers were previously set, this method appends the new header to the existing list
     * or creates a new list if none exists.
     *
     * @param header the request header to add to this API request.
     * @param <R>    the type of the request, extending ApiRequest
     *
     * @return a reference to this ApiRequest, allowing for method chaining.
     */
    public <R extends ApiRequest<T>> R withHeader(RequestHeader header) {
        if (this.headers == null) {
            this.headers = new ArrayList<>();
        }
        this.headers.add(header);
        return (R) this;
    }

    /**
     * Adds a path parameter to this API request.
     * If path parameters were previously set, this method appends the new parameter to the existing set
     * or creates a new set if none exists.
     *
     * @param pathParameter the path parameter to add to this API request.
     * @param <R>           the type of the request, extending ApiRequest
     *
     * @return a reference to this ApiRequest, allowing for method chaining.
     */
    public <R extends ApiRequest<T>> R withPathParameter(PathParameter pathParameter) {
        if (this.pathParameters == null) {
            this.pathParameters = new HashSet<>();
        }
        this.pathParameters.add(pathParameter);
        return (R) this;
    }

    /**
     * Adds a request query to this API request.
     * If request queries were previously set, this method appends the new query to the existing set
     * or creates a new set if none exists.
     *
     * @param requestQuery the request query to add to this API request.
     * @param <R>          the type of the request, extending ApiRequest
     *
     * @return a reference to this ApiRequest, allowing for method chaining.
     */
    public <R extends ApiRequest<T>> R withRequestQuery(RequestQuery requestQuery) {
        if (this.requestQueries == null) {
            this.requestQueries = new HashSet<>();
        }
        this.requestQueries.add(requestQuery);
        return (R) this;
    }

    /**
     * Adds request headers to this API request.
     * If headers were previously set, this method appends the new headers to the existing list
     * or creates a new list if none exists.
     *
     * @param headers the request headers to add to this API request.
     * @param <R>     the type of the request, extending ApiRequest
     *
     * @return a reference to this ApiRequest, allowing for method chaining.
     */
    public <R extends ApiRequest<T>> R withHeaders(RequestHeader... headers) {
        if (this.headers == null) {
            this.headers = new ArrayList<>();
        }
        this.headers.addAll(Arrays.asList(headers));
        return (R) this;
    }

    /**
     * Adds path parameters to this API request.
     * If path parameters were previously set, this method appends the new parameters to the existing set
     * or creates a new set if none exists.
     *
     * @param pathParameters the path parameters to add to this API request.
     * @param <R>            the type of the request, extending ApiRequest
     *
     * @return a reference to this ApiRequest, allowing for method chaining.
     */
    public <R extends ApiRequest<T>> R withPathParameters(PathParameter... pathParameters) {
        if (this.pathParameters == null) {
            this.pathParameters = new HashSet<>();
        }
        this.pathParameters.addAll(Arrays.asList(pathParameters));
        return (R) this;
    }

    /**
     * Adds request queries to this API request.
     * If request queries were previously set, this method appends the new queries to the existing set
     * or creates a new set if none exists.
     *
     * @param requestQueries the request queries to add to this API request.
     * @param <R>            the type of the request, extending ApiRequest
     *
     * @return a reference to this ApiRequest, allowing for method chaining.
     */
    public <R extends ApiRequest<T>> R withRequestQueries(RequestQuery... requestQueries) {
        if (this.requestQueries == null) {
            this.requestQueries = new HashSet<>();
        }
        this.requestQueries.addAll(Arrays.asList(requestQueries));
        return (R) this;
    }


    /**
     * Sets the body of this API request.
     * This method can be used to set the body for requests that require a payload, such as POST or PUT requests.
     *
     * @param body the body to set for this request. If null, no body will be set.
     * @param <R>  the type of the request, extending ApiRequest
     *
     * @return a reference to this ApiRequest, allowing for method chaining.
     */
    public <R extends ApiRequest<T>> R withBody(Object body) {
        this.body = body;
        return (R) this;
    }

    /**
     * Computes the request URL for this API request.
     *
     * @return the constructed request URL as a String.
     */
    public String computeRequestUrl() {
        String constructedEndpoint = endpoint;

        if (pathParameters != null && !pathParameters.isEmpty()) {
            for (PathParameter pathParameter : pathParameters) {
                constructedEndpoint = constructedEndpoint.replace(String.format("{%s}", pathParameter.getKey()),
                    pathParameter.getValue());
            }
        }

        if (requestQueries != null && !requestQueries.isEmpty()) {
            StringBuilder queryString = new StringBuilder("?");
            for (RequestQuery requestQuery : requestQueries) {
                queryString.append(encodeValue(requestQuery.getKey()))
                    .append("=")
                    .append(encodeValue(requestQuery.getValue()))
                    .append("&");
            }
            // Remove the last '&'
            queryString.setLength(queryString.length() - 1);
            constructedEndpoint += queryString.toString();
        }

        final String constructedUrl;

        if (url != null) {
            constructedUrl = url + constructedEndpoint;
        } else {
            // If no URL is present, we use the default API URL
            if (kirara.getApiUrl() == null) {
                throw new IllegalStateException("No API URL set in Kirara or in API Request.");
            } else {
                constructedUrl = kirara.getApiUrl() + encodeValue(constructedEndpoint);
            }
        }

        return constructedUrl;
    }

    /**
     * Encodes a value using UTF-8 encoding for use in URLs.
     *
     * @param value the value to encode
     *
     * @return the encoded value as a String
     */
    protected String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 is not supported!", e);
        }
    }

    /**
     * Invokes {@link HttpCore#send(ApiRequest)} on the current Kirara instance.
     *
     * @return A CompletableFuture that will complete with the response of type T.
     */
    public CompletableFuture<T> send() {
        return kirara.getHttpCore().send(this);
    }
}
