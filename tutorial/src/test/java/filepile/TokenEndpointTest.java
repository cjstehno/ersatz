package filepile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cjstehno.ersatz.ErsatzServer;
import io.github.cjstehno.ersatz.cfg.ServerConfig;
import io.github.cjstehno.ersatz.junit.ApplyServerConfig;
import io.github.cjstehno.ersatz.junit.SharedErsatzServerExtension;
import io.github.cjstehno.testthings.rando.Randomizer;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static io.github.cjstehno.ersatz.cfg.ContentType.APPLICATION_JSON;
import static io.github.cjstehno.ersatz.encdec.Encoders.text;
import static io.github.cjstehno.ersatz.util.BasicAuth.AUTHORIZATION_HEADER;
import static io.github.cjstehno.ersatz.util.BasicAuth.header;
import static io.github.cjstehno.testthings.rando.CoreRandomizers.constant;
import static io.github.cjstehno.testthings.rando.NumberRandomizers.aLongBetween;
import static io.github.cjstehno.testthings.rando.StringRandomizers.alphabetic;
import static io.github.cjstehno.testthings.rando.StringRandomizers.alphanumeric;
import static java.lang.System.currentTimeMillis;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SharedErsatzServerExtension.class) @ApplyServerConfig
class TokenEndpointTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    private final Randomizer<Token> tokenRando = () -> {
        val now = currentTimeMillis();
        return new Token(
            alphanumeric(constant(12)).one(),
            aLongBetween(now + 1000, now + 60000).one()
        );
    };

    private FilepileClient client;

    @SuppressWarnings("unused")
    private static void serverConfig(final ServerConfig cfg) {
        cfg.https();
        cfg.encoder(APPLICATION_JSON, Token.class, obj -> {
            try {
                return mapper.writeValueAsBytes(obj);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        cfg.encoder(APPLICATION_JSON, String.class, text);
    }

    @BeforeEach void beforeEach(final ErsatzServer server) {
        client = new FilepileClient(mapper, server.getHttpsUrl());
    }

    @Test @DisplayName("Requesting a token with valid credentials should succeed")
    void tokenWithValidCredentials(final ErsatzServer server) throws Exception {
        val generatedToken = tokenRando.one();
        val username = alphabetic(constant(6)).one();
        val password = alphanumeric(constant(12)).one();

        server.expectations(expect -> {
            expect.POST("/api/token", req -> {
                req.secure();
                req.called();

                req.header(AUTHORIZATION_HEADER, header(username, password));

                req.responder(res -> {
                    res.code(200);
                    res.body(generatedToken, APPLICATION_JSON);
                });
            });
        });

        assertEquals(generatedToken, client.token(username, password));

        server.assertVerified();
    }

    @ParameterizedTest @DisplayName("Requesting a token responds with non-200")
    @CsvSource({
        "401,Unable to retrieve token.",
        "500,Something horrible has happened.",
    })
    void tokenErrorResponse(final int code, final String message, final ErsatzServer server){
        val username = alphabetic(constant(6)).one();
        val password = alphanumeric(constant(12)).one();

        server.expectations(expect -> {
            expect.POST("/api/token", req -> {
                req.secure();
                req.called();

                req.header(AUTHORIZATION_HEADER, header(username, password));

                req.responder(res -> {
                    res.code(code);
                    res.body("{\"message\": \"" + message + "\"}", APPLICATION_JSON);
                });
            });
        });

        val thrown = assertThrows(FilepileClientException.class, () -> {
            client.token(username, password);
        });
        assertEquals(code, thrown.getCode());
        assertEquals(message, thrown.getMessage());

        server.assertVerified();
    }

    // FIXME: test non-https error
}