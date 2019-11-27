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
package com.stehno.ersatz;

/**
 * Configuration object for BASIC and DIGEST authentication support. If the username or password are unspecified or null, they will be "admin" and
 * "$3cr3t" respectively.
 * <p>
 * Only one of BASIC or DIGEST may be specified (last one called wins).
 * <p>
 * Enabling authentication causes ALL server endpoints to require the configured authentication.
 */
public class AuthenticationConfig {

    private String username = "admin";
    private String password = "$3cr3t";
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

    public void basic(final String username) {
        basic(username, null);
    }

    public void basic() {
        basic(null, null);
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

    public void digest(final String username) {
        digest(username, null);
    }

    public void digest() {
        digest(null, null);
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

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
