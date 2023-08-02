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

import lombok.NoArgsConstructor;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import static lombok.AccessLevel.PRIVATE;

/**
 * Helpers for working with <code>MimeType</code> objects.
 */
@NoArgsConstructor(access = PRIVATE)
public final class MimeTypes {

    /**
     * Creates a <code>MimeType</code> object from the specified string designator. If a parsing exception is thrown, it
     * will be wrapped in an <code>IllegalArgumentException</code> and rethrown.
     *
     * @param value the mime-type text value
     * @return the wrapped MimeType
     */
    public static MimeType createMimeType(final String value) {
        try {
            return new MimeType(value);
        } catch (MimeTypeParseException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
}
