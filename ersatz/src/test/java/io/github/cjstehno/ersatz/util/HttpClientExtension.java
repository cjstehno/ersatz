/**
 * Copyright (C) 2023 Christopher J. Stehno
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
package io.github.cjstehno.ersatz.util;

import static io.github.cjstehno.ersatz.util.BasicAuth.AUTHORIZATION_HEADER;
import static io.github.cjstehno.ersatz.util.BasicAuth.header;
import static java.util.Arrays.stream;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.create;
import static org.junit.platform.commons.support.HierarchyTraversalMode.TOP_DOWN;

import io.github.cjstehno.ersatz.ErsatzServer;
import io.github.cjstehno.ersatz.InMemoryCookieJar;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import lombok.val;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.platform.commons.support.ModifierSupport;
import org.junit.platform.commons.support.ReflectionSupport;

/**
 * JUnit 5 Extension which provides a reusable HTTP client wrapper around a configured OkHttp client. This extension
 * requires the ErsatzServerExtension configured to provide the server link.
 */
public class HttpClientExtension implements BeforeEachCallback, ParameterResolver {

    // TODO: make client create on beforeAll and then shared - ensure cleared after each request
    // TODO: maybe make annotation to force it to create a new one for a method

    private static final String SERVER_KEY = "server-instance";
    private static final String CLIENT_KEY = "client-instance";
    private static final String SHARED_SERVER_KEY = "shared-instance";
    private static final String CLIENT_SUFFIX = "Client";
    private static final Namespace NAMESPACE = create("io.github.cjstehno", "ersatz");

    @Override @SuppressWarnings("resource") public void beforeEach(final ExtensionContext context) throws Exception {
        val testInstance = context.getRequiredTestInstance();

        val server = findServer(context);

        val https = server.isHttpsEnabled();
        val client = new Client(server.getHttpUrl(), server.getUrl(https), https);

        findClient(testInstance).ifPresent(field -> {
            try {
                field.set(testInstance, client);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });

        context.getStore(NAMESPACE).put(CLIENT_KEY, client);
    }

    @Override
    public boolean supportsParameter(final ParameterContext pCtx,final ExtensionContext eCtx) throws ParameterResolutionException {
        return pCtx.getParameter().getType().isAssignableFrom(Client.class);
    }

    @Override
    public Object resolveParameter(final ParameterContext pCtx,final ExtensionContext eCtx) throws ParameterResolutionException {
        return eCtx.getStore(NAMESPACE).get(CLIENT_KEY);
    }

    private static ErsatzServer findServer(final ExtensionContext context) throws IllegalAccessException {
        val fields = ReflectionSupport.findFields(
            context.getRequiredTestClass(),
            f -> f.getType().isAssignableFrom(ErsatzServer.class) && ModifierSupport.isNotStatic(f),
            TOP_DOWN
        );

        if (fields.isEmpty()) {
            var instance = context.getStore(NAMESPACE).get(SERVER_KEY);
            if (instance == null) {
                instance = context.getStore(NAMESPACE).get(SHARED_SERVER_KEY);
            }
            return (ErsatzServer) instance;

        } else {
            val field = fields.get(0);
            field.setAccessible(true);
            return (ErsatzServer) field.get(context.getRequiredTestInstance());
        }
    }

    private static Optional<Field> findClient(final Object testInstance) {
        return stream(testInstance.getClass().getDeclaredFields())
            .filter(f -> f.getType().getSimpleName().endsWith(CLIENT_SUFFIX))
            .findFirst()
            .map(f -> {
                f.setAccessible(true);
                return f;
            });
    }

    /**
     * The HTTP client wrapper used by the HttpClientExtension. It may be used outside the extension.
     */
    public static class Client {

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

        public Response get(final String path, final Consumer<Request.Builder> config, final boolean https) throws IOException {
            val request = new Request.Builder().url((https ? httpsUrl : httpUrl) + path).get();
            if (config != null) config.accept(request);

            return client.newCall(request.build()).execute();
        }

        public Response get(final String path, final Consumer<Request.Builder> config) throws IOException {
            return get(path, config, false);
        }

        public Response get(final String path, final boolean https) throws IOException {
            return get(path, null, https);
        }

        public Response get(final String path) throws IOException {
            return get(path, null, false);
        }

        public CompletableFuture<Response> aget(final String path, final Consumer<Request.Builder> config, final boolean https) {
            return supplyAsync(() -> {
                try {
                    return get(path, config, https);
                } catch (IOException io) {
                    throw new IllegalArgumentException(io.getMessage());
                }
            });
        }

        public CompletableFuture<Response> aget(final String path) {
            return aget(path, null, false);
        }

        public Response head(final String path, final Consumer<Request.Builder> config, final boolean https) throws IOException {
            val request = new Request.Builder().url((https ? httpsUrl : httpUrl) + path).head();
            if (config != null) config.accept(request);

            return client.newCall(request.build()).execute();
        }

        public Response head(final String path, final Consumer<Request.Builder> config) throws IOException {
            return head(path, config, false);
        }

        public Response head(final String path, final boolean https) throws IOException {
            return head(path, null, https);
        }

        public Response head(final String path) throws IOException {
            return head(path, null, false);
        }

        public Response delete(final String path, final Consumer<Request.Builder> config, final boolean https) throws IOException {
            val request = new Request.Builder().url((https ? httpsUrl : httpUrl) + path).delete();
            if (config != null) config.accept(request);

            return client.newCall(request.build()).execute();
        }

        public Response delete(final String path, final Consumer<Request.Builder> config) throws IOException {
            return delete(path, config, false);
        }

        public Response delete(final String path, final boolean https) throws IOException {
            return delete(path, null, https);
        }

        public Response delete(final String path) throws IOException {
            return delete(path, null, false);
        }

        public Response trace(final String path, final Consumer<Request.Builder> config, final boolean https) throws IOException {
            val request = new Request.Builder().url((https ? httpsUrl : httpUrl) + path).method("TRACE", null);
            if (config != null) config.accept(request);

            return client.newCall(request.build()).execute();
        }

        public Response options(final String path, final Consumer<Request.Builder> config, final boolean https) throws IOException {
            val request = new Request.Builder().url((https ? httpsUrl : httpUrl) + path).method("OPTIONS", null);
            if (config != null) config.accept(request);

            return client.newCall(request.build()).execute();
        }

        public Response post(final String path, final Consumer<Request.Builder> config, final RequestBody body, final boolean https) throws IOException {
            val request = new Request.Builder().url((https ? httpsUrl : httpUrl) + path).post(body);
            if (config != null) config.accept(request);

            return client.newCall(request.build()).execute();
        }

        public Response post(final String path, final Consumer<Request.Builder> config, final RequestBody body) throws IOException {
            return post(path, config, body, false);
        }

        public Response post(final String path, final RequestBody body, final boolean https) throws IOException {
            return post(path, null, body, https);
        }

        public Response post(final String path, final RequestBody body) throws IOException {
            return post(path, null, body, false);
        }

        public Response put(final String path, final Consumer<Request.Builder> config, final RequestBody body, final boolean https) throws IOException {
            val request = new Request.Builder().url((https ? httpsUrl : httpUrl) + path).put(body);
            if (config != null) config.accept(request);

            return client.newCall(request.build()).execute();
        }

        public Response put(final String path, final Consumer<Request.Builder> config, final RequestBody body) throws IOException {
            return put(path, config, body, false);
        }

        public Response put(final String path, final RequestBody body, final boolean https) throws IOException {
            return put(path, null, body, https);
        }

        public Response put(final String path, final RequestBody body) throws IOException {
            return put(path, null, body, false);
        }

        public Response patch(final String path, final Consumer<Request.Builder> config, final RequestBody body, final boolean https) throws IOException {
            val request = new Request.Builder().url((https ? httpsUrl : httpUrl) + path).patch(body);
            if (config != null) config.accept(request);

            return client.newCall(request.build()).execute();
        }

        public Response patch(final String path, final Consumer<Request.Builder> config, final RequestBody body) throws IOException {
            return patch(path, config, body, false);
        }

        public Response patch(final String path, final RequestBody body, final boolean https) throws IOException {
            return patch(path, null, body, https);
        }

        public Response patch(final String path, final RequestBody body) throws IOException {
            return patch(path, null, body, false);
        }

        public static Request.Builder basicAuthHeader(final Request.Builder builder, final String user, final String pass) {
            return builder.header(AUTHORIZATION_HEADER, header(user, pass));
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
