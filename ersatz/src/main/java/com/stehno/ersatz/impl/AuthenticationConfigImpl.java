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
package com.stehno.ersatz.impl;

import com.stehno.ersatz.cfg.Authentication;
import com.stehno.ersatz.cfg.AuthenticationConfig;

/**
 * Default implementation of the <code>AuthenticationConfig</code> interface.
 */
public class AuthenticationConfigImpl implements AuthenticationConfig {

    private String username = DEFAULT_USERNAME;
    private String password = DEFAULT_PASSWORD;
    private Authentication type;

    /**
     * Configures BASIC authentication support.
     *
     * @param username the username or null to use the default
     * @param password the password or null to use the default
     */
    public void basic(final String username, final String password) {
        spec(Authentication.BASIC, username, password);
    }

    /**
     * Configures DIGEST authentication support.
     *
     * @param username the username or null to use the default
     * @param password the password or null to use the default
     */
    public void digest(final String username, final String password) {
        spec(Authentication.DIGEST, username, password);
    }

    /**
     * Used to retrieve the configured username (defaults to "admin").
     *
     * @return the configured username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Used to retrieve the configured password (defaults to "$3cr3t").
     *
     * @return the configured password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Used to retrieve the authentication type.
     *
     * @return the configured authentication type
     */
    public Authentication getType() {
        return type;
    }

    private void spec(final Authentication type, final String username, final String password) {
        this.type = type;
        if (username != null) {
            this.username = username;
        }
        if (password != null) {
            this.password = password;
        }
    }
}
