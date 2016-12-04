package com.stehno.ersatz.feat

import groovy.transform.TupleConstructor
import io.undertow.security.idm.Account
import io.undertow.security.idm.Credential
import io.undertow.security.idm.IdentityManager
import io.undertow.security.idm.PasswordCredential

import java.security.Principal

/**
 * Created by cjstehno on 12/4/16.
 */
@TupleConstructor
class SimpleIdentityManager implements IdentityManager {

    String username = 'admin'
    String password = '$3cr3t'

    @Override
    Account verify(Account account) {
        throw new UnsupportedOperationException()
    }

    @Override
    Account verify(final String id, final Credential credential) {
        if (username == id && password == String.valueOf(((PasswordCredential) credential).password)) {
            return new Account() {
                @Override
                Principal getPrincipal() {
                    new Principal() {
                        @Override String getName() { id }
                    }
                }

                @Override
                Set<String> getRoles() {
                    ['TESTER'] as Set<String>
                }
            }
        }

        return null
    }

    @Override
    Account verify(Credential credential) {
        throw new UnsupportedOperationException()
    }
}
