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

import io.undertow.security.idm.PasswordCredential
import spock.lang.Specification
import spock.lang.Unroll

class SimpleIdentityManagerSpec extends Specification {

    private final SimpleIdentityManager manager = new SimpleIdentityManager('testing', 's0meThing')

    def 'encode credential'() {
        expect:
        SimpleIdentityManager.encodedCredential('something', 'interesting') == 'Basic c29tZXRoaW5nOmludGVyZXN0aW5n'
    }

    def 'property access'(){
        expect:
        manager.username == 'testing'

        and:
        manager.password == 's0meThing'
    }

    def 'verify account not supported'() {
        when:
        manager.verify(new SimpleAccount('testing'))

        then:
        thrown(UnsupportedOperationException)
    }

    def 'verify credentials-only not supported'() {
        when:
        manager.verify(new PasswordCredential('s0meThing'.toCharArray()))

        then:
        thrown(UnsupportedOperationException)
    }

    @Unroll 'verify id and credential (#credential)'() {
        expect:
        manager.verify(userid, credential) == account

        where:
        userid    | credential                                        || account
        'testing' | null                                              || null
        'testing' | new PasswordCredential('s0meThing'.toCharArray()) || new SimpleAccount('testing')
        'testing' | new PasswordCredential('other'.toCharArray())     || null
        'nothing' | new PasswordCredential('s0meThing'.toCharArray()) || null
    }
}
