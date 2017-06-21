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
import groovy.transform.Immutable

/**
 * A content-type representation. Some of the standard content-types are provided as static constants for use elsewhere, others may be created as
 * instances of this class as needed.
 */
@Immutable @CompileStatic @SuppressWarnings('GroovyUnusedDeclaration')
class ContentType {

    public static final String CONTENT_TYPE_HEADER = 'Content-Type'

    public static final ContentType TEXT_PLAIN = new ContentType('text/plain')
    public static final ContentType TEXT_HTML = new ContentType('text/html')
    public static final ContentType TEXT_JAVASCRIPT = new ContentType('text/javascript')
    public static final ContentType APPLICATION_JAVASCRIPT = new ContentType('application/javascript')
    public static final ContentType TEXT_XML = new ContentType('text/xml')
    public static final ContentType APPLICATION_XML = new ContentType('application/xml')
    public static final ContentType APPLICATION_JSON = new ContentType('application/json')
    public static final ContentType TEXT_JSON = new ContentType('text/json')
    public static final ContentType APPLICATION_URLENCODED = new ContentType('application/x-www-form-urlencoded')
    public static final ContentType MULTIPART_FORMDATA = new ContentType('multipart/form-data')
    public static final ContentType MULTIPART_MIXED = new ContentType('multipart/mixed')
    public static final ContentType IMAGE_JPG = new ContentType('image/jpeg')
    public static final ContentType IMAGE_PNG = new ContentType('image/png')
    public static final ContentType IMAGE_GIF = new ContentType('image/gif')
    public static final ContentType MESSAGE_HTTP = new ContentType('message/http')

    String value
}
