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
package io.github.cjstehno.ersatz.server.undertow;

import io.github.cjstehno.ersatz.impl.ErsatzRequest;
import io.github.cjstehno.ersatz.impl.ExpectationsImpl;
import io.github.cjstehno.ersatz.impl.RequirementsImpl;
import io.github.cjstehno.ersatz.impl.UnmatchedRequestReport;
import io.github.cjstehno.ersatz.server.ClientRequest;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.nio.ByteBuffer;

import static io.github.cjstehno.ersatz.server.UnderlyingServer.NOT_FOUND_BODY;

/**
 * An Undertow <code>HttpHandler</code> used to start the Ersatz handling chain, but checking the request against its
 * requirements and configured matchers. If the request satisfies the matchers, control is handed off to the next handler
 * in the chain, otherwise a mismatch is reported.
 */
@RequiredArgsConstructor @Slf4j
public class ErsatzMatchingHandler implements HttpHandler {

    private static final byte[] EMPTY_RESPONSE = new byte[0];
    private final RequirementsImpl requirements;
    private final ExpectationsImpl expectations;
    private final boolean reportToConsole;
    private final ErsatzHandler next;

    @Override public void handleRequest(final HttpServerExchange exchange) throws Exception {
        val clientRequest = new UndertowClientRequest(exchange);
        log.debug("Handling request({}): {}", exchange.getProtocol(), clientRequest);

        // check the request against the global requirements
        if (!requirements.check(clientRequest)) {
            handleMismatch(exchange, clientRequest);
            return;
        }

        // check the request against the expectations
        expectations.findMatch(clientRequest).ifPresentOrElse(
            req -> {
                try {
                    val ersatzRequest = (ErsatzRequest) req;

                    // handle the matching request
                    next.handleRequest(exchange, clientRequest, ersatzRequest.getCurrentResponse());

                    // mark it as accepted
                    ersatzRequest.mark(clientRequest);

                } catch (final Exception ex) {
                    log.error("Error-Response: Internal Server Error (500): {}", ex.getMessage(), ex);
                    exchange.setStatusCode(500);
                    exchange.getResponseSender().send(ByteBuffer.wrap(EMPTY_RESPONSE));
                }
            },
            () -> handleMismatch(exchange, clientRequest)
        );
    }

    private void handleMismatch(final HttpServerExchange exchange, final ClientRequest clientRequest) {
        val report = new UnmatchedRequestReport(
            clientRequest,
            expectations.getRequests().stream().map(r -> (ErsatzRequest) r).toList(),
            requirements.getRequirements()
        );

        log.warn(report.render());

        if (reportToConsole) {
            System.out.println(report.render());
        }

        exchange.setStatusCode(404).getResponseSender().send(NOT_FOUND_BODY);
    }
}
