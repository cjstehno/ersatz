/*
 * Copyright (C) 2019 Christopher J. Stehno
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
package com.stehno.ersatz

/**
 * Configuration object for BASIC and DIGEST authentication support. If the username or password are unspecified or null, they will be "admin" and
 * "$3cr3t" respectively.
 *
 * Only one of BASIC or DIGEST may be specified (last one called wins).
 *
 * Enabling authentication causes ALL server endpoints to require the configured authentication.
 */
class AuthenticationConfig {

    /**
     * The configured username. Defaults to "admin".
     */
    String username = 'admin'

    /**
     * The configured password. Defaults to "$3cr3t".
     */
    String password = '$3cr3t'

    /**
     * The configured authentication type.
     */
    Authentication type

    /**
     * Configures BASIC authentication support.
     *
     * @param username the username or null to use the default
     * @param password the password or null to use the default
     */
    void basic(final String username = null, final String password = null) {
        spec Authentication.BASIC, username, password
    }

    /**
     * Configures DIGEST authentication support.
     *
     * @param username the username or null to use the default
     * @param password the password or null to use the default
     */
    void digest(final String username = null, final String password = null) {
        spec Authentication.DIGEST, username, password
    }

    private void spec(final Authentication type, final String username, final String password) {
        this.type = type
        if (username != null) {
            this.username = username
        }
        if (password != null) {
            this.password = password
        }
    }
}
