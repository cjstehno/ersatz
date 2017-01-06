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
import com.stehno.ersatz.ResponseEncoders

import java.util.function.Function

/**
 * Created by cjstehno on 1/6/17.
 */
class EncoderChain {
    // FIXME: resolve shared code between encoder/decoder chains

    private final List<ResponseEncoders> items = []

    EncoderChain(final ResponseEncoders firstItem = null) {
        if (firstItem) {
            first firstItem
        }
    }

    // the first item to be checked
    void first(final ResponseEncoders item) {
        items.add(0, item)
    }

    // the last item to be checked
    void last(final ResponseEncoders item) {
        items.add(item)
    }

    void second(final ResponseEncoders item) {
        items.add(items.size() > 0 ? 1 : 0, item)
    }

    Function<Object, String> resolve(final String contentType, final Class objectType) {
        items.findResult { i ->
            i.findEncoder(contentType, objectType)
        }
    }

    Function<Object, String> resolve(final ContentType contentType, final Class objectType) {
        items.findResult { i ->
            i.findEncoder(contentType, objectType)
        }
    }

    ResponseEncoders getAt(int index) {
        items[index]
    }

    int size() {
        items.size()
    }
}