/*
 * Copyright (C) 2017 Christopher J. Stehno
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

import groovy.json.JsonSlurper
import org.apache.commons.fileupload.FileItem
import org.apache.commons.fileupload.FileUpload
import org.apache.commons.fileupload.UploadContext
import org.apache.commons.fileupload.disk.DiskFileItemFactory

import java.util.function.BiFunction

import static com.stehno.ersatz.ContentType.TEXT_PLAIN
import static java.net.URLDecoder.decode
import static java.nio.charset.StandardCharsets.UTF_8

/**
 * Reusable request content decoder functions. Decoders are simply implementations of the <code>BiFunction<byte[], DecodingContext, Object></code>
 * interface, which takes the request content as a byte array along with the <code>DecodingContext</code> to return an <code>Object</code>
 * representation of the request data.
 */
@SuppressWarnings('PropertyName')
class Decoders {

    /**
     * Decoder that simply passes the content bytes through as an array of bytes.
     */
    static final BiFunction<byte[], DecodingContext, Object> passthrough = { byte[] content, DecodingContext ctx ->
        content
    }

    /**
     * Decoder that converts request content bytes into a UTF-8 string.
     */
    static final BiFunction<byte[], DecodingContext, Object> utf8String = { byte[] content, DecodingContext ctx ->
        content ? new String(content, UTF_8) : ''
    }

    /**
     * Decoder that converts request content bytes into a string of JSON and then parses it with <code>JsonSlurper</code> to return parsed JSON data.
     */
    static final BiFunction<byte[], DecodingContext, Object> parseJson = { byte[] content, DecodingContext ctx ->
        new JsonSlurper().parse(content ?: '{}'.bytes)
    }

    /**
     * Decoder that converts request content bytes in a url-encoded format into a map of name/value pairs.
     */
    static final BiFunction<byte[], DecodingContext, Object> urlEncoded = { byte[] content, DecodingContext ctx ->
        if (content) {
            return new String(content, UTF_8).split('&').collectEntries { String nvp ->
                String[] parts = nvp.split('=')
                [decode(parts[0], UTF_8.name()), decode(parts[1], UTF_8.name())]
            }
        }

        return [:]
    }

    /**
     * Decoder that converts request content bytes into a <code>MultipartRequestContent</code> object populated with the multipart request content.
     */
    static final BiFunction<byte[], DecodingContext, Object> multipart = { byte[] content, DecodingContext ctx ->
        List<FileItem> parts = new FileUpload(new DiskFileItemFactory(10_000, File.createTempDir())).parseRequest(new UploadContext() {
            @Override
            long contentLength() {
                ctx.contentLength
            }

            @Override
            String getCharacterEncoding() {
                ctx.characterEncoding
            }

            @Override
            String getContentType() {
                ctx.contentType
            }

            @Override
            int getContentLength() {
                ctx.contentLength
            }

            @Override
            InputStream getInputStream() throws IOException {
                new ByteArrayInputStream(content)
            }
        })

        MultipartRequestContent multipartRequest = new MultipartRequestContent()

        parts.each { part ->
            DecodingContext partCtx = new DecodingContext(part.size, part.contentType, null, ctx.decoderChain)

            if (part.isFormField()) {
                multipartRequest.part(part.fieldName, TEXT_PLAIN, ctx.decoderChain.resolve(TEXT_PLAIN).apply(part.get(), partCtx))
            } else {
                multipartRequest.part(
                    part.fieldName,
                    part.name,
                    part.contentType,
                    ctx.decoderChain.resolve(part.contentType).apply(part.get(), partCtx)
                )
            }
        }

        multipartRequest
    }
}
