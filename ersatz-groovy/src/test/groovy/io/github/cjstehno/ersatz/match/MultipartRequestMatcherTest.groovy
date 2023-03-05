/*
 * Copyright (C) 2023 Christopher J. Stehno
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
package io.github.cjstehno.ersatz.match

import io.github.cjstehno.ersatz.encdec.MultipartRequestContent
import io.github.cjstehno.ersatz.match.MultipartRequestMatcher
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MultipartRequestMatcherTest {

    @Test @DisplayName('multipart request matching')
    void multipartRequestMatching(){
        def matcher = MultipartRequestMatcher.multipartMatcher {
            part 'something', 'some-value'
        }

        assertTrue matcher.matches(MultipartRequestContent.multipartRequest {
            part 'something', 'some-value'
        })

        assertFalse matcher.matches(MultipartRequestContent.multipartRequest {
            part 'something', 'other-value'
        })
    }
}