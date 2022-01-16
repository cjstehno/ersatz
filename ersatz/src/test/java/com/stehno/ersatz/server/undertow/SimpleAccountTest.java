/**
 * Copyright (C) 2022 Christopher J. Stehno
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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class SimpleAccountTest {

    @ParameterizedTest @MethodSource("provider") @DisplayName("empty constructor")
    void emptyCtor(final String label, final SimpleAccount account, final String user, final Set<String> roles) {
        assertEquals(user, account.getUser());
        assertEquals(roles, account.getRoles());
        assertEquals(user, account.getPrincipal().getName());
    }

    private static Stream<Arguments> provider() {
        return Stream.of(
            arguments("empty", new SimpleAccount(), null, Set.of("TESTER")),
            arguments("user", new SimpleAccount("somebody"), "somebody", Set.of("TESTER")),
            arguments("user & roles", new SimpleAccount("somebody", Set.of("OTHER")), "somebody", Set.of("OTHER"))
        );
    }

    @Test @DisplayName("as string")
    void asString() {
        assertEquals("SimpleAccount{user='someuser', roles=[TESTER]}", new SimpleAccount("someuser").toString());
    }
}
