package com.stehno.ersatz.impl

import com.stehno.ersatz.HttpMethod
import com.stehno.ersatz.Request
import io.undertow.server.handlers.CookieImpl
import io.undertow.util.HeaderMap
import spock.lang.Specification

import static io.undertow.util.HttpString.tryFromString

class UnmatchedRequestReportSpec extends Specification {

    def 'unmatched report'() {
        setup:
        HeaderMap headers = new HeaderMap()
        headers.add(tryFromString('alpha'), 'bravo-1')
        headers.add(tryFromString('alpha'), 'bravo-2')
        headers.add(tryFromString('charlie'), 'delta')

        MockClientRequest request = new MockClientRequest(
            method: HttpMethod.GET,
            protocol: 'HTTP',
            path: '/alpha/foo',
            headers: headers,
            contentLength: 1234,
            contentType: 'text/plain',
            characterEncoding: 'UTF-8',
            body: 'This is some text content'.bytes,
            cookies: [ident: new CookieImpl('ident', 'asdfasdfasdf')]
        )
        request.query('selected', 'one', 'two')
        request.query('id', '1002')

        List<Request> expectations = [
            GroovyMock(Request) { r ->

            }
        ]

        when:
        String string = new UnmatchedRequestReport(request, expectations).toString()

        then:
        println string
    }
}
