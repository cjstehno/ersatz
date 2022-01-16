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

import com.stehno.ersatz.ErsatzServer;
import com.stehno.ersatz.InMemoryCookieJar;
import lombok.val;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.function.Consumer;

import static java.util.Arrays.stream;

// FIXME: document
// FIXME: consider making this a separate library (?)
public class HttpClientExtension implements BeforeEachCallback {

    @Override public void beforeEach(final ExtensionContext context) throws Exception {
        val testInstance = context.getRequiredTestInstance();

        val server = findInstance(testInstance).start();

        val https = server.isHttpsEnabled();
        val client = new Client(server.getHttpUrl(), https ? server.getHttpsUrl() : server.getHttpUrl(), https);
        findField(testInstance, "Client").set(testInstance, client);
    }

    static ErsatzServer findInstance(final Object testInstance) throws Exception {
        return (ErsatzServer) findField(testInstance, "ErsatzServer").get(testInstance);
    }

    private static Field findField(final Object testInstance, final String type) throws Exception {
        val field = stream(testInstance.getClass().getDeclaredFields())
            .filter(f -> f.getType().getSimpleName().endsWith(type))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("A field of type " + type + " must be specified."));
        field.setAccessible(true);

        return field;
    }

    public class Client {

        private final String httpUrl;
        private final String httpsUrl;
        private final OkHttpClient client;

        public Client(final String httpUrl, final String httpsUrl, final boolean httpsEnabled) throws Exception {
            this.httpUrl = httpUrl;
            this.httpsUrl = httpsUrl;

            client = configureHttps(
                new OkHttpClient.Builder().cookieJar(new InMemoryCookieJar()),
                httpsEnabled
            ).build();
        }

        public Response get(final String path, final Consumer<Request.Builder> config) throws IOException {
            return doGet(httpUrl + path, config);
        }

        public Response get(final String path) throws IOException {
            return get(path, null);
        }

        public Response gets(final String path, final Consumer<Request.Builder> config) throws IOException {
            return doGet(httpsUrl + path, config);
        }

        public Response gets(final String path) throws IOException {
            return doGet(httpsUrl + path, null);
        }

        private Response doGet(final String url, final Consumer<Request.Builder> config) throws IOException {
            val request = new Request.Builder().url(url).get();
            if (config != null) config.accept(request);

            return client.newCall(request.build()).execute();
        }

        private static OkHttpClient.Builder configureHttps(final OkHttpClient.Builder builder, final boolean enabled) throws KeyManagementException, NoSuchAlgorithmException {
            if (enabled) {
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
                val sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, trustAllCerts, new SecureRandom());

                // Create an ssl socket factory with our all-trusting manager
                val sslSocketFactory = sslContext.getSocketFactory();

                builder.sslSocketFactory(
                        sslSocketFactory,
                        (X509TrustManager) trustAllCerts[0]
                    )
                    .hostnameVerifier((s, sslSession) -> true);
            }

            return builder;
        }
    }
}
