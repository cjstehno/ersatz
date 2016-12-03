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

import com.stehno.ersatz.model.ContentResponse
import groovy.transform.CompileStatic

/**
 * Used to configure the provided response to an HTTP request.
 */
@CompileStatic
interface Response {

    ContentResponse body(final Object content)

    ContentResponse header(final String name, final String value)

    ContentResponse cookie(final String name, final String value)

    ContentResponse contentType(final String contentType)

    ContentResponse code(int code)
}

/*
    TODO:
        - headers(map)
        - cookies(map)
 */