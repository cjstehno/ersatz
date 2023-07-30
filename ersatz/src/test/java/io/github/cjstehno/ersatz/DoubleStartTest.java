package io.github.cjstehno.ersatz;

import io.github.cjstehno.ersatz.cfg.ServerConfig;
import io.github.cjstehno.ersatz.junit.ApplyServerConfig;
import io.github.cjstehno.ersatz.junit.ErsatzServerExtension;
import io.github.cjstehno.ersatz.util.HttpClientExtension;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;

import static io.github.cjstehno.ersatz.cfg.ContentType.TEXT_PLAIN;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This test is to verify issue <a href="https://github.com/cjstehno/ersatz/issues/182">#182</a>.
 */
@ExtendWith({ErsatzServerExtension.class, HttpClientExtension.class})
@ApplyServerConfig
public class DoubleStartTest {

    @SuppressWarnings("unused") private void serverConfig(final ServerConfig cfg) {
        cfg.expectations(expects -> {
            expects.GET("/init", req -> {
                req.called();
                req.responds().code(200);
            });
        });
    }

    @Test void moreExpectations(final ErsatzServer server, final HttpClientExtension.Client http) throws IOException {
        server.expectations(expects -> {
            expects.GET("/stuff", req -> {
                req.called().responds().code(200).body("things", TEXT_PLAIN);
            });
        });

        assertEquals(200, http.get("/init").code());

        val stuffResponse = http.get("/stuff");
        assertEquals(200, stuffResponse.code());
        assertEquals("things", stuffResponse.body().string());

        server.assertVerified();
    }
}
