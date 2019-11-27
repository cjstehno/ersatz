/*
 * Copyright (C) 2019 Christopher J. Stehno
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
package com.stehno.ersatz;

import com.stehno.ersatz.util.MimeTypes;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import space.jasan.support.groovy.closure.ConsumerWithDelegate;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import static com.stehno.ersatz.util.MimeTypes.createMimeType;
import static groovy.lang.Closure.DELEGATE_FIRST;

/**
 * Provides management of response encoders. You may share an instance of this class to define response part encoders across multiple multipart
 * response configurations.
 */
public class ResponseEncoders {

    private final List<EncoderMapping> encoders = new LinkedList<>();

    /**
     * Creates a response encoder collection with optionally specified configuration.
     *
     * @param closure the optional configuration closure
     */
    public ResponseEncoders(@DelegatesTo(value = ResponseEncoders.class, strategy = DELEGATE_FIRST) Closure closure) {
        if (closure != null) {
            ConsumerWithDelegate.create(closure).accept(this);
        }
    }

    public ResponseEncoders() {
        this(null);
    }

    /**
     * Used to register an encoder for a content-type, part object type.
     *
     * @param contentType the part content-type
     * @param objectType  the part object type
     * @param encoder     the encoder function
     */
    public void register(final String contentType, final Class objectType, final Function<Object, String> encoder) {
        encoders.add(new EncoderMapping(createMimeType(contentType), objectType, encoder));
    }

    /**
     * Used to register an encoder for a content-type, part object type.
     *
     * @param contentType the part content-type
     * @param objectType  the part object type
     * @param encoder     the encoder function
     */
    public void register(final ContentType contentType, final Class objectType, final Function<Object, String> encoder) {
        register(contentType.getValue(), objectType, encoder);
    }

    /**
     * Used to find an encoder for the given content-type and object type.
     * <p>
     * param contentType the part content-type
     *
     * @param objectType the part object type
     * @return the encoder function if one exists or null
     */
    public Function<Object, String> findEncoder(final String contentType, final Class objectType) {
        final var mime = createMimeType(contentType);

        return encoders.stream()
            .filter(m -> m.contentType.match(mime) && m.objectType.isAssignableFrom(objectType))
            .findFirst()
            .map(m -> m.encoder)
            .orElse(null);
    }

    /**
     * Used to find an encoder for the given content-type and object type.
     * <p>
     * param contentType the part content-type
     *
     * @param objectType the part object type
     * @return the encoder function if one exists or null
     */
    public Function<Object, String> findEncoder(final ContentType contentType, final Class objectType) {
        return findEncoder(contentType.getValue(), objectType);
    }

    /**
     * Immutable mapping of a content-type and object type to an encoder.
     */
    private static class EncoderMapping {

        final MimeType contentType;
        final Class objectType;
        final Function<Object, String> encoder;

        public EncoderMapping(MimeType contentType, Class objectType, Function<Object, String> encoder) {
            this.contentType = contentType;
            this.objectType = objectType;
            this.encoder = encoder;
        }
    }
}
