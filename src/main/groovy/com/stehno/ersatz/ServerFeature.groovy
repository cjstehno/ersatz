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
import io.undertow.server.HttpHandler

/**
 * A <code>ServerFeature</code> provides support for additional functionality on the Ersatz server.
 */
@CompileStatic
interface ServerFeature {

    /**
     * Applies the extended server configuration.
     *
     * @param handler the extension handler
     * @return the wrapped handler
     */
    HttpHandler apply(HttpHandler handler)
}