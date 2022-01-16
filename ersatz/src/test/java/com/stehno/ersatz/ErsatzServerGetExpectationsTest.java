/**
 * Copyright (C) 2022 Christopher J. Stehno
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stehno.ersatz;

import com.stehno.ersatz.cfg.ServerConfig;
import com.stehno.ersatz.junit.ErsatzServerExtension;
import com.stehno.ersatz.util.HttpClientExtension;
import okhttp3.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;

import static com.stehno.ersatz.cfg.ContentType.TEXT_PLAIN;
import static com.stehno.ersatz.util.BasicAuth.AUTHORIZATION_HEADER;
import static com.stehno.ersatz.util.BasicAuth.header;
import static com.stehno.ersatz.util.HttpClientExtension.Client.basicAuth;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith({ErsatzServerExtension.class, HttpClientExtension.class})
public class ErsatzServerGetExpectationsTest {

    /*
        FIXME: write tests for:
        - GetExpectations
        - AnyExpectations
        - PostExpectations
        - HeadExpectations
        - PutExpectations
        - DeleteExpectations
        - PatchExpectations
        - OptionsExpectations
        - Trace?

        with
        authentication
        chunking
        https
        multipart

        different content types (encoders/decoders)
     */

    // FIXME: authentication, chunking, contenttypes (on path consumer version)

    private static final String INSECURE_TEXT = "not secure!";
    private static final String SECURE_TEXT = "secure!!";

    private final ErsatzServer server = new ErsatzServer(ServerConfig::https);
    @SuppressWarnings("unused") private HttpClientExtension.Client client;

    @Test @DisplayName("GET with path only")
    void withPath() throws IOException {
        server.expectations(expect -> {
            expect.GET("/something").secure(false).called(1).responds().body(INSECURE_TEXT, TEXT_PLAIN);
            expect.GET("/something").secure().called(1).responds().body(SECURE_TEXT, TEXT_PLAIN);
        });

        assertOkWithString(INSECURE_TEXT, client.get("/something"));
        assertOkWithString(SECURE_TEXT, client.gets("/something"));
        verify();
    }

    @Test @DisplayName("GET with path and consumer")
    void withPathAndConsumer() throws IOException {
        server.expectations(expect -> {
            expect.GET("/something", req -> {
                req.secure(false);
                req.called(1);
                req.responder(res -> res.body(INSECURE_TEXT, TEXT_PLAIN));
            });
            expect.GET("/something", req -> {
                req.secure();
                req.called(1);
                req.responder(res -> res.body(SECURE_TEXT, TEXT_PLAIN));
            });
        });

        assertOkWithString(INSECURE_TEXT, client.get("/something"));
        assertOkWithString(SECURE_TEXT, client.gets("/something"));
        verify();
    }

    @Test @DisplayName("GET with path matcher only")
    void withPathMatcher() throws IOException {
        server.expectations(expect -> {
            expect.GET(startsWith("/loader/")).secure(false).called(1).responds().body(INSECURE_TEXT, TEXT_PLAIN);
            expect.GET(startsWith("/loader/")).secure().called(1).responds().body(SECURE_TEXT, TEXT_PLAIN);
        });

        assertOkWithString(INSECURE_TEXT, client.get("/loader/insecure"));
        assertOkWithString(SECURE_TEXT, client.gets("/loader/secure"));
        verify();
    }

    @Test @DisplayName("GET with path matcher and consumer")
    void withPathMatcherAndConsumer() throws IOException {
        server.expectations(expect -> {
            expect.GET(startsWith("/loader/"), req -> {
                req.secure(false);
                req.called(1);
                req.responder(res -> res.body(INSECURE_TEXT, TEXT_PLAIN));
            });
            expect.GET(startsWith("/loader/"), req -> {
                req.secure();
                req.called(1);
                req.responder(res -> res.body(SECURE_TEXT, TEXT_PLAIN));
            });
        });

        assertOkWithString(INSECURE_TEXT, client.get("/loader/insecure"));
        assertOkWithString(SECURE_TEXT, client.gets("/loader/secure"));
        verify();
    }

    @Test @DisplayName("GET with BASIC authentication")
    void withBASICAuthentication() throws IOException {
        server.expectations(cfg -> {
            cfg.GET("/safe1", req -> {
                req.header(AUTHORIZATION_HEADER, header("basicuser", "ba$icp@$$"));
                req.secure(false);
                req.called(1);
                req.responder(res -> res.body(INSECURE_TEXT, TEXT_PLAIN));
            });
            cfg.GET("/safe2", req -> {
                req.header(AUTHORIZATION_HEADER, header("basicuser", "anotherPa$$"));
                req.secure();
                req.called(1);
                req.responder(res -> res.body(SECURE_TEXT, TEXT_PLAIN));
            });
        });

        assertOkWithString(INSECURE_TEXT, client.get("/safe1", builder -> basicAuth(builder, "basicuser", "ba$icp@$$")));
        assertOkWithString(SECURE_TEXT, client.gets("/safe2", builder -> basicAuth(builder, "basicuser", "anotherPa$$")));
        verify();
    }

    private void verify() {
        assertTrue(server.verify());
    }

    private void assertOkWithString(final String content, final Response response) throws IOException {
        assertEquals(200, response.code());
        assertEquals(content, response.body().string());
    }
}
