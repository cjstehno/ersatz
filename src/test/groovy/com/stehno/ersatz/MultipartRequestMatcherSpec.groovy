package com.stehno.ersatz

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

    private static MultipartRequestMatcher newMatcher() {
        new MultipartRequestMatcher()
    }
}
