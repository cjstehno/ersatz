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
package com.stehno.ersatz.impl

import com.burgstaller.okhttp.AuthenticationCacheInterceptor
import com.burgstaller.okhttp.CachingAuthenticatorDecorator
import com.burgstaller.okhttp.digest.CachingAuthenticator
import com.burgstaller.okhttp.digest.Credentials
import com.burgstaller.okhttp.digest.DigestAuthenticator
import com.stehno.ersatz.ErsatzServer
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import spock.lang.AutoCleanup
import spock.lang.Specification

import java.util.concurrent.ConcurrentHashMap

class DigestSpec extends Specification {

    @AutoCleanup('stop')
    private final ErsatzServer ersatzServer = new ErsatzServer({
        authentication {
            digest()
        }
    })

    def 'DIGEST no-auth'() {
        setup:
        ersatzServer.expectations {
            get('/secrets').responds().content('Something secure')
        }.start()

        when:
        Response response = digestClient('admin', 'asdfasdfasdf').newCall(
            new Request.Builder().url("${ersatzServer.httpUrl}/secrets").build()
        ).execute()

        then:
        response.code() == 401
    }

    def 'DIGEST auth'() {
        setup:
        ersatzServer.expectations {
            get('/secrets').responds().content('Something secure')
        }.start()

        when:
        Response response = digestClient('admin', '$3cr3t').newCall(
            new Request.Builder().url("${ersatzServer.httpUrl}/secrets").build()
        ).execute()

        then:
        response.code() == 200
        response.body().string() == 'Something secure'
    }

    private static OkHttpClient digestClient(final String username, final String password) {
        Map<String, CachingAuthenticator> authCache = new ConcurrentHashMap<>()

        new OkHttpClient.Builder()
            .addInterceptor(new AuthenticationCacheInterceptor(authCache))
            .authenticator(new CachingAuthenticatorDecorator(new DigestAuthenticator(new Credentials(username, password)), authCache))
            .build()

    }
}
