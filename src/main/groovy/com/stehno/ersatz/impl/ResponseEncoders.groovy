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
package com.stehno.ersatz.impl

import com.stehno.ersatz.ContentType
import groovy.transform.Immutable
import groovy.transform.TypeChecked

import javax.activation.MimeType
import java.util.function.Function

/**
 * Provides management of response encoders. You may share an instance of this class to define response part encoders across multiple multipart
 * response configurations.
 */
@TypeChecked
class ResponseEncoders {
    // FIXME: pull this into public api

    private final List<EncoderMapping> encoders = []
    private ResponseEncoders parentEncoders

    /**
     * Creates a response encoder collection with optionally specified configuration.
     *
     * @param closure the optional configuration closure
     */
    ResponseEncoders(@DelegatesTo(ResponseEncoders) Closure closure = null) {
        if (closure) {
            closure.delegate = this
            closure.call()
        }
    }

    /**
     * Used to specify a parent encoder collection. If a parent is specified, any find operations will fist look in this collection and then, if no
     * encoder is found, it will check the parent collection.
     *
     * @param parent the parent encoders
     */
    void setParent(final ResponseEncoders parent) {
        this.parentEncoders = parent
    }

    /**
     * Used to register an encoder for a content-type, part object type.
     *
     * @param contentType the part content-type
     * @param objectType the part object type
     * @param encoder the encoder function
     */
    void register(final String contentType, final Class objectType, final Function<Object, String> encoder) {
        encoders << new EncoderMapping(new MimeType(contentType), objectType, encoder)
    }

    /**
     * Used to register an encoder for a content-type, part object type.
     *
     * @param contentType the part content-type
     * @param objectType the part object type
     * @param encoder the encoder function
     */
    void register(final ContentType contentType, final Class objectType, final Function<Object, String> encoder) {
        register contentType.value, objectType, encoder
    }

    /**
     * Used to find an encoder for the given content-type and object type. If a parent is configured on this encoder collection, it will be checked
     * for a match if one is not found in this collection.
     *
     * param contentType the part content-type
     * @param objectType the part object type
     * @return the encoder function if one exists or null
     */
    Function<Object, String> findEncoder(final String contentType, final Class objectType) {
        MimeType mime = new MimeType(contentType)
        Function<Object, String> encoder = encoders.find { em -> em.contentType.match(mime) && em.objectType.isAssignableFrom(objectType) }?.encoder

        encoder ?: (parentEncoders ? parentEncoders.findEncoder(contentType, objectType) : null)
    }

    /**
     * Used to find an encoder for the given content-type and object type. If a parent is configured on this encoder collection, it will be checked
     * for a match if one is not found in this collection.
     *
     * param contentType the part content-type
     * @param objectType the part object type
     * @return the encoder function if one exists or null
     */
    Function<Object, String> findEncoder(final ContentType contentType, final Class objectType) {
        findEncoder contentType.value, objectType
    }

    /**
     * Immutable mapping of a content-type and object type to an encoder.
     */
    @Immutable(knownImmutableClasses = [MimeType, Function])
    private static class EncoderMapping {

        MimeType contentType
        Class objectType
        Function<Object, String> encoder
    }
}
