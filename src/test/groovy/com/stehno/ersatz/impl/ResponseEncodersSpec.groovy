/*
 * Copyright (C) 2016 Christopher J. Stehno
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

import com.stehno.ersatz.ResponseEncoders
import spock.lang.Specification

import java.util.function.Function

import static com.stehno.ersatz.ContentType.*

class ResponseEncodersSpec extends Specification {

    private static final Function<Object, String> ENCODER_A = { o -> }
    private static final Function<Object, String> ENCODER_B = { o -> }
    private static final Function<Object, String> ENCODER_C = { o -> }

    def 'no parent'() {
        setup:
        ResponseEncoders encoders = new ResponseEncoders({
            register 'text/plain', String, ENCODER_A
            register IMAGE_GIF, InputStream, ENCODER_B
        })

        expect:
        encoders.findEncoder(TEXT_PLAIN, String) == ENCODER_A

        and:
        encoders.findEncoder('text/plain', String) == ENCODER_A

        and:
        !encoders.findEncoder('text/plain', File)
    }

    def 'with parent'() {
        setup:
        ResponseEncoders encoders = new ResponseEncoders({
            register 'text/plain', String, ENCODER_C
            register APPLICATION_JSON, File, ENCODER_B
        })

        encoders.parent = new ResponseEncoders({
            register 'text/plain', String, ENCODER_A
            register IMAGE_GIF, InputStream, ENCODER_B
        })

        expect:
        encoders.findEncoder(TEXT_PLAIN, String) == ENCODER_C

        and:
        encoders.findEncoder('text/plain', String) == ENCODER_C

        and:
        !encoders.findEncoder('text/plain', File)

        and:
        encoders.findEncoder(APPLICATION_JSON, File) == ENCODER_B

        and:
        encoders.findEncoder('image/gif', InputStream) == ENCODER_B
    }
}
