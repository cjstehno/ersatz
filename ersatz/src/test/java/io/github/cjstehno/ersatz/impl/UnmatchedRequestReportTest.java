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
package io.github.cjstehno.ersatz.impl;

import io.github.cjstehno.ersatz.encdec.Cookie;
import io.github.cjstehno.ersatz.encdec.ResponseEncoders;
import io.github.cjstehno.ersatz.server.MockClientRequest;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

import static io.github.cjstehno.ersatz.cfg.HttpMethod.*;
import static io.github.cjstehno.ersatz.match.HttpMethodMatcher.methodMatching;
import static io.github.cjstehno.ersatz.match.PathMatcher.pathMatching;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class UnmatchedRequestReportTest {

    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String RESET = "\u001B[0m";
    private static final String BODY = "This is some text content";

    @ParameterizedTest @DisplayName("unmatched report with type #contentType should print #content")
    @MethodSource("contentProvider")
    void unmatched(final String contentType, final String content) throws IOException {
        val headers = new LinkedHashMap<String, Deque<String>>();
        headers.put("alpha", new ArrayDeque<>(List.of("bravo-1", "bravo-2")));
        headers.put("charlie", new ArrayDeque<>(List.of("delta")));

        val request = new MockClientRequest(GET, "/alpha/foo");
        request.setScheme("HTTP");
        request.setHeaders(headers);
        request.setContentLength(12345);
        request.setContentType(contentType);
        request.setCharacterEncoding("UTF-8");
        request.setBody(BODY.getBytes());
        request.query("selected", "one", "two");
        request.query("id", "1002");
        request.setCookies(Map.of(
            "ident", new Cookie("asdfasdfasdf", null, null, null, 0, false, 0, false)
        ));

        val actualLines = new UnmatchedRequestReport(
            request,
            List.of(
                new ErsatzRequest(POST, pathMatching("/alpha/foo"), new ResponseEncoders()),
                (ErsatzRequest) new ErsatzRequest(PUT, pathMatching(startsWith("/alpha/bar")), new ResponseEncoders()).secure()
            ),
            List.of(
                (ErsatzRequestRequirement) new ErsatzRequestRequirement(methodMatching(DELETE), pathMatching("/delete")).header("some", "header"),
                (ErsatzRequestRequirement) new ErsatzRequestRequirement(methodMatching(GET), pathMatching(startsWith("/alpha"))).query("id", "1002"),
                (ErsatzRequestRequirement) new ErsatzRequestRequirement(methodMatching(GET), pathMatching(startsWith("/alpha"))).query("id", "555")
            )
        ).render().split("\n");

        val stream = UnmatchedRequestReportTest.class.getResourceAsStream("/report-template.txt");
        val expectedLines = IOUtils.toString(stream, UTF_8)
            .replaceAll("\\$\\{contentType}", contentType)
            .replaceAll("\\$\\{content}", content)
            .replaceAll("\\$\\{RED}", RED)
            .replaceAll("\\$\\{GREEN}", GREEN)
            .replaceAll("\\$\\{RESET}", RESET)
            .split("\n");

        assertEquals(expectedLines.length, actualLines.length);

        for (int line = 0; line < expectedLines.length; line++) {
            val expectedLine = expectedLines[line].trim();
            val actualLine = actualLines[line].trim();
            assertEquals(expectedLine, actualLine);
        }
    }

    private static Stream<Arguments> contentProvider() {
        return Stream.of(
            arguments("application/octet-stream", "[84, 104, 105, 115, 32, 105, 115, 32, 115, 111, 109, 101, 32, 116, 101, 120, 116, 32, 99, 111, 110, 116, 101, 110, 116]"),
            arguments("text/plain", BODY),
            arguments("text/csv", BODY),
            arguments("application/json", BODY),
            arguments("application/x-www-form-urlencoded", BODY)
        );
    }
}
