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

import com.stehno.ersatz.ClientRequest;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.joining;

/**
 * Helper object used to build and render a report of the unmatched request and the configured expectations.
 */
public class UnmatchedRequestReport {

    private static final List<String> TEXT_CONTENT_HINTS = Arrays.asList("text/", "/json", "application/x-www-form-urlencoded");
    private static final String RED = "\u001b[31m";
    private static final String GREEN = "\u001b[32m";
    private static final String RESET = "\u001b[0m";

    private final ClientRequest request;
    private final List<ErsatzRequest> expectations;

    public UnmatchedRequestReport(final ClientRequest request, final List<ErsatzRequest> expectations) {
        this.request = request;
        this.expectations = expectations;
    }

    // FIXME: @Memoized(maxCacheSize = 1, protectedCacheSize = 1)
    public String toString() {
        final StringBuilder out = new StringBuilder();

        out.append("# Unmatched Request\n\n");

        final String query = request.getQueryParams().entrySet().stream()
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(joining(", "));

        out.append(request.getProtocol() + " " + request.getMethod() + " " + request.getPath() + "? " + query + "\n");

        if (request.getHeaders() != null) {
            out.append("Headers:\n");
            request.getHeaders().forEach(h -> {
                out.append(" - " + h.getHeaderName() + ": " + h.toArray() + "\n");
            });
        }

        if (request.getCookies() != null) {
            out.append("Cookies:\n");
            request.getCookies().forEach((n, c) -> {
                out.append(" - " + n + " (" + c.getDomain() + ", " + c.getPath() + "): " + c.getValue() + "\n");
            });
        }

        if (request.getCharacterEncoding() != null) {
            out.append("Character-Encoding: " + request.getCharacterEncoding() + "\n");
        }

        if (request.getContentType() != null) {
            out.append("Content-type: " + request.getContentType() + "\n");
        }

        if (request.getContentLength() > 0) {
            out.append("Content-Length: " + request.getContentLength() + "\n");
        }

        if (request.getBody() != null) {
            out.append("Content:\n");
            if (request.getContentType() != null && TEXT_CONTENT_HINTS.stream().anyMatch(h -> request.getContentType().contains(h))) {
                out.append("  " + new String(request.getBody(), Charset.forName(request.getCharacterEncoding() != null ? request.getCharacterEncoding() : "UTF-8")) + "\n");
            } else {
                out.append("  " + request.getBody() + "\n");
            }
        }

        out.append("\n# Expectations\n\n");

        for (int index = 0; index < expectations.size(); index++) {
            final ErsatzRequest req = expectations.get(index);
            int count = req.getRequestMatchers().size();

            out.append("Expectation " + index + " (" + count + " matchers):\n");

            final AtomicInteger failed = new AtomicInteger(0);
            req.getRequestMatchers().forEach(matcher -> {
                boolean matches = matcher.matches(request);
                if (matches) {
                    out.append("  " + GREEN + "✓" + RESET + " " + matcher + "\n");
                } else {
                    out.append("  " + RED + "X " + matcher + RESET + "\n");
                    failed.incrementAndGet();
                }
            });

            out.append("  (" + count + " matchers: " + (count - failed.get()) + " matched, " + (failed.get() > 0 ? RED : "") + failed + " failed" + (failed.get() > 0 ? RESET : "") + ")\n\n");
        }

        return out.toString();
    }
}
