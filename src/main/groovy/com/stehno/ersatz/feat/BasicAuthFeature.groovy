package com.stehno.ersatz.feat

import groovy.transform.CompileStatic
import io.undertow.security.api.AuthenticationMechanism
import io.undertow.security.handlers.AuthenticationCallHandler
import io.undertow.security.handlers.AuthenticationConstraintHandler
import io.undertow.security.handlers.AuthenticationMechanismsHandler
import io.undertow.security.handlers.SecurityInitialHandler
import io.undertow.security.idm.Account
import io.undertow.security.idm.Credential
import io.undertow.security.idm.IdentityManager
import io.undertow.security.impl.BasicAuthenticationMechanism
import io.undertow.server.HttpHandler

import java.security.Principal

import static io.undertow.security.api.AuthenticationMode.PRO_ACTIVE

/**
 * A <code>ServerFeature</code> providing support for HTTP BASIC authentication.
 */
@CompileStatic
class BasicAuthFeature implements ServerFeature {

    // FIXME: needs work

    String realm = 'BasicTesting'
    IdentityManager identityManager = new SimpleIdentityManager()

    @Override
    HttpHandler apply(HttpHandler handler) {
        handler = new AuthenticationCallHandler(handler)
        handler = new AuthenticationConstraintHandler(handler)

        final List<AuthenticationMechanism> mechanisms = Collections.<AuthenticationMechanism> singletonList(new BasicAuthenticationMechanism(realm))
        handler = new AuthenticationMechanismsHandler(handler, mechanisms)
        handler = new SecurityInitialHandler(PRO_ACTIVE, identityManager, handler)

        handler
    }
}

class SimpleIdentityManager implements IdentityManager {

    static final Account DUMMY_ACCOUNT = new Account() {
        @Override
        Principal getPrincipal() {
            new Principal() {
                @Override
                String getName() { 'admin' }
            }
        }

        @Override
        Set<String> getRoles() {
            ['TESTER'] as Set<String>
        }
    }

    @Override
    Account verify(Account account) {
        DUMMY_ACCOUNT
    }

    @Override
    Account verify(String id, Credential credential) {
        DUMMY_ACCOUNT
    }

    @Override
    Account verify(Credential credential) {
        DUMMY_ACCOUNT
    }
}