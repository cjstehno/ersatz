/**
 * Copyright (C) 2023 Christopher J. Stehno
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
package io.github.cjstehno.ersatz.util;

import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

/**
 * Some useful HTTP request/response header names.
 */
@NoArgsConstructor(access = PRIVATE)
public final class HttpHeaders {

    /**
     * Authorization HTTP Header.
     */
    public static final String AUTHORIZATION = "Authorization";

    /**
     * Cache-Control HTTP Header.
     */
    public static final String CACHE_CONTROL = "Cache-Control";

    /**
     * Expires HTTP Header.
     */
    public static final String EXPIRES = "Expires";

    /**
     * Pragma HTTP Header.
     */
    public static final String PRAGMA = "Pragma";

    /**
     * Keep-Alive HTTP Header.
     */
    public static final String KEEP_ALIVE = "Keep-Alive";

    /**
     * Accept-Encoding HTTP Header.
     */
    public static final String ACCEPT_ENCODING = "Accept-Encoding";

    /**
     * Cookie HTTP Header.
     */
    public static final String COOKIE = "Cookie";

    /**
     * Set-Cookie HTTP Header.
     */
    public static final String SET_COOKIE = "Set-Cookie";

    /**
     * Content-Disposition HTTP Header.
     */
    public static final String CONTENT_DISPOSITION = "Content-Disposition";

    /**
     * Content-Length HTTP Header.
     */
    public static final String CONTENT_LENGTH = "Content-Length";

    /**
     * Content-Type HTTP Header.
     */
    public static final String CONTENT_TYPE = "Content-Type";

    /**
     * Content-Encoding HTTP Header.
     */
    public static final String CONTENT_ENCODING = "Content-Encoding";

    /**
     * Location HTTP Header.
     */
    public static final String LOCATION = "Location";

    /**
     * Host HTTP Header.
     */
    public static final String HOST = "Host";

    /**
     * Referer HTTP Header.
     */
    public static final String REFERER = "Referer";

    /**
     * User-Agent HTTP Header.
     */
    public static final String USER_AGENT = "User-Agent";

    /**
     * Allow HTTP Header.
     */
    public static final String ALLOW = "Allow";

    /**
     * Transfer-Encoding HTTP Header.
     */
    public static final String TRANSFER_ENCODING = "Transfer-Encoding";

    /**
     * Date HTTP Header.
     */
    public static final String DATE = "Date";
}
