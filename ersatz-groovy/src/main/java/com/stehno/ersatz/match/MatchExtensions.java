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
package com.stehno.ersatz.match;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import space.jasan.support.groovy.closure.ConsumerWithDelegate;

import static groovy.lang.Closure.DELEGATE_FIRST;

/**
 * Groovy extensions to the ersatz Hamcrest matchers providing Groovy DSL features.
 */
public class MatchExtensions {

    /**
     * Configures the cookie matcher with a closure delegating to a <code>CookieMatcher</code> instance.
     *
     * @param closure the configuration closure
     * @return the configured matcher
     */
    public static CookieMatcher cookieMatcher(
        final CookieMatcher type,
        @DelegatesTo(value = CookieMatcher.class, strategy = DELEGATE_FIRST) final Closure closure
    ) {
        return CookieMatcher.cookieMatcher(ConsumerWithDelegate.create(closure));
    }

    /**
     * Creates a new multipart matcher with a Groovy DSL closure (delegating to <code>MultipartRequestMatcher</code>).
     *
     * @param closure the configuration closure
     * @return a configured matcher instance
     */
    public static MultipartRequestMatcher multipartMatcher(
        final MultipartRequestMatcher type,
        @DelegatesTo(value = MultipartRequestMatcher.class, strategy = DELEGATE_FIRST) final Closure closure
    ) {
        return MultipartRequestMatcher.multipartMatcher(ConsumerWithDelegate.create(closure));
    }
}
