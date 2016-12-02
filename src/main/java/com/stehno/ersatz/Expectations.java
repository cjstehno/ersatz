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
package com.stehno.ersatz;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

/**
 * Allows configuration of the request/response expectations.
 */
public interface Expectations {

    /**
     * Allows configuration of a GET request.
     *
     * @param path the request path.
     * @return a `Request` configuration object
     */
    Request get(String path);

    Request get(String path, @DelegatesTo(Request.class) Closure closure);

    Request head(String path);

    Request head(String path, @DelegatesTo(Request.class) Closure closure);
}
