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
package com.stehno.ersatz.match;

import com.stehno.ersatz.cfg.ContentType;
import com.stehno.ersatz.encdec.MultipartRequestContent;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import space.jasan.support.groovy.closure.ConsumerWithDelegate;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Consumer;

import static groovy.lang.Closure.DELEGATE_FIRST;
import static org.hamcrest.Matchers.*;

/**
 * A Hamcrest matcher used to match <code>MultipartRequestContent</code>. The matcher may be created directly or by using the closure or consumer
 * static configuration methods.
 */
@SuppressWarnings("PMD.BeanMembersShouldSerialize")
public class MultipartRequestMatcher extends BaseMatcher<MultipartRequestContent> {

    private static final String VALUE = "value";
    private static final String CONTENT_TYPE = "contentType";
    private static final String FILE_NAME = "fileName";
    private final Map<String, Map<String, Matcher>> matchers = new LinkedHashMap<>();

    /**
     * Creates a new multipart matcher with a Groovy DSL closure (delegating to <code>MultipartRequestMatcher</code>).
     *
     * @param closure the configuration closure
     * @return a configured matcher instance
     */
    public static MultipartRequestMatcher multipartMatcher(@DelegatesTo(value = MultipartRequestMatcher.class, strategy = DELEGATE_FIRST) final Closure closure) {
        return multipartMatcher(ConsumerWithDelegate.create(closure));
    }

    /**
     * Creates a new multipart matcher with a consumer - it will have an instance of <code>MultipartRequestMatcher</code> passed into it for
     * configuration of the matcher.
     *
     * @param config the configuration consumer
     * @return a configured matcher instance
     */
    public static MultipartRequestMatcher multipartMatcher(final Consumer<MultipartRequestMatcher> config) {
        MultipartRequestMatcher matcher = new MultipartRequestMatcher();
        config.accept(matcher);
        return matcher;
    }

    /**
     * Applies a matcher to match when a part with the specified field name exists in the multipart request (and has a non-null value).
     *
     * @param fieldName the field name
     * @return a reference to this multipart request matcher
     */
    public MultipartRequestMatcher part(final String fieldName) {
        return part(fieldName, notNullValue());
    }

    /**
     * Applies the specified content matcher to the part with the specified field name.
     *
     * @param fieldName the field name
     * @param value     the value matcher
     * @return a reference to this multipart request matcher
     */
    public MultipartRequestMatcher part(final String fieldName, final Matcher<Object> value) {
        matchers.put(fieldName, Map.of(VALUE, value));
        return this;
    }

    /**
     * Applies a matcher for the field part value where the value must be equal to the specified value. This is simply an alias to calling
     * <code>part(fieldName, equalsTo(value))</code>.
     *
     * @param fieldName the field name
     * @param value     the field value
     * @return a reference to this multipart request matcher
     */
    public MultipartRequestMatcher part(final String fieldName, final String value) {
        return part(fieldName, equalTo(value));
    }

    /**
     * Applies the specified content matcher and content-type matcher to the part with the specified field name.
     *
     * @param fieldName   the field name
     * @param contentType the content type matcher
     * @param value       the value matcher
     * @return a reference to this multipart request matcher
     */
    public MultipartRequestMatcher part(final String fieldName, final Matcher<String> contentType, final Matcher<Object> value) {
        matchers.put(fieldName, Map.of("contentType", contentType, VALUE, value));
        return this;
    }

    /**
     * Applies the specified content matcher matcher to the part with the specified field name and where the content type starts with the specified
     * string value. This is analogous to calling <code>part(fieldName, startsWith(contentType), value)</code>.
     *
     * @param fieldName   the field name
     * @param contentType the content type
     * @param value       the value matcher
     * @return a reference to this multipart request matcher
     */
    public MultipartRequestMatcher part(final String fieldName, final String contentType, final Matcher<Object> value) {
        return part(fieldName, startsWith(contentType), value);
    }

    /**
     * Applies the specified content matcher matcher to the part with the specified field name and where the content type starts with the specified
     * string value. This is analogous to calling <code>part(fieldName, startsWith(contentType), value)</code>.
     *
     * @param fieldName   the field name
     * @param contentType the content type
     * @param value       the value matcher
     * @return a reference to this multipart request matcher
     */
    public MultipartRequestMatcher part(final String fieldName, final ContentType contentType, final Matcher<Object> value) {
        return part(fieldName, contentType.getValue(), value);
    }

    /**
     * Applies the specified content matcher, file name matcher and content-type matcher to the part with the specified field name.
     *
     * @param fieldName   the field name
     * @param fileName    the file name matcher
     * @param contentType the content type matcher
     * @param value       the value matcher
     * @return a reference to this multipart request matcher
     */
    public MultipartRequestMatcher part(String fieldName, Matcher<String> fileName, Matcher<String> contentType, Matcher<Object> value) {
        matchers.put(fieldName, Map.of("fileName", fileName, "contentType", contentType, VALUE, value));
        return this;
    }

    /**
     * Applies the value matcher to the part with the specified field name, where the file name must be equal to the given string, and the content
     * type must start with the specified value. This is analogous to calling:
     * <code>part(fieldName, equalTo(fileName), startsWith(contentType), value)</code>
     *
     * @param fieldName   the field name
     * @param fileName    the file name
     * @param contentType the content type
     * @param value       the value matcher
     * @return a reference to this multipart request matcher
     */
    public MultipartRequestMatcher part(String fieldName, String fileName, String contentType, Matcher<Object> value) {
        return part(fieldName, equalTo(fileName), startsWith(contentType), value);
    }

    /**
     * Applies the value matcher to the part with the specified field name, where the file name must be equal to the given string, and the content
     * type must start with the specified value. This is analogous to calling:
     * <code>part(fieldName, equalTo(fileName), startsWith(contentType.value), value)</code>
     *
     * @param fieldName   the field name
     * @param fileName    the file name
     * @param contentType the content type
     * @param value       the value matcher
     * @return a reference to this multipart request matcher
     */
    public MultipartRequestMatcher part(String fieldName, String fileName, ContentType contentType, Matcher<Object> value) {
        return part(fieldName, fileName, contentType.getValue(), value);
    }

    /**
     * Determines whether or not the given object matches the configured matchers.
     *
     * @param item the item being matched (must be an instance of MultipartRequestContent)
     * @return whether or not the match is accepted
     */
    @Override
    public boolean matches(final Object item) {
        if (!(item instanceof MultipartRequestContent)) {
            return false;
        }

        final var results = new LinkedList<Boolean>();

        final MultipartRequestContent mrc = (MultipartRequestContent) item;

        matchers.forEach((fn, matcher) -> {
            final var part = mrc.getAt(fn);
            if (part != null) {
                if (matcher.containsKey(FILE_NAME)) {
                    results.add(matcher.get(FILE_NAME).matches(part.getFileName()));
                }

                if (matcher.containsKey(CONTENT_TYPE)) {
                    results.add(matcher.get(CONTENT_TYPE).matches(part.getContentType()));
                }

                if (matcher.containsKey(VALUE)) {
                    results.add(matcher.get(VALUE).matches(part.getValue()));
                }
            } else {
                results.add(false);
            }
        });

        return results.stream().allMatch(r -> r);
    }

    /**
     * Describes the matcher.
     *
     * @param description the description container
     */
    @Override
    public void describeTo(final Description description) {
        description.appendText("MultipartRequestMatcher: ");

        matchers.forEach((fn, map) -> {
            map.forEach((f, m) -> {
                description.appendText(f + "(");
                m.describeTo(description);
                description.appendText(") ");
            });
        });
    }
}
