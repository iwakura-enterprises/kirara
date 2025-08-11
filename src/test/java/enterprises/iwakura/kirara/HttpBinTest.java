package enterprises.iwakura.kirara;

import org.junit.jupiter.api.Test;

public class HttpBinTest {

    @Test
    public void testHttpBin() {
        final var api = new HttpBinApi();
        final var response = api.getAnything().send().join();

        assert response != null : "Response should not be null";
        assert response.getUrl() != null : "Response URL should not be null";
    }
}
