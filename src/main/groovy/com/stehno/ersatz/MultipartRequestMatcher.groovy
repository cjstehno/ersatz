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
package com.stehno.ersatz

import groovy.transform.CompileStatic
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher

import java.util.function.Consumer

import static org.hamcrest.Matchers.*

/**
 * A Hamcrest matcher used to match <code>MultipartRequestContent</code>. The matcher may be created directly or by using the closure or consumer
 * static configuration methods.
 */
@CompileStatic
class MultipartRequestMatcher extends BaseMatcher<MultipartRequestContent> {

    private final Map<String, Map<String, Matcher>> matchers = [:]

    /**
     * Creates a new multipart matcher with a Groovy DSL closure (delegating to <code>MultipartRequestMatcher</code>).
     *
     * @param the configuration closure
     * @return a configured matcher instance
     */
    static MultipartRequestMatcher multipartMatcher(@DelegatesTo(MultipartRequestMatcher) final Closure closure) {
        MultipartRequestMatcher matcher = new MultipartRequestMatcher()
        closure.delegate = matcher
        closure.call()
        matcher
    }

    /**
     * Creates a new multipart matcher with a consumer - it will have an instance of <code>MultipartRequestMatcher</code> passed into it for
     * configuration of the matcher.
     *
     * @param the configuration consumer
     * @return a configured matcher instance
     */
    static MultipartRequestMatcher multipartMatcher(final Consumer<MultipartRequestMatcher> config) {
        MultipartRequestMatcher matcher = new MultipartRequestMatcher()
        config.accept(matcher)
        matcher
    }

    /**
     * Applies a matcher to match when a part with the specified field name exists in the multipart request (and has a non-null value).
     *
     * @param fieldName the field name
     * @return a reference to this multipart request matcher
     */
    MultipartRequestMatcher part(final String fieldName) {
        part(fieldName, notNullValue())
    }

    /**
     * Applies the specified content matcher to the part with the specified field name.
     *
     * @param fieldName the field name
     * @param value the value matcher
     * @return a reference to this multipart request matcher
     */
    MultipartRequestMatcher part(final String fieldName, final Matcher<Object> value) {
        matchers[fieldName] = [value: value]
        this
    }

    /**
     * Applies a matcher for the field part value where the value must be equal to the specified value. This is simply an alias to calling
     * <code>part(fieldName, equalsTo(value))</code>.
     *
     * @param fieldName the field name
     * @param value the field value
     * @return a reference to this multipart request matcher
     */
    MultipartRequestMatcher part(final String fieldName, final String value) {
        part fieldName, equalTo(value) as Matcher<Object>
    }

    /**
     * Applies the specified content matcher and content-type matcher to the part with the specified field name.
     *
     * @param fieldName the field name
     * @param contentType the content type matcher
     * @param value the value matcher
     * @return a reference to this multipart request matcher
     */
    MultipartRequestMatcher part(final String fieldName, final Matcher<String> contentType, final Matcher<Object> value) {
        matchers[fieldName] = [contentType: contentType, value: value]
        this
    }

    /**
     * Applies the specified content matcher matcher to the part with the specified field name and where the content type starts with the specified
     * string value. This is analogous to calling <code>part(fieldName, startsWith(contentType), value)</code>.
     *
     * @param fieldName the field name
     * @param contentType the content type
     * @param value the value matcher
     * @return a reference to this multipart request matcher
     */
    MultipartRequestMatcher part(final String fieldName, final String contentType, final Matcher<Object> value) {
        part fieldName, startsWith(contentType), value
    }

    /**
     * Applies the specified content matcher matcher to the part with the specified field name and where the content type starts with the specified
     * string value. This is analogous to calling <code>part(fieldName, startsWith(contentType), value)</code>.
     *
     * @param fieldName the field name
     * @param contentType the content type
     * @param value the value matcher
     * @return a reference to this multipart request matcher
     */
    MultipartRequestMatcher part(final String fieldName, final ContentType contentType, final Matcher<Object> value) {
        part fieldName, contentType.value, value
    }

    /**
     * Applies the specified content matcher, file name matcher and content-type matcher to the part with the specified field name.
     *
     * @param fieldName the field name
     * @param fileName the file name matcher
     * @param contentType the content type matcher
     * @param value the value matcher
     * @return a reference to this multipart request matcher
     */
    MultipartRequestMatcher part(String fieldName, Matcher<String> fileName, Matcher<String> contentType, Matcher<Object> value) {
        matchers[fieldName] = [fileName: fileName, contentType: contentType, value: value]
        this
    }

    /**
     * Applies the value matcher to the part with the specified field name, where the file name must be equal to the given string, and the content
     * type must start with the specified value. This is analogous to calling:
     * <code>part(fieldName, equalTo(fileName), startsWith(contentType), value)</code>
     *
     * @param fieldName the field name
     * @param fileName the file name
     * @param contentType the content type
     * @param value the value matcher
     * @return a reference to this multipart request matcher
     */
    MultipartRequestMatcher part(String fieldName, String fileName, String contentType, Matcher<Object> value) {
        part fieldName, equalTo(fileName), startsWith(contentType), value
    }

    /**
     * Applies the value matcher to the part with the specified field name, where the file name must be equal to the given string, and the content
     * type must start with the specified value. This is analogous to calling:
     * <code>part(fieldName, equalTo(fileName), startsWith(contentType.value), value)</code>
     *
     * @param fieldName the field name
     * @param fileName the file name
     * @param contentType the content type
     * @param value the value matcher
     * @return a reference to this multipart request matcher
     */
    MultipartRequestMatcher part(String fieldName, String fileName, ContentType contentType, Matcher<Object> value) {
        part fieldName, fileName, contentType.value, value
    }

    /**
     * Determines whether or not the given object matches the configured matchers.
     *
     * @param item the item being matched (must be an instance of MultipartRequestContent)
     * @return whether or not the match is accepted
     */
    @Override
    boolean matches(final Object item) {
        if (!(item instanceof MultipartRequestContent)) {
            return false
        }

        def results = []

        MultipartRequestContent mrc = item as MultipartRequestContent

        matchers.each { String fn, Map<String, Matcher> matcher ->
            def part = mrc[fn]

            if (part) {
                if (matcher.fileName) {
                    results << matcher.fileName.matches(part.fileName)
                }

                if (matcher.contentType) {
                    results << matcher.contentType.matches(part.contentType)
                }

                if (matcher.value) {
                    results << matcher.value.matches(part.value)
                }
            } else {
                results << false
            }
        }

        results.every()
    }

    /**
     * Describes the matcher.
     *
     * @param description the description container
     */
    @Override
    void describeTo(final Description description) {
        description.appendText('MultipartRequestMatcher: ')

        matchers.each { fn, map ->
            map.each { f, m ->
                description.appendText("${f}(")
                m.describeTo(description)
                description.appendText(') ')
            }
        }
    }
}
