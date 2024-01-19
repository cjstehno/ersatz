/**
 * Copyright (C) 2024 Christopher J. Stehno
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
package io.github.cjstehno.ersatz.server.undertow;

import io.github.cjstehno.ersatz.cfg.Response;
import io.github.cjstehno.ersatz.impl.ErsatzForwardResponse;
import io.github.cjstehno.ersatz.server.ClientRequest;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.nio.ByteBuffer;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Set;

import static java.lang.String.join;

/**
 * An Ersatz Undertow handler used to handle request forwarding to gather response data from an external server URI.
 * <p>
 * This implementation utilizes the <a href="https://square.github.io/okhttp/">OkHttp</a> library to make its forwarded
 * requests. An attempt was made to use the built-in JDK HttpClient, however, it was overly restrictive and was too much
 * effort to make it work with HTTPS requests.
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE) @Slf4j
public class ErsatzForwardHandler implements ErsatzHandler {

    private static final Set<String> REQUESTS_WITH_BODY = Set.of("post", "put", "patch");
    private final ErsatzHandler next;

    /**
     * Handles the request. It will determine whether the request should be forwarded to an external resource to retrieve
     * its response, otherwise it will continue the standard Ersatz processing.
     *
     * @param exchange       the server exchange object for the current request/response cycle
     * @param clientRequest  the incoming client request
     * @param ersatzResponse the configured outgoing response
     * @throws Exception if there is a problem handling the request/response
     */
    public void handleRequest(final HttpServerExchange exchange, final ClientRequest clientRequest, final Response ersatzResponse) throws Exception {
        if (ersatzResponse instanceof ErsatzForwardResponse) {
            val fullTargetUri = resolveTargetUri(exchange, ersatzResponse);
            log.info("Request forwarding to: {}", fullTargetUri);

            val client = configureClient(clientRequest.getScheme().equalsIgnoreCase("https"));

            val requestMethod = exchange.getRequestMethod().toString();
            val hasBody = REQUESTS_WITH_BODY.contains(requestMethod.toLowerCase());

            val requestBuilder = new Request.Builder()
                .method(exchange.getRequestMethod().toString(), hasBody ? RequestBody.create(clientRequest.getBody()) : null)
                .url(fullTargetUri);

            // copy request headers
            exchange.getRequestHeaders().forEach(header -> {
                requestBuilder.header(header.getHeaderName().toString(), join(";", header));
            });

            try (val response = client.newCall(requestBuilder.build()).execute()) {
                // copy response headers
                response.headers().forEach(pair -> {
                    exchange.getResponseHeaders().putAll(new HttpString(pair.getFirst()), response.headers(pair.getFirst()));
                });

                exchange.setStatusCode(response.code());
                exchange.getResponseSender().send(ByteBuffer.wrap(response.body().bytes()));
            }

        } else {
            next.handleRequest(exchange, clientRequest, ersatzResponse);
        }
    }

    private static String resolveTargetUri(final HttpServerExchange exchange, final Response response) {
        val queryString = exchange.getQueryString();
        return ((ErsatzForwardResponse) response).getProxyTargetUri() + exchange.getRequestPath() + (!queryString.isEmpty() ? "?" + queryString : "");
    }

    // We're just going to ignore HTTPS for forwarded requests
    private static OkHttpClient configureClient(final boolean https) throws KeyManagementException, NoSuchAlgorithmException {
        val builder = new OkHttpClient.Builder();

        if (https) {
            // Create a trust manager that does not validate certificate chains
            final var trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
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
            ).hostnameVerifier((s, sslSession) -> true);
        }

        return builder.build();
    }
}
