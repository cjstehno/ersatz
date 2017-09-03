/*
 * Copyright (C) 2017 Christopher J. Stehno
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
package com.stehno.ersatz.impl

import com.stehno.ersatz.ClientRequest
import groovy.transform.Memoized

/**
 * Helper object used to build and render a report of the unmatched request and the configured expectations.
 */
class UnmatchedRequestReport {

    private final ClientRequest request
    private final List<ErsatzRequest> expectations

    UnmatchedRequestReport(final ClientRequest request, final List<ErsatzRequest> expectations) {
        this.request = request
        this.expectations = expectations
    }

    @Memoized(maxCacheSize = 1, protectedCacheSize = 1)
    String toString() {
        StringBuilder out = new StringBuilder()

        out.append('# Unmatched Request\n\n')

        String query = request.queryParams.collect { k, v -> "$k=$v" }.join(', ')
        out.append("${request.protocol} ${request.method} ${request.path} ? $query\n")

        if (request.headers) {
            out.append 'Headers:\n'
            request.headers.each { h ->
                out.append " - ${h.headerName}: ${h.toArray()}\n"
            }
        }

        if (request.cookies) {
            out.append 'Cookies:\n'
            request.cookies.each { n, c ->
                out.append " - ${n} (${c.domain}, ${c.path}): ${c.value}\n"
            }
        }

        if (request.characterEncoding) {
            out.append "Character-Encoding: ${request.characterEncoding}\n"
        }

        if (request.contentType) {
            out.append "Content-type: ${request.contentType}\n"
        }

        if (request.contentLength > 0) {
            out.append "Content-Length: ${request.contentLength}\n"
        }

        if (request.body) {
            out.append 'Content:\n'
            out.append request.body ? "  ${request.body}\n" : '  <empty>\n'
        }

        out.append('\n# Expectations\n\n')

        expectations.eachWithIndex { ErsatzRequest req, int index ->
            int count = req.requestMatchers.size()

            out.append "Expectation $index ($count matchers):\n"

            int failed = 0
            req.requestMatchers.each { RequestMatcher matcher ->
                boolean matches = matcher.matches(request)
                if (matches) {
                    out.append "  ✓ ${matcher}\n"
                } else {
                    out.append "  X ${matcher}\n"
                    failed++
                }
            }

            out.append "  ($count matchers: ${count - failed} matched, $failed failed)\n\n"
        }

        out.toString()
    }
}
