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
package io.github.cjstehno.ersatz.match;

import io.github.cjstehno.ersatz.encdec.DecoderChain;
import io.github.cjstehno.ersatz.encdec.DecodingContext;
import io.github.cjstehno.ersatz.server.ClientRequest;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import static io.github.cjstehno.ersatz.match.HeaderMatcher.contentTypeHeader;
import static lombok.AccessLevel.PRIVATE;
import static org.hamcrest.CoreMatchers.startsWith;

/**
 * Matcher for matching request body content.
 */
public abstract class BodyMatcher extends BaseMatcher<ClientRequest> {

    /**
     * The decoder chain to be used when decoding the request body.
     */
    @Setter public DecoderChain decoderChain;

    /**
     * Creates a matcher which will match based on the body content matcher and the specified content type.
     *
     * @param matcher the body content matcher
     * @param contentType the expected content type
     * @return the body matcher
     */
    public static BodyMatcher bodyMatching(final Matcher<Object> matcher, final String contentType) {
        return new BodyMatches(contentType, matcher);
    }

    @RequiredArgsConstructor(access = PRIVATE)
    private static class BodyMatches extends BodyMatcher {

        private final String contentType;
        private final Matcher<Object> bodyMatcher;

        @Override public boolean matches(final Object actual) {
            val request = (ClientRequest) actual;
            return contentTypeHeader(startsWith(contentType)).matches(request) &&
                bodyMatcher.matches(decode(request));
        }

        private Object decode(final ClientRequest request) {
            val decoder = decoderChain.resolve(contentType);
            return decoder != null ? decoder.apply(
                request.getBody(),
                new DecodingContext(
                    request.getContentLength(),
                    request.getContentType(),
                    request.getCharacterEncoding(),
                    decoderChain
                )
            ) : null;
        }

        @Override public void describeTo(final Description description) {
            description.appendText("Body is ");
            bodyMatcher.describeTo(description);
            description.appendText(" and content-type is a string starting with " + contentType);
        }
    }
}
