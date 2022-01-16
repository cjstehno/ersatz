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

import lombok.val;

import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

public interface BasicAuth {

    String AUTHORIZATION_HEADER = "Authorization";

    static String header(final String username, final String password) {
        check(username, password);
        val userColonPass = username + ":" + password;
        val bytes = userColonPass.getBytes(UTF_8);
        val encoded = Base64.getEncoder().encodeToString(bytes);
        return "Basic " + encoded;
    }

    private static void check(final String... values) {
        for (String value : values) {
            if (value.contains(":"))
                throw new IllegalArgumentException("The username and password cannot contain a colon!");
        }
    }
}
