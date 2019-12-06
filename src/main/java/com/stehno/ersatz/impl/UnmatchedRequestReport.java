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
package com.stehno.ersatz.impl;

import com.stehno.ersatz.server.ClientRequest;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.stream.Collectors.joining;

/**
 * Helper object used to build and render a report of the unmatched request and the configured expectations.
 */
public class UnmatchedRequestReport implements Report {

    private static final List<String> TEXT_CONTENT_HINTS = List.of("text/", "/json", "application/x-www-form-urlencoded");

    private final ClientRequest request;
    private final List<ErsatzRequest> expectations;
    private final AtomicReference<String> cache = new AtomicReference<>();

    public UnmatchedRequestReport(final ClientRequest request, final List<ErsatzRequest> expectations) {
        this.request = request;
        this.expectations = expectations;
    }

    public String render() {
        if (cache.get() == null) {
            final StringBuilder out = new StringBuilder();

            out.append("# Unmatched Request\n\n");

            final String query = request.getQueryParams().entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).collect(joining(", "));
            out.append(request.getProtocol()).append(" ").append(request.getMethod()).append(" ").append(request.getPath()).append(" ? ").append(query).append("\n");

            if (request.getHeaders() != null) {
                out.append("Headers:\n");
                request.getHeaders().forEach((hname, hvalues) -> {
                    out.append(" - ").append(hname).append(": ").append(hvalues).append("\n");
                });
            }

            if (request.getCookies() != null) {
                out.append("Cookies:\n");
                request.getCookies().forEach((n, c) -> {
                    out.append(" - ").append(n).append(" (").append(c.getDomain()).append(", ").append(c.getPath()).append("): ").append(c.getValue()).append("\n");
                });
            }

            if (request.getCharacterEncoding() != null) {
                out.append("Character-Encoding: ").append(request.getCharacterEncoding()).append("\n");
            }

            if (request.getContentType() != null) {
                out.append("Content-type: ").append(request.getContentType()).append("\n");
            }

            if (request.getContentLength() > 0) {
                out.append("Content-Length: ").append(request.getContentLength()).append("\n");
            }

            if (request.getBody() != null) {
                out.append("Content:\n");
                if (request.getContentType() != null && TEXT_CONTENT_HINTS.stream().anyMatch(h -> request.getContentType().contains(h))) {
                    out.append("  ").append(new String(request.getBody(), Charset.forName(request.getCharacterEncoding() != null ? request.getCharacterEncoding() : "UTF-8"))).append("\n");
                } else {
                    out.append("  ").append(Arrays.toString(request.getBody())).append("\n");
                }
            }

            out.append("\n# Expectations\n\n");

            for (int index = 0; index < expectations.size(); index++) {
                final var req = expectations.get(index);

                int count = req.getRequestMatchers().size();

                out.append("Expectation ").append(index).append(" (").append(count).append(" matchers):\n");

                final var failed = new AtomicInteger(0);
                req.getRequestMatchers().forEach(matcher -> {
                    boolean matches = matcher.matches(request);
                    if (matches) {
                        out.append("  ").append(GREEN).append("✓").append(RESET).append(" ").append(matcher).append("\n");
                    } else {
                        out.append("  ").append(RED).append("X ").append(matcher).append(RESET).append("\n");
                        failed.incrementAndGet();
                    }
                });

                final int matched = count - failed.get();
                out.append("  (").append(count).append(" matchers: ").append(matched).append(" matched, ").append(failed.get() > 0 ? RED : "").append(failed.get()).append(" failed").append(failed.get() > 0 ? RESET : "").append(")\n\n");
            }

            cache.set(out.toString());
        }

        return cache.get();
    }
}
