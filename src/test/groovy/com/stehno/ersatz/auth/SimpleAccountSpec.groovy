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

import spock.lang.Specification

import static com.stehno.vanilla.test.Assertions.assertValidEqualsAndHashcode

class SimpleAccountSpec extends Specification {

    def 'empty constructor'() {
        expect:
        account.user == user

        and:
        account.roles == roles

        and:
        account.principal.name == user

        where:
        label          | account                                                 | user       | roles
        'empty'        | new SimpleAccount()                                     | null       | ['TESTER'] as Set<String>
        'user'         | new SimpleAccount('somebody')                           | 'somebody' | ['TESTER'] as Set<String>
        'user & roles' | new SimpleAccount('somebody', ['OTHER'] as Set<String>) | 'somebody' | ['OTHER'] as Set<String>
    }

    def 'as string'() {
        expect:
        new SimpleAccount('someuser') as String == 'com.stehno.ersatz.auth.SimpleAccount(someuser, [TESTER])'
    }

    def 'equals and hashCode'() {
        expect:
        assertValidEqualsAndHashcode(
            new SimpleAccount('someuser', ['ADMIN'] as Set<String>),
            new SimpleAccount('someuser', ['ADMIN'] as Set<String>),
            new SimpleAccount('someuser', ['ADMIN'] as Set<String>),
            new SimpleAccount('other', ['GUEST'] as Set<String>)
        )
    }
}
