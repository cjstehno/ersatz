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
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

import static java.util.Collections.emptyList

/**
 * In-memory OkHttp CookieJar Implementation used for testing.
 */
@CompileStatic
class InMemoryCookieJar implements CookieJar {

    private final ConcurrentMap<HttpUrl, List<Cookie>> cookies = new ConcurrentHashMap<>()

    @Override
    void saveFromResponse(HttpUrl url, List<Cookie> cook) {
        cookies.put(url, cook)
    }

    @Override
    List<Cookie> loadForRequest(HttpUrl url) {
        return cookies.getOrDefault(url, (List<Cookie>) emptyList())
    }
}