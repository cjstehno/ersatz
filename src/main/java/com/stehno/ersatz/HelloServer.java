package com.stehno.ersatz;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

/**
 * Created by cjstehno on 12/1/16.
 */
public class HelloServer {

    public static void main(final String[] args) {
        final ErsatzServer ersatzServer = new ErsatzServer();

        ersatzServer.requesting(expectations -> {
            expectations.get("/foo").responds().body("This is Ersatz!!");
            expectations.get("/bar").responds().body("This is Bar!!");
        });

        Undertow server = Undertow.builder()
            .addHttpListener(8080, "localhost")
            .setHandler(new HttpHandler() {
                @Override
                public void handleRequest(final HttpServerExchange exchange) throws Exception {
                    ersatzServer.getExpectations().requests().forEach(getreq -> {
                        if (matches(exchange, getreq)) {
                            exchange.getResponseSender().send(getreq.getResponse().getBody().toString());
                        }
                    });
                }
            }).build();

        server.start();
    }

    private static boolean matches(final HttpServerExchange exchange, final GetRequest request) {
        return exchange.getRequestPath().equals(request.getPath());
    }
}
