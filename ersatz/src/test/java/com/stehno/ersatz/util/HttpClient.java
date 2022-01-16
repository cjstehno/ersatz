/**
 * Copyright (C) 2022 Christopher J. Stehno
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
package com.stehno.ersatz.util;

import com.stehno.ersatz.InMemoryCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static java.util.Collections.emptyMap;
import static java.util.concurrent.CompletableFuture.supplyAsync;

public class HttpClient {

    // FIXME: once the tests have been converted, look into using the Java 9 client instead
    // FIXME: consider making this an extension that automatically links with server to resolves paths

    private final OkHttpClient client;

    public HttpClient() {
        this(false, null);
    }

    public HttpClient(final boolean https) {
        this(https, null);
    }

    public HttpClient(final Consumer<Builder> config) {
        this(false, config);
    }

    public HttpClient(final boolean https, final Consumer<Builder> config) {
        final var builder = new Builder().cookieJar(new InMemoryCookieJar());

        if (https) {
            try {
                configureHttps(builder);
            } catch (Exception ex) {
                throw new IllegalStateException("Unable to configure HTTPS: " + ex.getMessage(), ex);
            }
        }

        if (config != null) {
            config.accept(builder);
        }

        client = builder.build();
    }

    public Response get(final Map<String, Object> headers, final String url) throws IOException {
        final var request = new Request.Builder().url(url).get();
        return client.newCall(applyHeaders(request, headers).build()).execute();
    }

    private Request.Builder applyHeaders(final Request.Builder request, final Map<String, Object> headers) {
        headers.forEach((k, v) -> {
            if (v instanceof Collection) {
                ((Collection<?>) v).forEach(item -> request.addHeader(k, item.toString()));
            } else {
                request.header(k, v.toString());
            }
        });

        return request;
    }

    public Response get(final String url) throws IOException {
        return get(emptyMap(), url);
    }

    public CompletableFuture<Response> getAsync(final Map<String, Object> headers, final String url) {
        return supplyAsync(() -> {
            try {
                return get(headers, url);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    public CompletableFuture<Response> getAsync(final String url) {
        return supplyAsync(() -> {
            try {
                return get(url);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    public Response delete(final String url) throws IOException {
        return delete(emptyMap(), url);
    }

    public Response delete(final Map<String, Object> headers, final String url) throws IOException {
        final var request = new Request.Builder().url(url).delete();
        return client.newCall(applyHeaders(request, headers).build()).execute();
    }

    public Response post(final String url, final RequestBody body) throws IOException {
        return post(emptyMap(), url, body);
    }

    public Response post(final Map<String, Object> headers, final String url, final RequestBody body) throws IOException {
        final var request = new Request.Builder().url(url).post(body);
        return client.newCall(applyHeaders(request, headers).build()).execute();
    }

    public Response put(final String url, final RequestBody body) throws IOException {
        return put(emptyMap(), url, body);
    }

    public Response put(final Map<String, Object> headers, final String url, final RequestBody body) throws IOException {
        final var request = new Request.Builder().url(url).put(body);
        return client.newCall(applyHeaders(request, headers).build()).execute();
    }

    public Response head(final String url) throws IOException {
        return head(emptyMap(), url);
    }

    public Response head(final Map<String, Object> headers, final String url) throws IOException {
        final var request = new Request.Builder().url(url).head();
        return client.newCall(applyHeaders(request, headers).build()).execute();
    }

    private static void configureHttps(final Builder builder) throws KeyManagementException, NoSuchAlgorithmException {
        // Create a trust manager that does not validate certificate chains
        final var trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }
        };

        // Install the all-trusting trust manager
        final SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new SecureRandom());

        // Create an ssl socket factory with our all-trusting manager
        final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

        builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
            .hostnameVerifier((s, sslSession) -> true);
    }

    public Response options(final String url) throws IOException {
        final var request = new Request.Builder().url(url).method("OPTIONS", null);
        return client.newCall(request.build()).execute();
    }

    public Response trace(String url) throws IOException {
        final var request = new Request.Builder().url(url).method("TRACE", null);
        return client.newCall(request.build()).execute();
    }
}
