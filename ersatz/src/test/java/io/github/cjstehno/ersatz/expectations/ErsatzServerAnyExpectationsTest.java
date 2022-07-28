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
package io.github.cjstehno.ersatz.expectations;

import io.github.cjstehno.ersatz.ErsatzServer;
import io.github.cjstehno.ersatz.junit.ErsatzServerExtension;
import io.github.cjstehno.ersatz.util.HttpClientExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;

import static io.github.cjstehno.ersatz.TestAssertions.assertOkWithString;
import static io.github.cjstehno.ersatz.TestAssertions.verify;
import static io.github.cjstehno.ersatz.cfg.ContentType.TEXT_PLAIN;
import static okhttp3.MediaType.parse;
import static okhttp3.RequestBody.create;
import static org.hamcrest.Matchers.startsWith;

@ExtendWith({ErsatzServerExtension.class, HttpClientExtension.class})
public class ErsatzServerAnyExpectationsTest {

    // FIXME: do some testing with ANY as with/out content

    private final ErsatzServer server = new ErsatzServer(cfg -> {
        cfg.https();
    });
    @SuppressWarnings("unused") private HttpClientExtension.Client client;

    @ParameterizedTest(name = "[{index}] path only: https({0}) -> {1}")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttpsWithContent")
    void withPath(final boolean https, final String responseText) throws IOException {
        server.expectations(expect -> {
            expect.ANY("/something").secure(https).called(2).responds().body(responseText, TEXT_PLAIN);
        });

        assertOkWithString(responseText, client.get("/something", https));
        assertOkWithString(responseText, client.post("/something", create("", parse(TEXT_PLAIN.getValue())), https));
        verify(server);
    }

    @ParameterizedTest(name = "[{index}] path and consumer: https({0}) -> {1}")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttpsWithContent")
    void withPathAndConsumer(final boolean https, final String responseText) throws IOException {
        server.expectations(expect -> {
            expect.ANY("/something", req -> {
                req.secure(https);
                req.called(2);
                req.responder(res -> res.body(responseText, TEXT_PLAIN));
            });
        });

        assertOkWithString(responseText, client.get("/something", https));
        assertOkWithString(responseText, client.post("/something", create("", parse(TEXT_PLAIN.getValue())), https));
        verify(server);
    }

    @ParameterizedTest(name = "[{index}] path matcher: https({0}) -> {1}")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttpsWithContent")
    void withPathMatcher(final boolean https, final String responseText) throws IOException {
        server.expectations(expect -> {
            expect.ANY(startsWith("/loader/")).secure(https).called(2).responds().body(responseText, TEXT_PLAIN);
        });

        assertOkWithString(responseText, client.get("/loader/insecure", https));
        assertOkWithString(responseText, client.post("/loader/insecure", create("", parse(TEXT_PLAIN.getValue())), https));
        verify(server);
    }

    @ParameterizedTest(name = "[{index}] path matcher and consumer: https({0}) -> {1}")
    @MethodSource("io.github.cjstehno.ersatz.TestArguments#httpAndHttpsWithContent")
    void withPathMatcherAndConsumer(final boolean https, final String responseText) throws IOException {
        server.expectations(expect -> {
            expect.ANY(startsWith("/loader/"), req -> {
                req.secure(https);
                req.called(2);
                req.responder(res -> res.body(responseText, TEXT_PLAIN));
            });
        });

        assertOkWithString(responseText, client.get("/loader/insecure", https));
        assertOkWithString(responseText, client.post("/loader/insecure", create("", parse(TEXT_PLAIN.getValue())), https));
        verify(server);
    }
}
