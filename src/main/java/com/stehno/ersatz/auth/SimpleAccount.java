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
package com.stehno.ersatz.auth;

import io.undertow.security.idm.Account;

import java.security.Principal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.Objects.hash;

/**
 * Simple implementation of the <code>Account</code> interface used for BASIC and DIGEST authentication testing.
 */
public class SimpleAccount implements Account {

    private final Set<String> roles = new HashSet<>(singletonList("TESTER"));
    private final String user;
    private final Principal principal;

    public SimpleAccount(final String user) {
        this.user = user;
        this.principal = () -> user;
    }

    public String getUser() {
        return user;
    }

    @Override public Principal getPrincipal() {
        return principal;
    }

    @Override public Set<String> getRoles() {
        return roles;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleAccount that = (SimpleAccount) o;
        return Objects.equals(roles, that.roles) &&
            Objects.equals(user, that.user) &&
            Objects.equals(principal, that.principal);
    }

    @Override public int hashCode() {
        return hash(roles, user, principal);
    }

    @Override public String toString() {
        return format("SimpleAccount{user='%s'}", user);
    }
}
