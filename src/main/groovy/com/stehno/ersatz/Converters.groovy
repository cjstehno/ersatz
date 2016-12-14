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

import groovy.json.JsonSlurper
import groovy.transform.CompileStatic

import java.util.function.Function

import static java.net.URLDecoder.decode
import static java.nio.charset.StandardCharsets.UTF_8

/**
 * Reusable request content converter functions.
 */
@CompileStatic @SuppressWarnings('PropertyName')
class Converters {

    /**
     * Converts request content bytes into a UTF-8 string.
     */
    static final Function<byte[], Object> utf8String = { byte[] m -> m ? new String(m, UTF_8) : '' } as Function<byte[], Object>

    /**
     * Converts request content bytes into a string of JSON and then parses it with <code>JsonSlurper</code> to return
     * parsed JSON data.
     */
    static final Function<byte[], Object> parseJson = { byte[] m -> new JsonSlurper().parse(m ?: '{}'.bytes) } as Function<byte[], Object>

    /**
     * Converts request content bytes in a url-encoded format into a map of name/value pairs.
     */
    static final Function<byte[], Object> urlEncoded = { byte[] m ->
        if (m) {
            return new String(m, UTF_8).split('&').collectEntries { String nvp ->
                String[] parts = nvp.split('=')
                [decode(parts[0], UTF_8.name()), decode(parts[1], UTF_8.name())]
            }
        }

        return [:]
    } as Function<byte[], Object>
}
