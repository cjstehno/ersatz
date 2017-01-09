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

import groovy.transform.TupleConstructor
import io.undertow.security.idm.Account
import io.undertow.security.idm.Credential
import io.undertow.security.idm.IdentityManager
import io.undertow.security.idm.PasswordCredential

import java.security.Principal

/**
 * IdentityManager used by the <code>BasicAuthFeature</code>. The default username is "admin" and the default password is "$3cr3t".
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

    static String encodedCredential(final String user, final String pass) {
        String encoded = "$user:$pass".bytes.encodeBase64()
        "Basic $encoded"
    }
}
