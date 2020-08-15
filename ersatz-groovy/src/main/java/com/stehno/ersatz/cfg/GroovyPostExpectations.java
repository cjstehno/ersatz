/**
 * Copyright (C) 2020 Christopher J. Stehno
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
package com.stehno.ersatz.cfg;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.hamcrest.Matcher;
import space.jasan.support.groovy.closure.ConsumerWithDelegate;

import static com.stehno.ersatz.match.ErsatzMatchers.pathMatcher;
import static groovy.lang.Closure.DELEGATE_FIRST;

public interface GroovyPostExpectations extends PostExpectations {

    /**
     * Allows configuration of a POST request expectation using the Groovy DSL.
     *
     * @param path    the expected request path
     * @param closure the Groovy closure containing the configuration
     * @return a <code>RequestWithContent</code> configuration object
     */
    default GroovyRequestWithContent POST(String path, @DelegatesTo(value = GroovyRequestWithContent.class, strategy = DELEGATE_FIRST) Closure closure) {
        return POST(pathMatcher(path), closure);
    }

    /**
     * Allows configuration of a POST request expectation using the Groovy DSL.
     *
     * @param matcher the path matcher
     * @param closure the Groovy closure containing the configuration
     * @return a <code>RequestWithContent</code> configuration object
     */
    default GroovyRequestWithContent POST(Matcher<String> matcher, @DelegatesTo(value = GroovyRequestWithContent.class, strategy = DELEGATE_FIRST) Closure closure) {
        return (GroovyRequestWithContent) POST(matcher, ConsumerWithDelegate.create(closure));
    }
}
