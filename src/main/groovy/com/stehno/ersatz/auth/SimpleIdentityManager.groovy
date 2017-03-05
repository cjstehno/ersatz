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
import io.undertow.security.idm.*

import static io.undertow.util.HexConverter.convertToHexBytes
import static java.lang.String.valueOf

/**
 * IdentityManager used by the <code>BasicAuthFeature</code>. The default username is "admin" and the default password is "$3cr3t".
 */
@CompileStatic
class SimpleIdentityManager implements IdentityManager {

    final String username
    final String password

    SimpleIdentityManager(final String username, final String password) {
        this.username = username
        this.password = password
    }

    @Override
    Account verify(final Account account) {
        throw new UnsupportedOperationException()
    }

    @Override
    Account verify(final String id, final Credential credential) {
        if (credential instanceof PasswordCredential) {
            if (username == id && password == valueOf(((PasswordCredential) credential).password)) {
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

    /**
     * Creates the encoded credential string required for the BASIC authentication header.
     *
     * @param user the username
     * @param pass the password
     * @return the encoded credential string
     */
    static String encodedCredential(final String user, final String pass) {
        "Basic ${("$user:$pass".bytes.encodeBase64() as String)}"
    }
}

