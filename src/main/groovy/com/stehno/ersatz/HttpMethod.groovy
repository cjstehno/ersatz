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

import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor

/**
 * Enumeration of the supported HTTP request methods.
 */
@CompileStatic @TupleConstructor
enum HttpMethod {

    ANY('*'),
    GET('GET'),
    HEAD('HEAD'),
    POST('POST'),
    PUT('PUT'),
    DELETE('DELETE'),
    PATCH('PATCH'),
    OPTIONS('OPTIONS'),
    TRACE('TRACE')

    final String value

    @Override String toString() {
        value
    }
}