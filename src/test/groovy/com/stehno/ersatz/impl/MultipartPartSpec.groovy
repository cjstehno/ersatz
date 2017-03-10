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

import spock.lang.Specification

import static com.stehno.vanilla.test.Assertions.assertValidEqualsAndHashcode

class MultipartPartSpec extends Specification {

    def 'properties'(){
        when:
        def part = new MultipartPart('file','file.txt', 'text/plain', 'binary', 'some file')

        then:
        part.fieldName == 'file'
        part.fileName == 'file.txt'
        part.contentType == 'text/plain'
        part.transferEncoding == 'binary'
        part.value == 'some file'
    }

    def 'equals and hash'(){
        assertValidEqualsAndHashcode(
            new MultipartPart('file','file.txt', 'text/plain', 'binary', 'some file'),
            new MultipartPart('file','file.txt', 'text/plain', 'binary', 'some file'),
            new MultipartPart('file','file.txt', 'text/plain', 'binary', 'some file'),
            new MultipartPart('file','file.bin', 'text/plain', 'text', 'other file')
        )
    }
}
