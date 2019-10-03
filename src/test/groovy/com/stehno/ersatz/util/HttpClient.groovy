/*
 * Copyright (C) 2019 Christopher J. Stehno
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
package com.stehno.ersatz.util

import com.stehno.ersatz.InMemoryCookieJar
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response

import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate

class HttpClient {

    private final OkHttpClient client

    HttpClient(final boolean enableHttps = false, final Closure<Void> config = null) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder().cookieJar(new InMemoryCookieJar())

        if (enableHttps) {
            configureHttps(builder)
        }

        if (config) {
            config.call(builder)
        }

        client = builder.build()
    }

    HttpClient(final Closure<Void> closure) {
        this(false, closure)
    }

    Response get(final Map<String, Object> headers = [:], final String url) {
        def request = new Request.Builder().url(url).get()

        headers?.each { k, v ->
            if (v instanceof Collection) {
                v.each { item ->
                    request.addHeader(k, item as String)
                }
            } else {
                request.header(k, v as String)
            }
        }

        client.newCall(request.build()).execute()
    }

    Response delete(final Map<String, Object> headers = [:], final String url) {
        def request = new Request.Builder().url(url).delete()

        headers?.each { k, v ->
            if (v instanceof Collection) {
                v.each { item ->
                    request.addHeader(k, item as String)
                }
            } else {
                request.header(k, v as String)
            }
        }

        client.newCall(request.build()).execute()
    }

    Response post(final Map<String, Object> headers = [:], final String url, RequestBody body) {
        Request.Builder request = new Request.Builder().url(url).post(body)

        headers?.each { k, v ->
            if (v instanceof Collection) {
                v.each { item ->
                    request.addHeader(k, item as String)
                }
            } else {
                request.header(k, v as String)
            }
        }

        client.newCall(request.build()).execute()
    }

    Response put(final Map<String, Object> headers = [:], final String url, RequestBody body) {
        Request.Builder request = new Request.Builder().url(url).put(body)

        headers?.each { k, v ->
            if (v instanceof Collection) {
                v.each { item ->
                    request.addHeader(k, item as String)
                }
            } else {
                request.header(k, v as String)
            }
        }

        client.newCall(request.build()).execute()
    }

    Response head(final Map<String, Object> headers = [:], final String url) {
        Request.Builder request = new Request.Builder().url(url).head()

        headers?.each { k, v ->
            if (v instanceof Collection) {
                v.each { item ->
                    request.addHeader(k, item as String)
                }
            } else {
                request.header(k, v as String)
            }
        }

        client.newCall(request.build()).execute()
    }

    private static void configureHttps(final OkHttpClient.Builder builder) {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = [
            new X509TrustManager() {
                @Override
                void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                X509Certificate[] getAcceptedIssuers() {
                    return [] as X509Certificate[]
                }
            }
        ] as TrustManager[]

        // Install the all-trusting trust manager
        final SSLContext sslContext = SSLContext.getInstance('SSL')
        sslContext.init(null, trustAllCerts, new SecureRandom())

        // Create an ssl socket factory with our all-trusting manager
        final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

        builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier(new HostnameVerifier() {
            @Override boolean verify(String s, SSLSession sslSession) {
                return true
            }
        })
    }
}
