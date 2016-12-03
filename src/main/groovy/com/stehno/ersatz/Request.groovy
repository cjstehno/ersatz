/**
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

import groovy.transform.CompileStatic

import java.util.function.Consumer
import java.util.function.Function

/**
 * Allows configuration of request matchers for expected responses.
 */
@CompileStatic
interface Request {

    Request header(final String name, final String value)

    String header(final String name)

    Request contentType(final String contentType)

    Request query(final String name, final String value)

    List<String> query(final String name)

    Request cookie(final String name, final String value)

    String cookie(final String name)

    Request listener(final Consumer<Request> listener)

    Request verifier(final Function<Integer, Boolean> verifier)

    Response responds()

    Request responder(final Consumer<Response> responder)

    Request responder(@DelegatesTo(Response) final Closure closure)

    Request condition(final Function<Request, Boolean> matcher)
}

/*
    TODO:
        - headers(map)
        - query(map)
        - cookies(map)
 */
