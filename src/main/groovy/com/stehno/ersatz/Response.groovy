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

/**
 * Used to configure the provided response to an HTTP request.
 */
@CompileStatic
interface Response {

    Response body(final Object content)

    Response header(final String name, final String value)

    Response cookie(final String name, final String value)

    Response contentType(final String contentType)

    Response code(int code)

    Map<String, String> getHeaders()

    Map<String, String> getCookies()

    Object getBody()

    Integer getCode()
}

/*
    TODO:
        - headers(map)
        - cookies(map)
 */