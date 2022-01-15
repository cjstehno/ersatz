/*
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
package com.stehno.ersatz.server.undertow;

import io.undertow.security.idm.PasswordCredential;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class SimpleIdentityManagerTest {

    private SimpleIdentityManager manager;

    @BeforeEach void beforeEach() {
        manager = new SimpleIdentityManager("testing", "s0meThing");
    }

    @Test @DisplayName("encode credentials")
    void encodeCreds() {
        assertEquals("Basic c29tZXRoaW5nOmludGVyZXN0aW5n", SimpleIdentityManager.encodedCredential("something", "interesting"));
    }

    @Test @DisplayName("verify account not supported")
    void verifyAccountNotSupported() {
        assertThrows(UnsupportedOperationException.class, () -> {
            manager.verify(new SimpleAccount("testing"));
        });
    }

    @Test @DisplayName("verify credentials-only not supported")
    void verifyCredsOnlyNotSupported() {
        assertThrows(UnsupportedOperationException.class, () -> {
            manager.verify(new PasswordCredential("s0meThing".toCharArray()));
        });
    }

    @ParameterizedTest @DisplayName("verify id and credential (#credential)") @MethodSource("provider")
    void verifyIdAndCreds(final String userid, PasswordCredential credential, final SimpleAccount account) {
        assertEquals(account, manager.verify(userid, credential));
    }

    private static Stream<Arguments> provider() {
        return Stream.of(
            arguments("testing", null, null),
            arguments("testing", new PasswordCredential("s0meThing".toCharArray()), new SimpleAccount("testing")),
            arguments("testing", new PasswordCredential("other".toCharArray()), null),
            arguments("nothing", new PasswordCredential("s0meThing".toCharArray()), null)
        );
    }
}
