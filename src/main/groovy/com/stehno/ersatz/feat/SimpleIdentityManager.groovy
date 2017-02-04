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

import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import io.undertow.security.idm.*
import io.undertow.util.HexConverter

import java.security.Principal

import static io.undertow.util.HexConverter.convertToHexBytes

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
        if (credential instanceof PasswordCredential) {
            if (username == id && password == String.valueOf(((PasswordCredential) credential).password)) {
                return new SimpleAccount(id)
            }

        } else if (credential instanceof DigestCredential) {
            DigestCredential digestCredential = credential as DigestCredential

            byte[] bytes = digestCredential.algorithm.messageDigest.digest("${username}:${digestCredential.realm}:${password}".toString().bytes)

            if (digestCredential.verifyHA1(convertToHexBytes(bytes))) {
                return new SimpleAccount(id)
            }
        }

        return null
    }

    @Override
    Account verify(Credential credential) {
        throw new UnsupportedOperationException()
    }

    static String encodedCredential(final String user, final String pass) {
        "Basic ${("$user:$pass".bytes.encodeBase64() as String)}"
    }
}

@CompileStatic
class SimpleAccount implements Account {

    private final Principal principal

    SimpleAccount(final String user) {
        principal = new Principal() {
            @Override String getName() { user }
        }
    }

    @Override
    Principal getPrincipal() {
        principal
    }

    @Override
    Set<String> getRoles() {
        ['TESTER'] as Set<String>
    }
}
