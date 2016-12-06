package com.stehno.ersatz.impl

import com.stehno.ersatz.ErsatzServer
import com.stehno.ersatz.InMemoryCookieJar
import groovy.json.JsonSlurper
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import spock.lang.AutoCleanup
import spock.lang.Specification

import static com.stehno.ersatz.ErsatzServer.NOT_FOUND_BODY
import static okhttp3.MediaType.parse
import static okhttp3.Request.Builder
import static okhttp3.RequestBody.create

class ErsatzRequestWithContentSpec extends Specification {

    private static final String BODY_CONTENT = '{"label":"Body Content", "text":"This is some body content."}'
    private final OkHttpClient client = new OkHttpClient.Builder().cookieJar(new InMemoryCookieJar()).build()
    private final ErsatzRequestWithContent request = new ErsatzRequestWithContent('POST', '/posting')
    @AutoCleanup('stop') private final ErsatzServer server = new ErsatzServer()

    def 'body'() {
        when:
        request.body(BODY_CONTENT)

        then:
        request.body == BODY_CONTENT
    }

    def 'contentType'() {
        when:
        request.contentType('image/jpeg')

        then:
        request.contentType == 'image/jpeg'
    }

    def 'body with content-type'() {
        when:
        request.body(BODY_CONTENT, 'text/plain')

        then:
        request.body == BODY_CONTENT
        request.contentType == 'text/plain'
    }

    def 'matching: body'() {
        setup:
        server.expectations {
            post('/posting').body(BODY_CONTENT).responds().content('accepted')
        }.start()

        when:
        String value = exec(clientPost('/posting', 'text/plain', BODY_CONTENT).build()).body().string()

        then:
        value == 'accepted'

        when:
        value = exec(clientPost('/posting', 'text/plain', '').build()).body().string()

        then:
        value == NOT_FOUND_BODY
    }

    def 'matching: body and content-type'() {
        setup:
        server.expectations {
            post('/posting').body(BODY_CONTENT, 'text/plain; charset=utf-8').responds().content('accepted')
        }.start()

        when:
        String value = exec(clientPost('/posting', 'text/plain; charset=utf-8', BODY_CONTENT).build()).body().string()

        then:
        value == 'accepted'

        when:
        value = exec(clientPost('/posting', 'text/html', BODY_CONTENT).build()).body().string()

        then:
        value == NOT_FOUND_BODY
    }

    def 'matching: body with converter (builder)'() {
        setup:
        server.expectations {
            post('/posting').body([label: "Body Content", text: "This is some body content."]).contentType('some/json; charset=utf-8').converter('some/json; charset=utf-8', { b ->
                new JsonSlurper().parse(b)
            }).responds().content('accepted')
        }.start()

        when:
        String value = exec(clientPost('/posting', 'some/json; charset=utf-8', BODY_CONTENT).build()).body().string()

        then:
        value == 'accepted'

        when:
        value = exec(clientPost('/posting', 'text/html', BODY_CONTENT).build()).body().string()

        then:
        value == NOT_FOUND_BODY
    }

    def 'matching: body with converter (dls)'() {
        setup:
        String responseContent = 'accepted'

        server.expectations {
            post('/posting') {
                body([label: "Body Content", text: "This is some body content."])
                contentType 'some/json; charset=utf-8'
                converter('some/json; charset=utf-8', { b -> new JsonSlurper().parse b })
                responder {
                    content responseContent
                }
            }
        }.start()

        when:
        String value = exec(clientPost('/posting', 'some/json; charset=utf-8', BODY_CONTENT).build()).body().string()

        then:
        value == responseContent

        when:
        value = exec(clientPost('/posting', 'text/html', BODY_CONTENT).build()).body().string()

        then:
        value == NOT_FOUND_BODY
    }

    private Builder clientPost(final String path, final String contentType, final String content) {
        new Builder().post(create(parse(contentType), content)).url("${server.serverUrl}${path}")
    }

    private Response exec(Request req) {
        client.newCall(req).execute()
    }
}
