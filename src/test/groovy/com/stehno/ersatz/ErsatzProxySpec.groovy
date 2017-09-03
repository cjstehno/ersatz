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
package com.stehno.ersatz

import spock.lang.Specification

import static com.stehno.ersatz.ContentType.TEXT_PLAIN

class ErsatzProxySpec extends Specification {

    def 'http proxy'() {
        setup:
        ErsatzServer ersatzServer = new ErsatzServer({
            autoStart()
            expectations {
                get('/').called(1).responds().code(200).content('Hello', TEXT_PLAIN)
                get('/foo').called(1).responds().code(200).content('Foo!', TEXT_PLAIN)
            }
        })

        ErsatzProxy ersatzProxy = new ErsatzProxy(ersatzServer.httpUrl)
        ersatzProxy.start()

        when:
        String text = "${ersatzProxy.url}".toURL().text

        then:
        text == 'Hello'

        when:
        text = "${ersatzProxy.url}/foo".toURL().text

        then:
        text == 'Foo!'

        and:
        ersatzServer.verify()

        cleanup:
        ersatzProxy.stop()
        ersatzServer.stop()
    }
}
