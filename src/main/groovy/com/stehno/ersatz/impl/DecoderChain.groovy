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
package com.stehno.ersatz.impl

import com.stehno.ersatz.ContentType
import com.stehno.ersatz.DecodingContext
import com.stehno.ersatz.RequestDecoders
import groovy.transform.CompileStatic

import java.util.function.BiFunction

/**
 * FIXME: document
 *
 * operations are fired on items from first --> last until non-null result is found
 */
@CompileStatic
class DecoderChain {

    private final List<RequestDecoders> items = []

    DecoderChain(final RequestDecoders firstItem = null) {
        if (firstItem) first firstItem
    }

    // the first item to be checked
    void first(final RequestDecoders item) {
        items.add(0, item)
    }

    // the last item to be checked
    void last(final RequestDecoders item) {
        items.add(item)
    }

    void afterFirst(final RequestDecoders item) {
        items.add(items.size() > 0 ? 1 : 0, item)
    }

    BiFunction<byte[], DecodingContext, Object> resolve(final String contentType) {
        items.findResult { i ->
            i.findDecoder(contentType)
        }
    }

    BiFunction<byte[], DecodingContext, Object> resolve(final ContentType contentType) {
        items.findResult { i ->
            i.findDecoder(contentType)
        }
    }

    RequestDecoders getAt(int index) {
        items[index]
    }

    int size() {
        items.size()
    }
}