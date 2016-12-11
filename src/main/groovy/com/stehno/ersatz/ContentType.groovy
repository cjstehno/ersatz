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
import groovy.transform.Immutable

/**
 * A content-type representation. Some of the standard content-types are provided as static constants for use elsewhere.
 */
@Immutable @CompileStatic @SuppressWarnings('GroovyUnusedDeclaration')
class ContentType {

    static final ContentType TEXT_PLAIN = new ContentType('text/plain')
    static final ContentType TEXT_HTML = new ContentType('text/html')
    static final ContentType TEXT_JAVASCRIPT = new ContentType('text/javascript')
    static final ContentType APPLICATION_JAVASCRIPT = new ContentType('application/javascript')
    static final ContentType TEXT_XML = new ContentType('text/xml')
    static final ContentType APPLICATION_XML = new ContentType('application/xml')
    static final ContentType APPLICATION_JSON = new ContentType('application/json')
    static final ContentType TEXT_JSON = new ContentType('text/json')
    static final ContentType APPLICATION_URLENCODED = new ContentType('application/x-www-form-urlencoded')

    String value
}
