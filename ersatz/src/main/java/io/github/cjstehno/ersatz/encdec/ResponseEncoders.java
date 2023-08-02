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
package io.github.cjstehno.ersatz.encdec;

import io.github.cjstehno.ersatz.cfg.ContentType;
import lombok.RequiredArgsConstructor;
import lombok.val;

import javax.activation.MimeType;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.github.cjstehno.ersatz.encdec.MimeTypes.createMimeType;

/**
 * Provides management of response encoders. You may share an instance of this class to define response part encoders across multiple multipart
 * response configurations.
 */
public class ResponseEncoders {

    private final List<EncoderMapping> encoders = new LinkedList<>();

    /**
     * Creates a response encoder container configured by the provided consumer.
     *
     * @param consumer the configuration consumer
     * @return the configured response encoders
     */
    public static ResponseEncoders encoders(final Consumer<ResponseEncoders> consumer) {
        final var encoders = new ResponseEncoders();
        consumer.accept(encoders);
        return encoders;
    }

    /**
     * Used to register an encoder for a content-type, part object type.
     *
     * @param contentType the part content-type
     * @param objectType  the part object type
     * @param encoder     the encoder function
     */
    public void register(final String contentType, final Class objectType, final Function<Object, byte[]> encoder) {
        encoders.add(new EncoderMapping(createMimeType(contentType), objectType, encoder));
    }

    /**
     * Used to register an encoder for a content-type, part object type.
     *
     * @param contentType the part content-type
     * @param objectType  the part object type
     * @param encoder     the encoder function
     */
    public void register(final ContentType contentType, final Class objectType, final Function<Object, byte[]> encoder) {
        register(contentType.getValue(), objectType, encoder);
    }

    /**
     * Used to find an encoder for the given content-type and object type.
     *
     * @param contentType the part content-type
     * @param objectType  the part object type
     * @return the encoder function if one exists or null
     */
    public Function<Object, byte[]> findEncoder(final String contentType, final Class objectType) {
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
     * @param contentType the response content type to be encoded
     * @param objectType  the part object type
     * @return the encoder function if one exists or null
     */
    public Function<Object, byte[]> findEncoder(final ContentType contentType, final Class objectType) {
        return findEncoder(contentType.getValue(), objectType);
    }

    /**
     * Merges the given encoders into the existing encoder set. Any incoming encoders that match the conten
     * type and object type will be overwritten.
     *
     * @param otherEncoders the incoming encoders
     */
    public void merge(final ResponseEncoders otherEncoders) {
        for (val encoder : otherEncoders.encoders) {
            val existingIndex = indexOfEncoder(encoder.contentType, encoder.objectType);
            if (existingIndex != -1) {
                // replace the existing one
                encoders.set(existingIndex, encoder);
            } else {
                // add the new one
                encoders.add(encoder);
            }
        }
    }

    private int indexOfEncoder(final MimeType mimeType, final Class objectType) {
        for (int i = 0; i < encoders.size(); i++) {
            val mapping = encoders.get(i);
            if (mapping.contentType.match(mimeType) && mapping.objectType.isAssignableFrom(objectType)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Immutable mapping of a content-type and object type to an encoder.
     */
    @RequiredArgsConstructor @SuppressWarnings("ClassCanBeRecord")
    private static class EncoderMapping {

        private final MimeType contentType;
        private final Class objectType;
        private final Function<Object, byte[]> encoder;
    }
}
