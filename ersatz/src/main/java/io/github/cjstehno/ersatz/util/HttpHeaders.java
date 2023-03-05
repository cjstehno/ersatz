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

/**
 * Some useful HTTP request/response header names.
 */
public interface HttpHeaders {

    /**
     * Authorization HTTP Header.
     */
    String AUTHORIZATION = "Authorization";

    /**
     * Cache-Control HTTP Header.
     */
    String CACHE_CONTROL = "Cache-Control";

    /**
     * Expires HTTP Header.
     */
    String EXPIRES = "Expires";

    /**
     * Pragma HTTP Header.
     */
    String PRAGMA = "Pragma";

    /**
     * Keep-Alive HTTP Header.
     */
    String KEEP_ALIVE = "Keep-Alive";

    /**
     * Accept-Encoding HTTP Header.
     */
    String ACCEPT_ENCODING = "Accept-Encoding";

    /**
     * Cookie HTTP Header.
     */
    String COOKIE = "Cookie";

    /**
     * Set-Cookie HTTP Header.
     */
    String SET_COOKIE = "Set-Cookie";

    /**
     * Content-Disposition HTTP Header.
     */
    String CONTENT_DISPOSITION = "Content-Disposition";

    /**
     * Content-Length HTTP Header.
     */
    String CONTENT_LENGTH = "Content-Length";

    /**
     * Content-Type HTTP Header.
     */
    String CONTENT_TYPE = "Content-Type";

    /**
     * Content-Encoding HTTP Header.
     */
    String CONTENT_ENCODING = "Content-Encoding";

    /**
     * Location HTTP Header.
     */
    String LOCATION = "Location";

    /**
     * Host HTTP Header.
     */
    String HOST = "Host";

    /**
     * Referer HTTP Header.
     */
    String REFERER = "Referer";

    /**
     * User-Agent HTTP Header.
     */
    String USER_AGENT = "User-Agent";

    /**
     * Allow HTTP Header.
     */
    String ALLOW = "Allow";

    /**
     * Transfer-Encoding HTTP Header.
     */
    String TRANSFER_ENCODING = "Transfer-Encoding";

    /**
     * Date HTTP Header.
     */
    String DATE = "Date";
}
