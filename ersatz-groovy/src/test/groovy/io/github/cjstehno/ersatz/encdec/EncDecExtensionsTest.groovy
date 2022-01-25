/*
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
package io.github.cjstehno.ersatz.encdec

import io.github.cjstehno.ersatz.encdec.Cookie
import io.github.cjstehno.ersatz.encdec.DecodingContext
import io.github.cjstehno.ersatz.encdec.Encoders
import io.github.cjstehno.ersatz.encdec.MultipartPart
import io.github.cjstehno.ersatz.encdec.MultipartRequestContent
import io.github.cjstehno.ersatz.encdec.MultipartResponseContent
import io.github.cjstehno.ersatz.encdec.RequestDecoders
import io.github.cjstehno.ersatz.encdec.ResponseEncoders
import org.junit.jupiter.api.Test

import java.util.function.BiFunction
import java.util.function.Function

import static io.github.cjstehno.ersatz.cfg.ContentType.*
import static org.junit.jupiter.api.Assertions.*

class EncDecExtensionsTest {

    private static final Function<Object, byte[]> ENCODER_A = o -> new byte[0]
    private static final Function<Object, byte[]> ENCODER_B = o -> new byte[0]
    private static final BiFunction<byte[], DecodingContext, Object> DECODER_A = (b, dc) -> new Object()
    private static final BiFunction<byte[], DecodingContext, Object> DECODER_B = (b, dc) -> new Object()
    private static final Function<Object, byte[]> BYTE_ENCODER = o -> ((String) o).getBytes();

    @Test
    void cookieExtension() {
        def cookie = Cookie.cookie {
            value 'bravo'
            comment 'a cookie'
            domain 'localhost'
            path '/yummy'
            version 1
            httpOnly true
            maxAge 123
            secure true
        }

        assertEquals('bravo', cookie.getValue());
        assertEquals('a cookie', cookie.getComment());
        assertEquals('localhost', cookie.getDomain());
        assertEquals('/yummy', cookie.getPath());
        assertEquals(1, cookie.getVersion());
        assertTrue(cookie.isHttpOnly());
        assertEquals(123, cookie.getMaxAge());
        assertTrue(cookie.isSecure());
    }

    @Test
    void encodersExtension() {
        def encoders = ResponseEncoders.encoders {
            register 'text/plain', String, ENCODER_A
            register IMAGE_GIF, InputStream, ENCODER_B
        }

        assertEquals ENCODER_A, encoders.findEncoder(TEXT_PLAIN, String);
        assertEquals ENCODER_A, encoders.findEncoder('text/plain', String);
        assertNull encoders.findEncoder('text/plain', File);
    }

    @Test
    void decodersExtension() {
        def decoders = RequestDecoders.decoders {
            register TEXT_PLAIN, DECODER_A
            register 'image/png', DECODER_B
        }

        assertEquals DECODER_A, decoders.findDecoder(TEXT_PLAIN)
        assertEquals DECODER_A, decoders.findDecoder('text/plain')
        assertEquals DECODER_B, decoders.findDecoder(IMAGE_PNG)
        assertNull decoders.findDecoder(TEXT_HTML)
    }

    @Test
    void multipartRequestContentExtension() {
        def mrc = MultipartRequestContent.multipartRequest {
            part 'alpha', 'one'
            part 'bravo', 'text/markdown', 'this _is_ *rich^ text'
            part 'charlie', IMAGE_PNG, new byte[]{8, 6, 7, 5, 3, 0, 9}
            part 'delta', 'delta.txt', 'text/markdown', 'this _is_ more text'
            part 'echo', 'some.png', IMAGE_PNG, new byte[]{4, 2}
        }

        assertEquals new MultipartPart('alpha', null, TEXT_PLAIN.getValue(), null, 'one'), mrc['alpha']
        assertEquals new MultipartPart('bravo', null, 'text/markdown', null, 'this _is_ *rich^ text'), mrc['bravo']
        assertEquals new MultipartPart('charlie', null, IMAGE_PNG.getValue(), null, new byte[]{8, 6, 7, 5, 3, 0, 9}), mrc['charlie']
        assertEquals new MultipartPart('delta', 'delta.txt', 'text/markdown', null, 'this _is_ more text'), mrc['delta']
        assertEquals new MultipartPart('echo', 'some.png', IMAGE_PNG.getValue(), null, new byte[]{4, 2}), mrc['echo']
    }

    @Test
    void multipartResponseContentExtension() {
        def mrc = MultipartResponseContent.multipartResponse {
            boundary 'abc123'

            encoder 'text/plain', String, BYTE_ENCODER
            encoder APPLICATION_JSON, String, BYTE_ENCODER
            encoder 'image/jpeg', InputStream, Encoders.binaryBase64

            field 'foo', 'bar'

            part 'alpha', 'text/plain', 'This is some text'
            part 'bravo', APPLICATION_JSON, '{"answer":42}'
            part 'charlie', 'charlie.txt', TEXT_PLAIN, 'This is a text file'
            part 'charlie-2', 'charlie-2.txt', "text/plain", 'This is another text file'
            part 'delta', 'delta.jpg', IMAGE_JPG, new ByteArrayInputStream('fake image content for testing'.getBytes()), 'base64'
        }

        assertEquals 'multipart/mixed; boundary=abc123', mrc.getContentType()

        def expectedLines = '''            --abc123
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
            Content-Disposition: form-data; name="charlie-2"; filename="charlie-2.txt"
            Content-Type: text/plain
            
            This is another text file
            --abc123
            Content-Disposition: form-data; name="delta"; filename="delta.jpg"
            Content-Transfer-Encoding: base64
            Content-Type: image/jpeg
            
            ZmFrZSBpbWFnZSBjb250ZW50IGZvciB0ZXN0aW5n
            --abc123--'''.stripIndent().lines()

        def response = new String(Encoders.multipart.apply(mrc)).lines();

        assertLinesMatch expectedLines, response;
    }
}
