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
package com.stehno.ersatz.cfg;

/**
 * Configuration object for BASIC and DIGEST authentication support. If the username or password are unspecified or null, they will be "admin" and
 * "$3cr3t" respectively.
 * <p>
 * Only one of BASIC or DIGEST may be specified (last one called wins).
 * <p>
 * Enabling authentication causes ALL server endpoints to require the configured authentication.
 */
public interface AuthenticationConfig {

    String DEFAULT_USERNAME = "admin";
    String DEFAULT_PASSWORD = "$3cr3t";

    /**
     * Configures BASIC authentication support.
     *
     * @param username the username or null to use the default
     * @param password the password or null to use the default
     */
    void basic(final String username, final String password);

   default void basic(final String username){
       basic(username, DEFAULT_PASSWORD);
   }

    default void basic(){
       basic(DEFAULT_USERNAME, DEFAULT_PASSWORD);
    }

    /**
     * Configures DIGEST authentication support.
     *
     * @param username the username or null to use the default
     * @param password the password or null to use the default
     */
    void digest(final String username, final String password);

    default void digest(final String username){
        digest(username, DEFAULT_USERNAME);
    }

    default void digest(){
        digest(DEFAULT_USERNAME, DEFAULT_PASSWORD);
    }
}
