/**
 * Copyright (C) 2024 Christopher J. Stehno
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

import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;
import java.util.Objects;

/**
 * Representation of a multipart part for requests and responses.
 */
@Getter @ToString @SuppressWarnings("ClassCanBeRecord")
public class MultipartPart {

    private final String fieldName;
    private final String fileName;
    private final String contentType;
    private final String transferEncoding;
    private final Object value;

    /**
     * Creates a new multipart part with the provided parameters.
     *
     * @param fieldName        the field name
     * @param fileName         the file name
     * @param contentType      the content type of the part
     * @param transferEncoding the transfer encoding
     * @param value            the part value
     */
    public MultipartPart(final String fieldName, final String fileName, final String contentType, final String transferEncoding, final Object value) {
        this.fieldName = fieldName;
        this.fileName = fileName;
        this.contentType = contentType;
        this.transferEncoding = transferEncoding;
        this.value = value;
    }

    @Override public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MultipartPart that = (MultipartPart) o;
        return Objects.equals(fieldName, that.fieldName) &&
            Objects.equals(fileName, that.fileName) &&
            Objects.equals(contentType, that.contentType) &&
            Objects.equals(transferEncoding, that.transferEncoding) &&
            (
                value.getClass().isArray() ?
                    Arrays.equals((byte[]) value, (byte[]) that.value) :
                    Objects.deepEquals(value, that.value)
            );
    }

    @Override public int hashCode() {
        return Objects.hash(
            fieldName, fileName, contentType, transferEncoding,
            value.getClass().isArray() ? Arrays.hashCode((byte[]) value) : value
        );
    }
}
