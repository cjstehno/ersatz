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
package com.stehno.ersatz

import groovy.transform.TupleConstructor
import org.apache.commons.fileupload.FileItem

import java.util.function.Function

import static com.stehno.ersatz.ContentType.CONTENT_TYPE_HEADER

/**
 * Content matcher (condition) used to assist in matching multipart request content.
 */
class MultipartContentMatcher implements Function<ClientRequest, Boolean> {

    private Closure<Boolean> closure
    private Function<FileItemMatcher, Boolean> function

    static MultipartContentMatcher multipart(@DelegatesTo(FileItemMatcher) final Closure<Boolean> closure) {
        new MultipartContentMatcher(closure: closure)
    }

    static MultipartContentMatcher multipart(final Function<FileItemMatcher, Boolean> function) {
        new MultipartContentMatcher(function: function)
    }

    @Override
    Boolean apply(final ClientRequest clientRequest) {
        FileItemMatcher fileItemMatcher = new FileItemMatcher(clientRequest)
        boolean headerMatch = clientRequest.headers.get(CONTENT_TYPE_HEADER, 0)?.startsWith('multipart/')

        if (closure) {
            closure.delegate = fileItemMatcher
            return headerMatch && closure.call(clientRequest)
        }

        return headerMatch && function.apply(fileItemMatcher)
    }

    /**
     * Used to determine whether or not the given <code>FileItem</code> has the specified attributes. The allowed attributes are: 'fieldName' to
     * match the file field name, 'string' to match the item value as a string, 'fileName' to match the name of the file if the content is a file,
     * 'contentType' to match the content type of the entry, 'bytes' to match the content as a byte array, and 'size' to match the item size value.
     *
     * @param attrs the map of attributes to be matched
     * @param fileItem the multipart file item
     * @return true if the specified conditions are matched
     */
    static boolean attrs(final Map<String, Object> attrs, final FileItem fileItem) {
        attrs.every { k, v ->
            switch (k) {
                case 'fieldName':
                    return v == fileItem.fieldName
                case 'fileName':
                    return v == fileItem.name
                case 'string':
                    return v == fileItem.getString('UTF-8')
                case 'size':
                    return v == fileItem.size
                case 'contentType':
                    return fileItem.contentType?.startsWith(v)
                case 'bytes':
                    return v == fileItem.get()
                default:
                    return false
            }
        }
    }

    /**
     * DSL model for matching <code>FileItem</code> parts in a multipart request.
     */
    @TupleConstructor
    static class FileItemMatcher {

        final ClientRequest clientRequest

        /**
         * Matches a part at the specified index that must match the given attributes. See the <code>attrs(Map<String,Object>,FileItem)</code> method
         * for more details about the supported match fields.
         *
         * @param map the map of matched attributes
         * @param index the index of the part being matched
         * @return <code>true</code> if the part matches the expected criteria
         */
        boolean part(final Map<String, Object> map, final int index) {
            attrs(map, clientRequest.fileItems[index])
        }

        /**
         * Matches a field part at the specified index with the given name and value content.
         *
         * @param index the part index
         * @param name the field name
         * @param value the field value
         */
        boolean field(final int index, final String name, final String value) {
            part(index, fieldName: name, string: value)
        }

        /**
         * Matches a field part at the specified index with the given name, value content and content-type.
         *
         * @param index the part index
         * @param name the field name
         * @param value the field value
         * @param contentType the content-type
         */
        boolean field(final int index, final String name, final String value, final String contentType) {
            part(index, fieldName: name, string: value, contentType: contentType)
        }

        /**
         * Matches a field part at the specified index with the given name, value content and content-type.
         *
         * @param index the part index
         * @param name the field name
         * @param value the field value
         * @param contentType the content-type
         */
        boolean field(final int index, final String name, final String value, final ContentType contentType) {
            field(index, name, value, contentType.value)
        }

        /**
         * Matches a file part at the specified index with the given attributes.
         *
         * @param index the part index
         * @param fieldName the field name
         * @param fileName the file name
         * @param contentType the content type (matched as "starts with")
         * @param fileSize the expected file size
         * @return <code>true</code> if the part matches the expected criteria
         */
        boolean file(final int index, final String fieldName, final String fileName, String contentType, long fileSize) {
            part(index, fieldName: fieldName, fileName: fileName, contentType: contentType, size: fileSize)
        }

        /**
         * Matches a file part at the specified index with the given attributes.
         *
         * @param index the part index
         * @param fieldName the field name
         * @param fileName the file name
         * @param contentType the content type (matched as "starts with")
         * @param fileSize the expected file size
         * @return <code>true</code> if the part matches the expected criteria
         */
        boolean file(final int index, final String fieldName, final String fileName, ContentType contentType, long fileSize) {
            file(index, fieldName, fileName, contentType.value, fileSize)
        }

        /**
         * Matches a file part at the specified index with the given attributes.
         *
         * @param index the part index
         * @param fieldName the field name
         * @param fileName the file name
         * @param contentType the content type (matched as "starts with")
         * @param bytes the content of the part as a byte array
         * @return <code>true</code> if the part matches the expected criteria
         */
        boolean file(final int index, final String fieldName, final String fileName, String contentType, byte[] bytes) {
            part(index, fieldName: fieldName, fileName: fileName, contentType: contentType, bytes: bytes)
        }

        /**
         * Matches a file part at the specified index with the given attributes.
         *
         * @param index the part index
         * @param fieldName the field name
         * @param fileName the file name
         * @param contentType the content type (matched as "starts with")
         * @param bytes the content of the part as a byte array
         * @return <code>true</code> if the part matches the expected criteria
         */
        boolean file(final int index, final String fieldName, final String fileName, ContentType contentType, byte[] bytes) {
            file index, fieldName, fileName, contentType.value, bytes
        }

        /**
         * Matches a file part at the specified index with the given attributes.
         *
         * @param index the part index
         * @param fieldName the field name
         * @param fileName the file name
         * @param contentType the content type (matched as "starts with")
         * @param text the content of the part as a string of text.
         * @return <code>true</code> if the part matches the expected criteria
         */
        boolean file(final int index, final String fieldName, final String fileName, String contentType, String text) {
            part(index, fieldName: fieldName, fileName: fileName, contentType: contentType, string: text)
        }

        /**
         * Matches a file part at the specified index with the given attributes.
         *
         * @param index the part index
         * @param fieldName the field name
         * @param fileName the file name
         * @param contentType the content type (matched as "starts with")
         * @param text the content of the part as a string of text.
         * @return <code>true</code> if the part matches the expected criteria
         */
        boolean file(final int index, final String fieldName, final String fileName, ContentType contentType, String text) {
            file index, fieldName, fileName, contentType.value, text
        }
    }
}

