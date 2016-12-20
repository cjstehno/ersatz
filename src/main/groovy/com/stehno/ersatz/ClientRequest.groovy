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

import groovy.transform.CompileStatic
import groovy.transform.Memoized
import groovy.transform.TupleConstructor
import io.undertow.io.Receiver
import io.undertow.server.HttpServerExchange
import io.undertow.server.handlers.Cookie
import io.undertow.util.HeaderMap
import org.apache.commons.fileupload.FileItem
import org.apache.commons.fileupload.FileUpload
import org.apache.commons.fileupload.UploadContext
import org.apache.commons.fileupload.disk.DiskFileItemFactory

import java.util.function.Function

/**
 * An abstraction around the underlying HTTP server request that aids in matching and working with requests.
 */
@CompileStatic @TupleConstructor
class ClientRequest {

    /**
     * The wrapped <code>HttpServerExchange</code> object.
     */
    final HttpServerExchange exchange

    /**
     * Retrieves the HTTP method for the request.
     *
     * @return the HTTP method for the request
     */
    String getMethod() {
        exchange.requestMethod.toString()
    }

    /**
     * Retrieves the request path.
     *
     * @return the request path
     */
    String getPath() {
        exchange.requestPath
    }

    /**
     * Retrieves the URL query string parameters for the request.
     *
     * @return the query string parameters
     */
    Map<String, Deque<String>> getQueryParams() {
        exchange.queryParameters
    }

    /**
     * Retrieves the request headers.
     *
     * @return the request headers
     */
    HeaderMap getHeaders() {
        exchange.requestHeaders
    }

    /**
     * Retrieves the cookies associated with the request.
     *
     * @return the request cookies
     */
    Map<String, Cookie> getCookies() {
        exchange.requestCookies
    }

    /**
     * Retrieves the body content (if any) as a byte array (null for an empty request).
     *
     * @return the optional body content as a byte array.
     */
    @Memoized
    byte[] getBodyAsBytes() {
        byte[] bytes = null

        exchange.requestReceiver.receiveFullBytes(new Receiver.FullBytesCallback() {
            @Override
            void handle(final HttpServerExchange exch, byte[] message) {
                bytes = message
            }
        })

        bytes
    }

    /**
     * Retrieves the request body content (if there is any) as the result of converting the raw byte array to an Object using the provided converter
     * function - the default will be as a byte array. If there is no request body content, a value of <code>null</code> is returned.
     *
     * @param converter the converter function
     * @return the converted request body content (or null)
     */
    @Memoized
    Object getBody(final Function<byte[], Object> converter = { b -> b }) {
        bodyAsBytes ? converter.apply(bodyAsBytes) : null
    }

    /**
     * Retrieves the request body content as a <code>String</code> (<code>null</code> for an empty request).
     *
     * @return the body content as a string (or null)
     */
    String getBodyAsString() {
        getBody { b -> b ? new String(b as byte[]) : '' }
    }

    @Override
    String toString() {
        "{ $method $path (query=$queryParams, headers=$headers, cookies=$cookies): ${bodyAsString ? bodyAsString.take(1000) : '<empty>'} }"
    }

    /**
     * Used to retrieve the list of FileItems contained in a <code>multipart/form-data</code> request.
     *
     * @return the list of <code>FileItem</code> objects representing the multipart request content.
     */
    @Memoized
    List<FileItem> getFileItems() {
        new FileUpload(new DiskFileItemFactory(10_000, File.createTempDir())).parseRequest(new UploadContext() {
            @Override
            long contentLength() {
                exchange.requestContentLength
            }

            @Override
            String getCharacterEncoding() {
                exchange.requestCharset
            }

            @Override
            String getContentType() {
                exchange.requestHeaders.get('Content-Type').first
            }

            @Override
            int getContentLength() {
                exchange.requestContentLength
            }

            @Override
            InputStream getInputStream() throws IOException {
                new ByteArrayInputStream(bodyAsBytes as byte[])
            }
        })
    }
}
