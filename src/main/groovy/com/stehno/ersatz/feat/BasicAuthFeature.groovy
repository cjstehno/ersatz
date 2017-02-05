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
package com.stehno.ersatz.feat

import com.stehno.ersatz.ServerFeature
import groovy.transform.CompileStatic
import io.undertow.security.api.AuthenticationMechanism
import io.undertow.security.handlers.AuthenticationCallHandler
import io.undertow.security.handlers.AuthenticationConstraintHandler
import io.undertow.security.handlers.AuthenticationMechanismsHandler
import io.undertow.security.handlers.SecurityInitialHandler
import io.undertow.security.idm.IdentityManager
import io.undertow.security.impl.BasicAuthenticationMechanism
import io.undertow.server.HttpHandler

import static io.undertow.security.api.AuthenticationMode.PRO_ACTIVE

/**
 * A <code>ServerFeature</code> providing support for HTTP BASIC authentication. This will apply BASIC authentication to all requests configured on
 * the server. To enable this feature, configure it on the <code>ErsatzServer</code> or <code>ServerConfig</code> instance:
 *
 * <pre><code>
 * ErsatzServer server = new ErsatzServer({
 *     feature new BasicAuthFeature()
 * })
 * </code></pre>
 */
@CompileStatic
class BasicAuthFeature implements ServerFeature {

    /**
     * The realm to be used. Defaults to "BasicTesting".
     */
    String realm = 'BasicTesting'

    /**
     * The IdentityManager to be used. Defaults to the <code>SimpleIdentityManager</code>.
     */
    IdentityManager identityManager = new SimpleIdentityManager()

    @Override
    HttpHandler apply(final HttpHandler handler) {
        new SecurityInitialHandler(
            PRO_ACTIVE,
            identityManager,
            new AuthenticationMechanismsHandler(
                new AuthenticationConstraintHandler(
                    new AuthenticationCallHandler(handler)
                ),
                Collections.<AuthenticationMechanism> singletonList(new BasicAuthenticationMechanism(realm))
            )
        )
    }
}

