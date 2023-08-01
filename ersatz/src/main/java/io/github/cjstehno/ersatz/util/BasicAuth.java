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
package io.github.cjstehno.ersatz.util;

import static io.github.cjstehno.ersatz.util.HttpHeaders.AUTHORIZATION;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Base64.getEncoder;
import static lombok.AccessLevel.PRIVATE;

import io.github.cjstehno.ersatz.cfg.Request;
import lombok.NoArgsConstructor;

/**
 * Helper utility for configuring BASIC request authentication.
 */
@NoArgsConstructor(access = PRIVATE)
public final class BasicAuth {

    // FIXME: add usage to guide (and matcher)

    /**
     * The authorization header name.
     */
    public static final String AUTHORIZATION_HEADER = AUTHORIZATION;

    /**
     * Used to generate the Authorization header value for the given username and password.
     *
     * @param username the username (cannot contain a colon)
     * @param password the password (cannot contain a colon)
     * @return the generated header value
     */
    public static String basicAuthHeaderValue(final String username, final String password) {
        check(username, password);
        return "Basic " + getEncoder().encodeToString((username + ":" + password).getBytes(UTF_8));
    }

    /**
     * Used to generate the Authorization header value for the given username and password.
     *
     * @param username the username (cannot contain a colon)
     * @param password the password (cannot contain a colon)
     * @return the generated header value
     * @deprecated Use <code>basicAuthHeaderValue(String,String)</code> instead.
     */
    @Deprecated
    public static String header(final String username, final String password) {
        return basicAuthHeaderValue(username, password);
    }

    /**
     * Shortcut method for configuring BASIC authentication on a request expectation with the provided username and
     * password.
     *
     * @param request  the request expectation
     * @param userame  the username (cannot contain a colon)
     * @param password the password (cannot contain a colon)
     * @return the request passed in (to allow chaining)
     */
    public static Request basicAuth(final Request request, final String userame, final String password) {
        return request.header(AUTHORIZATION_HEADER, header(userame, password));
    }

    private static void check(final String... values) {
        for (String value : values) {
            if (value.contains(":"))
                throw new IllegalArgumentException("The username and password cannot contain a colon!");
        }
    }
}
