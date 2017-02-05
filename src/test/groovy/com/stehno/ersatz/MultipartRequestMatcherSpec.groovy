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

import org.hamcrest.Description
import org.hamcrest.StringDescription
import spock.lang.Specification

import java.util.function.Consumer

import static com.stehno.ersatz.ContentType.*
import static com.stehno.ersatz.MultipartRequestContent.multipart
import static com.stehno.ersatz.MultipartRequestMatcher.multipartMatcher
import static org.hamcrest.Matchers.*

class MultipartRequestMatcherSpec extends Specification {

    private final MultipartRequestContent content = multipart {
        part 'alpha', 'one'
        part 'bravo', 'bravo.dat', APPLICATION_JSON, '{"label":"This is content!"}'
    }

    def 'matching wrong type'() {
        expect:
        !multipartMatcher {
            part 'alpha', equalTo('one')
        }.matches('This will fail')
    }

    def 'configured with closure'() {
        expect:
        multipartMatcher {
            part 'alpha', equalTo('one')
        }.matches(content)
    }

    def 'configured with consumer'() {
        expect:
        multipartMatcher(new Consumer<MultipartRequestMatcher>() {
            @Override void accept(final MultipartRequestMatcher matcher) {
                matcher.part('alpha', equalTo('one'))
            }
        }).matches(content)
    }

    def 'part(fieldName)'() {
        expect:
        matcher.matches(content) == result

        where:
        matcher                    || result
        newMatcher().part('alpha') || true
        newMatcher().part('zzz')   || false
        newMatcher().part('bravo') || true
    }

    def 'part(fieldName,matcher)'() {
        expect:
        matcher.matches(content) == result

        where:
        matcher                                              || result
        newMatcher().part('alpha', equalTo('one'))           || true
        newMatcher().part('alpha', equalTo('two'))           || false
        newMatcher().part('alpha', startsWith('on'))         || true
        newMatcher().part('alpha', startsWith('z'))          || false
        newMatcher().part('bravo', containsString('is con')) || true
    }

    def 'part(fieldName,string)'() {
        expect:
        matcher.matches(content) == result

        where:
        matcher                           || result
        newMatcher().part('alpha', 'one') || true
        newMatcher().part('alpha', 'two') || false
    }

    def 'part(fieldName,contentType,matcher)'() {
        expect:
        matcher.matches(content) == result

        where:
        matcher                                                                 || result
        newMatcher().part('alpha', startsWith('text/plain'), equalTo('one'))    || true
        newMatcher().part('alpha', startsWith('image/png'), equalTo('one'))     || false
        newMatcher().part('bravo', equalTo('application/json'), notNullValue()) || true
    }

    def 'part(fieldName,String contentType,matcher)'() {
        expect:
        matcher.matches(content) == result

        where:
        matcher                                                        || result
        newMatcher().part('alpha', 'text/plain', equalTo('one'))       || true
        newMatcher().part('alpha', 'image/png', equalTo('one'))        || false
        newMatcher().part('bravo', 'application/json', notNullValue()) || true
    }

    def 'part(fieldName,ContentType,matcher)'() {
        expect:
        matcher.matches(content) == result

        where:
        matcher                                                      || result
        newMatcher().part('alpha', TEXT_PLAIN, equalTo('one'))       || true
        newMatcher().part('alpha', IMAGE_PNG, equalTo('one'))        || false
        newMatcher().part('bravo', APPLICATION_JSON, notNullValue()) || true
    }

    def 'part(fieldName,fileName,contentType,matcher)'() {
        expect:
        matcher.matches(content) == result

        where:
        matcher                                                                                                                   || result
        newMatcher().part('alpha', nullValue(), startsWith('text/plain'), equalTo('one'))                                         || true
        newMatcher().part('alpha', nullValue(), startsWith('text/plain'), equalTo('two'))                                         || false
        newMatcher().part('bravo', equalTo('bravo.dat'), startsWith('application/json'), equalTo('{"label":"This is content!"}')) || true
        newMatcher().part('bravo', equalTo('bravo.dat'), startsWith('application/json'), equalTo('something else'))               || false
        newMatcher().part('bravo', equalTo('bravo.dat'), startsWith('text/plain'), equalTo('{"label":"This is content!"}'))       || false
        newMatcher().part('bravo', endsWith('.json'), startsWith('application/json'), equalTo('{"label":"This is content!"}'))    || false
    }

    def 'part(fieldName,String fileName, String contentType,matcher)'() {
        expect:
        matcher.matches(content) == result

        where:
        matcher                                                                                              || result
        newMatcher().part('bravo', 'bravo.dat', 'application/json', equalTo('{"label":"This is content!"}')) || true
        newMatcher().part('bravo', 'bravo.dat', 'application/json', equalTo('something else'))               || false
        newMatcher().part('bravo', 'bravo.dat', 'text/plain', equalTo('{"label":"This is content!"}'))       || false
    }

    def 'part(fieldName,String fileName, ContentType contentType,matcher)'() {
        expect:
        matcher.matches(content) == result

        where:
        matcher                                                                                            || result
        newMatcher().part('bravo', 'bravo.dat', APPLICATION_JSON, equalTo('{"label":"This is content!"}')) || true
        newMatcher().part('bravo', 'bravo.dat', APPLICATION_JSON, equalTo('something else'))               || false
        newMatcher().part('bravo', 'bravo.dat', TEXT_PLAIN, equalTo('{"label":"This is content!"}'))       || false
    }

    def 'matching description'() {
        setup:
        Description description = new StringDescription()

        when:
        multipartMatcher {
            part 'alpha', equalTo('one')
        }.describeTo(description)

        then:
        description.toString() == 'MultipartRequestMatcher: value("one") '
    }

    private static MultipartRequestMatcher newMatcher() {
        new MultipartRequestMatcher()
    }
}
