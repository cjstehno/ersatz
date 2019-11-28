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
package com.stehno.ersatz.server.undertow;

import io.undertow.security.idm.*;

import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static io.undertow.util.HexConverter.convertToHexBytes;
import static java.lang.String.format;
import static java.lang.String.valueOf;

/**
 * IdentityManager used by the <code>BasicAuthFeature</code>. The default username is "admin" and the default password is "$3cr3t".
 */
public class SimpleIdentityManager implements IdentityManager {

    private final String username;
    private final String password;

    public SimpleIdentityManager(final String username, final String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public Account verify(final Account account) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Account verify(final String id, final Credential credential) {
        if (credential instanceof PasswordCredential) {
            if (username.equals(id) && password.equals(valueOf(((PasswordCredential) credential).getPassword()))) {
                return new SimpleAccount(id);
            }

        } else if (credential instanceof DigestCredential) {
            try {
                final DigestCredential digestCredential = (DigestCredential) credential;
                final var bytes = digestCredential.getAlgorithm().getMessageDigest().digest((format("%s:%s:%s", username, digestCredential.getRealm(), password)).getBytes());

                if (digestCredential.verifyHA1(convertToHexBytes(bytes))) {
                    return new SimpleAccount(id);
                }

            } catch (NoSuchAlgorithmException e) {
                return null;
            }

        }

        return null;
    }

    @Override
    public Account verify(Credential credential) {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates the encoded credential string required for the BASIC authentication header.
     *
     * @param user the username
     * @param pass the password
     * @return the encoded credential string
     */
    public static String encodedCredential(final String user, final String pass) {
        return "Basic " + Base64.getEncoder().encodeToString((user + ":" + pass).getBytes());
    }
}

