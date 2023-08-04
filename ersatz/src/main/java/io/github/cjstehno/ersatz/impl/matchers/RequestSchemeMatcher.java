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
package io.github.cjstehno.ersatz.impl.matchers;

import io.github.cjstehno.ersatz.server.ClientRequest;
import lombok.RequiredArgsConstructor;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * Matcher used to match the request scheme to determine whether it is secure (HTTPS) or not (HTTP).
 */
@RequiredArgsConstructor
public class RequestSchemeMatcher extends BaseMatcher<ClientRequest> {

    private final boolean secure;

    @Override public boolean matches(final Object actual) {
        return ((ClientRequest) actual).getScheme().equalsIgnoreCase(secure ? "HTTPS" : "HTTP");
    }

    @Override public void describeTo(final Description description) {
        description.appendText("Scheme is " + (secure ? "HTTPS" : "HTTP"));
    }
}
