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
package com.stehno.ersatz.impl

import com.stehno.ersatz.Encoders
import com.stehno.ersatz.MultipartResponseContent
import spock.lang.Specification

import java.util.function.Consumer

import static com.stehno.ersatz.ContentType.*

class ErsatzMultipartResponseContentSpec extends Specification {


    private static final List<String> MULTIPART_RESPONSE_TEXT = '''
            --abc123
            Content-Disposition: form-data; name="foo"
            Content-Type: text/plain
            
            bar
            --abc123
            Content-Disposition: form-data; name="alpha"
            Content-Type: text/plain
            
            This is some text
            --abc123
            Content-Disposition: form-data; name="bravo"
            Content-Type: application/json
            
            {"answer":42}
            --abc123
            Content-Disposition: form-data; name="charlie"; filename="charlie.txt"
            Content-Type: text/plain
            
            This is a text file
            --abc123
            Content-Disposition: form-data; name="delta"; filename="delta.jpg"
            Content-Transfer-Encoding: base64
            Content-Type: image/jpeg
            
            ZmFrZSBpbWFnZSBjb250ZW50IGZvciB0ZXN0aW5n
            --abc123--
        '''.stripIndent().trim().readLines().asImmutable()

    def 'multipart: closure'() {
        when:
        MultipartResponseContent mc = MultipartResponseContent.multipart {
            boundary 'abc123'

            encoder 'text/plain', String, { o -> o as String }
            encoder APPLICATION_JSON, String, { o -> o as String }
            encoder 'image/jpeg', InputStream, Encoders.binaryBase64

            field 'foo', 'bar'

            part 'alpha', 'text/plain', 'This is some text'
            part 'bravo', APPLICATION_JSON, '{"answer":42}'
            part 'charlie', 'charlie.txt', TEXT_PLAIN, 'This is a text file'
            part 'delta', 'delta.jpg', IMAGE_JPG, new ByteArrayInputStream('fake image content for testing'.bytes), 'base64'
        }

        then:
        mc.contentType == 'multipart/mixed; boundary=abc123'

        and:
        Encoders.multipart.apply(mc).trim().readLines() == MULTIPART_RESPONSE_TEXT
    }

    def 'multipart: consumer'() {
        when:

        MultipartResponseContent mc = MultipartResponseContent.multipart(new Consumer<MultipartResponseContent>() {
            @Override void accept(final MultipartResponseContent mult) {
                mult.boundary 'abc123'

                mult.encoder 'text/plain', String, { o -> o as String }
                mult.encoder APPLICATION_JSON, String, { o -> o as String }
                mult.encoder 'image/jpeg', InputStream, Encoders.binaryBase64

                mult.field 'foo', 'bar'

                mult.part 'alpha', 'text/plain', 'This is some text'
                mult.part 'bravo', APPLICATION_JSON, '{"answer":42}'
                mult.part 'charlie', 'charlie.txt', TEXT_PLAIN, 'This is a text file'
                mult.part 'delta', 'delta.jpg', IMAGE_JPG, new ByteArrayInputStream('fake image content for testing'.bytes), 'base64'
            }
        })

        then:
        mc.contentType == 'multipart/mixed; boundary=abc123'

        and:
        Encoders.multipart.apply(mc).trim().readLines() == MULTIPART_RESPONSE_TEXT
    }
}
