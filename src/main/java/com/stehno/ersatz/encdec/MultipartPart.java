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
package com.stehno.ersatz.encdec;

import java.util.Objects;

import static java.lang.String.format;

/**
 * Representation of a multipart part for requests and responses.
 */
@SuppressWarnings("PMD.BeanMembersShouldSerialize")
public class MultipartPart {

    private String fieldName;
    private String fileName;
    private String contentType;
    private String transferEncoding;
    private Object value;

    public MultipartPart(String fieldName, String fileName, String contentType, String transferEncoding, Object value) {
        this.fieldName = fieldName;
        this.fileName = fileName;
        this.contentType = contentType;
        this.transferEncoding = transferEncoding;
        this.value = value;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public String getTransferEncoding() {
        return transferEncoding;
    }

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
