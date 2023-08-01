/**
 * Copyright (C) 2023 Christopher J. Stehno
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
package io.github.cjstehno.ersatz.examples;

import static io.github.cjstehno.ersatz.cfg.ContentType.IMAGE_JPG;
import static io.github.cjstehno.testthings.rando.NumberRandomizers.aFloat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.cjstehno.ersatz.ErsatzServer;
import io.github.cjstehno.ersatz.junit.SharedErsatzServerExtension;
import io.github.cjstehno.ersatz.util.HttpClientExtension;
import io.github.cjstehno.ersatz.util.HttpClientExtension.Client;
import io.github.cjstehno.testthings.junit.Resource;
import io.github.cjstehno.testthings.junit.ResourcesExtension;
import io.github.cjstehno.testthings.junit.SharedRandomExtension;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * A bit of a contrived example to show how you can use the
 * <a href="https://cjstehno.github.io/test-things/">Test-Things</a> testing library with Ersatz, also written by me.
 */
@ExtendWith({
    // provides a means of pinning randomness
    SharedRandomExtension.class,

    // provides access to resources
    ResourcesExtension.class,

    // provides the server management
    SharedErsatzServerExtension.class,

    // provides a pre-wired test client for ersatz (internal only)
    HttpClientExtension.class
})
public class ErsatzThingsTest {

    private static final String SECRET_HEADER = "X-Secret";

    // loads the image resource as a byte array
    @Resource("/test-image.jpg") private static byte[] IMAGE_CONTENT;

    @Test void things(final ErsatzServer ersatz, final Client client) throws Exception {
        // generates a random secret value
        val secret = aFloat().one().toString();

        ersatz.expectations(expect -> {
            expect.GET("/images/42", req -> {
                req.called();
                req.header(SECRET_HEADER, secret);
                req.responder(res -> {
                    res.body(IMAGE_CONTENT, IMAGE_JPG);
                    res.code(200);
                });
            });
        });

        // make the request
        val response = client.get("/images/42", builder -> builder.header(SECRET_HEADER, secret));
        assertEquals(200, response.code());
        assertEquals(721501, response.body().bytes().length);

        ersatz.assertVerified();
    }
}
