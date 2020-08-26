/*
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
package com.stehno.ersatz.match;


import com.stehno.ersatz.encdec.MultipartRequestContent;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.stehno.ersatz.cfg.ContentType.*;
import static com.stehno.ersatz.encdec.MultipartRequestContent.multipartRequest;
import static com.stehno.ersatz.match.MultipartRequestMatcher.multipartMatcher;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class MultipartRequestMatcherTest {

    private MultipartRequestContent content;

    @BeforeEach void beforeEach() {
        content = multipartRequest(mrc -> {
            mrc.part("alpha", "one");
            mrc.part("bravo", "bravo.dat", APPLICATION_JSON, "{\"label\":\"This is content!\"}");
        });
    }

    @Test @DisplayName("matching wrong type")
    void matchingWrongType() {
        assertFalse(
            multipartMatcher(mrm -> mrm.part("alpha", equalTo("one"))).matches("This will fail")
        );
    }

    @Test @DisplayName("configured")
    void configuredWithClosure() {
        assertTrue(
            multipartMatcher(mrm -> mrm.part("alpha", equalTo("one"))).matches(content)
        );
    }

    @ParameterizedTest @DisplayName("part(field-name)")
    @CsvSource({
        "alpha,true",
        "zzz,false",
        "bravo,true"
    })
    void partWithFieldName(final String name, final boolean result) {
        assertEquals(result, newMatcher().part(name).matches(content));
    }

    @ParameterizedTest @DisplayName("part(fieldName,matcher)") @MethodSource("partFieldMatcherProvider")
    void partFieldMatcher(final String name, Matcher<Object> m, final boolean result) {
        assertEquals(result, newMatcher().part(name, m).matches(content));
    }

    private static Stream<Arguments> partFieldMatcherProvider() {
        return Stream.of(
            arguments("alpha", equalTo("one"), true),
            arguments("alpha", equalTo("two"), false),
            arguments("alpha", startsWith("on"), true),
            arguments("alpha", startsWith("z"), false),
            arguments("bravo", containsString("is con"), true)
        );
    }

    @ParameterizedTest @DisplayName("part(fieldName,string)")
    @CsvSource({
        "alpha,one,true",
        "alpha,two,false"
    })
    void partFieldString(final String name, final String value, final boolean result) {
        assertEquals(result, newMatcher().part(name, value).matches(content));
    }

    @ParameterizedTest @DisplayName("part(fieldName,contentType,matcher)")
    @MethodSource("partFieldContentMatcherProvider")
    void partFieldContentMatcher(String name, Matcher<String> contentMatcher, Matcher<Object> valueMatcher, boolean result) {
        assertEquals(result, newMatcher().part(name, contentMatcher, valueMatcher).matches(content));
    }

    private static Stream<Arguments> partFieldContentMatcherProvider() {
        return Stream.of(
            arguments("alpha", startsWith("text/plain"), equalTo("one"), true),
            arguments("alpha", startsWith("image/png"), equalTo("one"), false),
            arguments("bravo", equalTo("application/json"), notNullValue(), true)
        );
    }

    @ParameterizedTest @DisplayName("part(fieldName,fileName,contentType,matcher)")
    @MethodSource("partEverythingProvider")
    void partFieldFileContentMatcher(Matcher matcher, boolean result) {
        assertEquals(result, matcher.matches(content));
    }

    private static Stream<Arguments> partEverythingProvider() {
        return Stream.of(
            arguments(newMatcher().part("alpha", blankOrNullString(), startsWith("text/plain"), equalTo("one")), true),
            arguments(newMatcher().part("alpha", blankOrNullString(), startsWith("text/plain"), equalTo("two")), false),
            arguments(newMatcher().part("bravo", equalTo("bravo.dat"), startsWith("application/json"), equalTo("{\"label\":\"This is content!\"}")), true),
            arguments(newMatcher().part("bravo", equalTo("bravo.dat"), startsWith("application/json"), equalTo("something else")), false),
            arguments(newMatcher().part("bravo", equalTo("bravo.dat"), startsWith("text/plain"), equalTo("{\"label\":\"This is content!\"}")), false),
            arguments(newMatcher().part("bravo", endsWith(".json"), startsWith("application/json"), equalTo("{\"label\":\"This is content!\"}")), false),

            arguments(newMatcher().part("bravo", "bravo.dat", "application/json", equalTo("{\"label\":\"This is content!\"}")), true),
            arguments(newMatcher().part("bravo", "bravo.dat", "application/json", equalTo("something else")), false),
            arguments(newMatcher().part("bravo", "bravo.dat", "text/plain", equalTo("{\"label\":\"This is content!\"}")), false),

            arguments(newMatcher().part("bravo", "bravo.dat", APPLICATION_JSON, equalTo("{\"label\":\"This is content!\"}")), true),
            arguments(newMatcher().part("bravo", "bravo.dat", APPLICATION_JSON, equalTo("something else")), false),
            arguments(newMatcher().part("bravo", "bravo.dat", TEXT_PLAIN, equalTo("{\"label\":\"This is content!\"}")), false),

            arguments(newMatcher().part("alpha", "text/plain", equalTo("one")), true),
            arguments(newMatcher().part("alpha", "image/png", equalTo("one")), false),
            arguments(newMatcher().part("bravo", "application/json", notNullValue()), true),

            arguments(newMatcher().part("alpha", TEXT_PLAIN, equalTo("one")), true),
            arguments(newMatcher().part("alpha", IMAGE_PNG, equalTo("one")), false),
            arguments(newMatcher().part("bravo", APPLICATION_JSON, notNullValue()), true)
        );
    }

    @Test @DisplayName("matching description")
    void matchingDescription() {
        Description description = new StringDescription();

        multipartMatcher(mrm -> {
            mrm.part("alpha", equalTo("one"));
        }).describeTo(description);

        assertEquals("MultipartRequestMatcher: value(\"one\") ", description.toString());
    }

    private static MultipartRequestMatcher newMatcher() {
        return new MultipartRequestMatcher();
    }
}
