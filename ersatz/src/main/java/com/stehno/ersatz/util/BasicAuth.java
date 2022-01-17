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
package com.stehno.ersatz.util;

import com.stehno.ersatz.cfg.Request;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Base64.getEncoder;

/**
 * Helper utility for configuring BASIC request authentication.
 */
public interface BasicAuth {

    /**
     * The authorization header name.
     */
    String AUTHORIZATION_HEADER = "Authorization";

    /**
     * Used to generate the Authorization header value for the given username and password.
     *
     * @param username the username (cannot contain a colon)
     * @param password the password (cannot contain a colon)
     * @return the generated header value
     */
    static String header(final String username, final String password) {
        check(username, password);
        return "Basic " + getEncoder().encodeToString((username + ":" + password).getBytes(UTF_8));
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
    static Request basicAuth(final Request request, final String userame, final String password) {
        return request.header(AUTHORIZATION_HEADER, header(userame, password));
    }

    private static void check(final String... values) {
        for (String value : values) {
            if (value.contains(":"))
                throw new IllegalArgumentException("The username and password cannot contain a colon!");
        }
    }
}
