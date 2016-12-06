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

import groovy.transform.CompileStatic

/**
 * Used to configure the provided response to an HTTP request.
 */
@CompileStatic
interface Response {

    Response content(final Object content)

    Response content(final Object content, final String contentType)

    Response header(final String name, final String value)

    Response headers(final Map<String, String> headers)

    Response cookie(final String name, final String value)

    Response cookies(final Map<String, String> cookies)

    Response contentType(final String contentType)

    String getContentType()

    Response code(int code)

    Map<String, String> getHeaders()

    Map<String, String> getCookies()

    Object getContent()

    Integer getCode()
}
