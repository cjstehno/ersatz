/**
 * Copyright (C) 2022 Christopher J. Stehno
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

    String AUTHORIZATION = "Authorization";
    String CACHE_CONTROL = "Cache-Control";
    String EXPIRES = "Expires";
    String PRAGMA = "Pragma";
    String KEEP_ALIVE = "Keep-Alive";
    String ACCEPT_ENCODING = "Accept-Encoding";
    String COOKIE = "Cookie";
    String SET_COOKIE = "Set-Cookie";
    String CONTENT_DISPOSITION = "Content-Disposition";
    String CONTENT_LENGTH = "Content-Length";
    String CONTENT_TYPE = "Content-Type";
    String CONTENT_ENCODING = "Content-Encoding";
    String LOCATION = "Location";
    String HOST = "Host";
    String REFERER = "Referer";
    String USER_AGENT = "User-Agent";
    String ALLOW = "Allow";
    String TRANSFER_ENCODING = "Transfer-Encoding";
    String DATE = "Date";
}
