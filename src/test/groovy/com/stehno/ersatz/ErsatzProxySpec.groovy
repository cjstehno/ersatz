package com.stehno.ersatz

import spock.lang.Specification

import static com.stehno.ersatz.ContentType.TEXT_PLAIN

class ErsatzProxySpec extends Specification {

    def 'proxy'() {
        setup:
        ErsatzServer ersatzServer = new ErsatzServer({
            autoStart()
            expectations {
                get('/').called(1).responds().code(200).content('Hello', TEXT_PLAIN)
                get('/foo').called(1).responds().code(200).content('Foo!', TEXT_PLAIN)
            }
        })

        ErsatzProxy ersatzProxy = new ErsatzProxy(target: ersatzServer.httpUrl)
        ersatzProxy.start()

        when:
        String text = "${ersatzProxy.httpUrl}".toURL().text

        then:
        text == 'Hello'

        when:
        text = "${ersatzProxy.httpUrl}/foo".toURL().text

        then:
        text == 'Foo!'

        and:
        ersatzServer.verify()

        cleanup:
        ersatzProxy.stop()
        ersatzServer.stop()
    }
}
