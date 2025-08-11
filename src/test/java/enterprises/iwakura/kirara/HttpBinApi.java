package enterprises.iwakura.kirara;

import com.google.gson.Gson;
import enterprises.iwakura.kirara.core.ApiRequest;
import enterprises.iwakura.kirara.core.Kirara;
import enterprises.iwakura.kirara.gson.GsonSerializer;
import enterprises.iwakura.kirara.httpclient.HttpClientHttpCore;

public class HttpBinApi extends Kirara {

    public HttpBinApi() {
        super(new HttpClientHttpCore(), new GsonSerializer(new Gson()));
    }

    @Override
    public String getApiUrl() {
        return "https://httpbin.org";
    }

    public ApiRequest<HttpBinResponse> getAnything() {
        return this.createRequest("GET", "/anything", HttpBinResponse.class)
                .withExplicitHeaders(this.defaultRequestHeaders);
    }
}
