/**
 * Copyright (C) 2022 Christopher J. Stehno
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
package com.stehno.ersatz.encdec;

import java.util.Objects;

import static java.lang.String.format;

/**
 * Representation of a multipart part for requests and responses.
 */
public class MultipartPart {

    private final String fieldName;
    private final String fileName;
    private final String contentType;
    private final String transferEncoding;
    private final Object value;

    /**
     * Creates a new multipart part with the provided parameters.
     *
     * @param fieldName the field name
     * @param fileName the file name
     * @param contentType the content type of the part
     * @param transferEncoding the transfer encoding
     * @param value the part value
     */
    public MultipartPart(String fieldName, String fileName, String contentType, String transferEncoding, Object value) {
        this.fieldName = fieldName;
        this.fileName = fileName;
        this.contentType = contentType;
        this.transferEncoding = transferEncoding;
        this.value = value;
    }

    /**
     * Used to retrive the field name.
     *
     * @return the field name
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Used to retreive the file name.
     *
     * @return the file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Used to retrieve the content-type.
     *
     * @return the content-type
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Used to retrieve the transfer-encoding.
     *
     * @return the transfer-encoding
     */
    public String getTransferEncoding() {
        return transferEncoding;
    }

    /**
     * Used to retrieve the part value.
     *
     * @return the part value
     */
    public Object getValue() {
        return value;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MultipartPart that = (MultipartPart) o;
        return Objects.equals(fieldName, that.fieldName) &&
            Objects.equals(fileName, that.fileName) &&
            Objects.equals(contentType, that.contentType) &&
            Objects.equals(transferEncoding, that.transferEncoding) &&
            Objects.deepEquals(value, that.value);
    }

    @Override public int hashCode() {
        return Objects.hash(fieldName, fileName, contentType, transferEncoding, value);
    }

    @Override public String toString() {
        return format(
            "MultipartPart{fieldName='%s', fileName='%s', contentType='%s', transferEncoding='%s', value=%s}",
            fieldName, fileName, contentType, transferEncoding, value
        );
    }
}
