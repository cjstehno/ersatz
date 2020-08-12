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
package com.stehno.ersatz.server.undertow

import com.stehno.ersatz.server.undertow.SimpleAccount
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

import java.util.stream.Stream

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.params.provider.Arguments.arguments

class SimpleAccountTest {

    @ParameterizedTest @MethodSource('provider') @DisplayName('empty constructor')
    void emptyCtor(final String label, final SimpleAccount account, final String user, final Set<String> roles) {
        assertEquals user, account.user

        assertEquals roles, account.roles

        assertEquals user, account.principal.name
    }

    private static Stream<Arguments> provider() {
        Stream.of(
            arguments('empty', new SimpleAccount(), null, ['TESTER'] as Set<String>),
            arguments('user', new SimpleAccount('somebody'), 'somebody', ['TESTER'] as Set<String>),
            arguments('user & roles', new SimpleAccount('somebody', ['OTHER'] as Set<String>), 'somebody', ['OTHER'] as Set<String>)
        )
    }

    @Test @DisplayName('as string')
    void asString() {
        assertEquals 'SimpleAccount{user=\'someuser\', roles=[TESTER]}', new SimpleAccount('someuser') as String
    }
}
