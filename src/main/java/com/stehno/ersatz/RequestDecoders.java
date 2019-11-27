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

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import space.jasan.support.groovy.closure.ConsumerWithDelegate;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static groovy.lang.Closure.DELEGATE_FIRST;

/**
 * Configuration manager for a collection of request content decoders.
 */
public class RequestDecoders {

    private final List<DecoderMapping> decoders = new LinkedList<>();

    /**
     * Creates a new decoder collection optionally registering decoders with the Groovy DSL closure.
     *
     * @param closure the optional configuration closure
     */
    public RequestDecoders(@DelegatesTo(value = RequestDecoders.class, strategy = DELEGATE_FIRST) Closure closure) {
        if (closure != null) {
            ConsumerWithDelegate.create(closure).accept(this);
        }
    }

    public RequestDecoders() {
        this(null);
    }

    /**
     * Registers the decoder function with the collection
     *
     * @param contentType the content type
     * @param decoder     the decoder function
     */
    public void register(final ContentType contentType, final BiFunction<byte[], DecodingContext, Object> decoder) {
        register(contentType.getValue(), decoder);
    }

    /**
     * Registers the decoder function with the collection
     *
     * @param contentType the content type
     * @param decoder     the decoder function
     */
    public void register(final String contentType, final BiFunction<byte[], DecodingContext, Object> decoder)  {
        decoders.stream()
            .filter(m -> m.mimeType.toString().equals(contentType))
            .findFirst()
            .ifPresent(decoders::remove);

        decoders.add(new DecoderMapping(createMimeType(contentType), decoder));
    }

    /**
     * Finds a decoder for the specified content type.
     *
     * @param contentType the content type
     * @return the decoder function
     */
    public BiFunction<byte[], DecodingContext, Object> findDecoder(final ContentType contentType)  {
        return findDecoder(contentType.getValue());
    }

    /**
     * Finds a decoder for the specified content type.
     *
     * @param contentType the content type
     * @return the decoder function
     */
    public BiFunction<byte[], DecodingContext, Object> findDecoder(final String contentType)  {
        final MimeType mimeType = createMimeType(contentType);

        final List<DecoderMapping> found = decoders.stream()
            .filter(c -> c.mimeType.match(mimeType))
            .collect(Collectors.toList());

        if (found.isEmpty()) {
            return null;

        } else if (found.size() > 1) {
            return found.stream()
                .filter(f -> f.mimeType.toString().equals(contentType))
                .findFirst()
                .map(m -> m.decoder)
                .orElse(null);

        } else {
            return found.get(0).decoder;
        }
    }

    private static MimeType createMimeType(final String value){
        try {
            return new MimeType(value);
        } catch (MimeTypeParseException e) {
            // TODO: better way?
            throw new IllegalArgumentException(e);
        }
    }

    private static class DecoderMapping {

        final MimeType mimeType;
        final BiFunction<byte[], DecodingContext, Object> decoder;

        DecoderMapping(MimeType mimeType, BiFunction<byte[], DecodingContext, Object> decoder) {
            this.mimeType = mimeType;
            this.decoder = decoder;
        }
    }
}