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
package com.stehno.ersatz

import spock.lang.Specification

class GetExpectationSpec extends Specification {

    // TODO: this stuff...
    // similar testing in Java JUnit test (?)
    // Request get(String path)
    // Request get(String path, @DelegatesTo(Request) Closure closure)
    // Request get(String path, Consumer<Request> config)

    // FIXME: an empty request (GET/HEAD) should not have a request content type

    /*
        -- requests --

        with(out) headers
        different content types
        with(out) queries (single and map)
        with(out) cookies (single and map)
        listener
        verifier
        responds()
        responder(consumer)
        responder(closure)
        condition(function)
        condition(closure)

        - test closures with external variables

        -- responses --


     */
}
