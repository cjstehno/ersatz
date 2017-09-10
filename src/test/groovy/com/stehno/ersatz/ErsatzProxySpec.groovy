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

import com.stehno.ersatz.proxy.ErsatzProxy
import com.stehno.ersatz.proxy.ProxyConfig
import com.stehno.ersatz.proxy.ProxyExpectations
import spock.lang.Specification

import java.util.function.Consumer

import static com.stehno.ersatz.ContentType.TEXT_PLAIN

class ErsatzProxySpec extends Specification {

    def 'http proxy (closure)'() {
        setup:
        ErsatzServer ersatzServer = new ErsatzServer({
            expectations {
                get('/').called(1).responds().code(200).content('Hello', TEXT_PLAIN)
                get('/foo').called(1).responds().code(200).content('Foo!', TEXT_PLAIN)
            }
        })

        ErsatzProxy ersatzProxy = new ErsatzProxy({
            target ersatzServer.httpUrl
            expectations {
                get '/'
                get '/foo'
            }
        })

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
        ersatzProxy.verify()

        cleanup:
        ersatzProxy.stop()
        ersatzServer.stop()
    }

    def 'http proxy (consumer)'() {
        setup:
        ErsatzServer ersatzServer = new ErsatzServer({
            expectations {
                get('/').called(1).responds().code(200).content('Hello', TEXT_PLAIN)
                get('/foo').called(1).responds().code(200).content('Foo!', TEXT_PLAIN)
            }
        })

        ErsatzProxy ersatzProxy = new ErsatzProxy(new Consumer<ProxyConfig>() {
            @Override void accept(final ProxyConfig config) {
                config.target ersatzServer.httpUrl
                config.expectations(new Consumer<ProxyExpectations>() {
                    @Override void accept(final ProxyExpectations expect) {
                        expect.get '/'
                        expect.get '/foo'
                    }
                })
            }
        })

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
        ersatzProxy.verify()

        cleanup:
        ersatzProxy.stop()
        ersatzServer.stop()
    }
}
