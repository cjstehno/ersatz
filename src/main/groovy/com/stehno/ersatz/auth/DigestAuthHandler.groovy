/*
 * Copyright (C) 2017 Christopher J. Stehno
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
package com.stehno.ersatz.auth

import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import io.undertow.security.api.AuthenticationMechanism
import io.undertow.security.handlers.AuthenticationCallHandler
import io.undertow.security.handlers.AuthenticationConstraintHandler
import io.undertow.security.handlers.AuthenticationMechanismsHandler
import io.undertow.security.handlers.SecurityInitialHandler
import io.undertow.security.idm.IdentityManager
import io.undertow.security.impl.DigestAuthenticationMechanism
import io.undertow.server.HttpHandler

import static io.undertow.security.api.AuthenticationMode.PRO_ACTIVE

/**
 * Server handler factory for DIGEST authentication. See <code>ServerConfig</code> for authentication configuration details.
 */
@CompileStatic @TupleConstructor
class DigestAuthHandler {

    /**
     * The IdentityManager to be used.
     */
    final IdentityManager identityManager

    /**
     * The realm to be used. Defaults to "DigestTesting".
     */
    private final String realm = 'DigestTesting'

    HttpHandler apply(final HttpHandler handler) {
        new SecurityInitialHandler(
            PRO_ACTIVE,
            identityManager,
            new AuthenticationMechanismsHandler(
                new AuthenticationConstraintHandler(
                    new AuthenticationCallHandler(handler)
                ),
                Collections.<AuthenticationMechanism> singletonList(new DigestAuthenticationMechanism(realm, 'localhost', 'DIGEST', identityManager))
            )
        )
    }
}
