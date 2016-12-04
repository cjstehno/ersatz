package com.stehno.ersatz

import com.stehno.ersatz.feat.BasicAuthFeature
import okhttp3.OkHttpClient
import spock.lang.Specification

class BasicSpec extends Specification {

    private final ErsatzServer ersatzServer = new ErsatzServer(features: [new BasicAuthFeature()])
    private final OkHttpClient client = new OkHttpClient()

    def 'BASIC auth'() {
        setup:
        ersatzServer.expectations {
            get('/secrets').responds().body('Something secure')
        }.start()

        String encodedCred = "Basic ${'admin:$3cr3t'.bytes.encodeBase64()}"

        when:
        okhttp3.Response response = client.newCall(new okhttp3.Request.Builder().url("${ersatzServer.serverUrl}/secrets").build()).execute()

        then:
        response.code() == 401
        response.body().string() == ''

        when:
        response = client.newCall(new okhttp3.Request.Builder().url("${ersatzServer.serverUrl}/secrets").addHeader('Authorization',encodedCred).build()).execute()

        then:
        response.code() == 200
        response.body().string() == 'Something secure'
    }

    def cleanup() {
        ersatzServer.stop()
    }
}
