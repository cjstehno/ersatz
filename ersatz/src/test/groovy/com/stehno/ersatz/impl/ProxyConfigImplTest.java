/**
 * Copyright (C) 2020 Christopher J. Stehno
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ProxyConfigImplTest {

    // TODO: need to test the groovy closure

    private ProxyConfigImpl config;

    @BeforeEach void beforeEach() {
        this.config = new ProxyConfigImpl();
    }

    @Test @DisplayName("configuration (string)")
    void config_string() throws URISyntaxException {
        config.autoStart(false).target("http://localhost/foo").expectations(exp -> {
            exp.ANY("/");
        });

        assertFalse(config.isAutoStart());
        assertEquals(new URI("http://localhost/foo"), config.getTargetUri());
        assertEquals(1, config.getExpectations().getMatchers().size());
    }

    @Test @DisplayName("configuration (URI)")
    void config_uri() throws URISyntaxException {
        config.autoStart(false).target(new URI("http://localhost/foo")).expectations(exp -> {
            exp.ANY("/");
        });

        assertFalse(config.isAutoStart());
        assertEquals(new URI("http://localhost/foo"), config.getTargetUri());
        assertEquals(1, config.getExpectations().getMatchers().size());
    }

    @Test @DisplayName("configuration (closure)")
    void config_url() throws Exception {
        config.autoStart(false).target(new URL("http://localhost/foo")).expectations(exp -> {
            exp.ANY("/");
        });

        assertFalse(config.isAutoStart());
        assertEquals(new URI("http://localhost/foo"), config.getTargetUri());
        assertEquals(1, config.getExpectations().getMatchers().size());
    }
}
