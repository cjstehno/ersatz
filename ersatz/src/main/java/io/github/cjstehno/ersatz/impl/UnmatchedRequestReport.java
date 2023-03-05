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
package io.github.cjstehno.ersatz.impl;

import io.github.cjstehno.ersatz.server.ClientRequest;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.stream.Collectors.joining;

/**
 * Helper object used to build and render a report of the unmatched request and the configured expectations.
 */
@RequiredArgsConstructor
public class UnmatchedRequestReport implements Report {

    private static final List<String> TEXT_CONTENT_HINTS = List.of("text/", "/json", "application/x-www-form-urlencoded");

    private final AtomicReference<String> cache = new AtomicReference<>();
    private final ClientRequest request;
    private final List<ErsatzRequest> expectations;
    private final List<ErsatzRequestRequirement> requirements;

    @Override public String render() {
        if (cache.get() == null) {
            val out = new StringBuilder();

            out.append("# Unmatched Request\n\n");

            final String query = request.getQueryParams().entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).collect(joining(", "));
            out.append(request.getScheme()).append(" ").append(request.getMethod()).append(" ").append(request.getPath()).append(" ? ").append(query).append("\n");

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

            // Write out the requirements
            renderRequirements(out);

            // Write out the expectations
            renderExpectations(out);

            cache.set(out.toString());
        }

        return cache.get();
    }

    private void renderRequirements(StringBuilder out) {
        out.append("\n# Requirements\n\n");

        for (int r = 0; r < requirements.size(); r++) {
            val requirement = requirements.get(r);

            out.append("Requirement %d (%s):\n".formatted(r, requirement.getDescription()));

            if (requirement.matches(request)) {
                requirement.getMatchers().forEach(m -> {
                    val met = m.matches(request);
                    if (met) {
                        out.append("  %s%s%s %s\n".formatted(GREEN, CHECKMARK, RESET, describe(m)));

                    } else {
                        out.append("  %sX %s%s\n".formatted(RED, describe(m), RESET));
                    }
                });

            } else {
                // not applicable (write out for informational)
                requirement.getMatchers().forEach(m -> {
                    out.append("  - %s\n".formatted(describe(m)));
                });
            }

            out.append('\n');
        }
    }

    private void renderExpectations(StringBuilder out) {
        out.append("# Expectations\n\n");

        for (int index = 0; index < expectations.size(); index++) {
            val req = expectations.get(index);
            val count = req.getRequestMatchers().size();

            out.append("Expectation %d (%d matchers):\n".formatted(index, count));

            val failed = new AtomicInteger(0);
            req.getRequestMatchers().forEach(matcher -> {
                boolean matches = matcher.matches(request);
                if (matches) {
                    out.append("  ").append(GREEN).append(CHECKMARK).append(RESET).append(" ").append(matcher).append("\n");
                } else {
                    out.append("  ").append(RED).append("X ").append(matcher).append(RESET).append("\n");
                    failed.incrementAndGet();
                }
            });

            val matched = count - failed.get();
            val failures = failed.get() > 0;
            out.append("  (%d matchers: %d matched, %s%d failed%s)\n\n".formatted(count, matched, failures ? RED : "", failed.get(), failures ? RESET : ""));
        }
    }

    private static String describe(final Matcher<?> matcher) {
        val desc = new StringDescription();
        matcher.describeTo(desc);
        return desc.toString();
    }
}
