/**
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

import groovy.json.JsonSlurper;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.UploadContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static com.stehno.ersatz.ContentType.TEXT_PLAIN;
import static java.util.Collections.emptyMap;

/**
 * Reusable request content decoder functions. Decoders are simply implementations of the <code>BiFunction<byte[], DecodingContext, Object></code>
 * interface, which takes the request content as a byte array along with the <code>DecodingContext</code> to return an <code>Object</code>
 * representation of the request data.
 */
public class Decoders {

    private static final Logger log = LoggerFactory.getLogger(Decoders.class);

    /**
     * Decoder that simply passes the content bytes through as an array of bytes.
     */
    public static BiFunction<byte[], DecodingContext, Object> passthrough = (bytes, decodingContext) -> bytes;

    /**
     * Decoder that converts request content bytes into a UTF-8 string.
     */
    public static BiFunction<byte[], DecodingContext, Object> utf8String = (bytes, decodingContext) -> hasContent(bytes) ? new String(bytes, StandardCharsets.UTF_8) : "";

    /**
     * Decoder that converts request content bytes into a string of JSON and then parses it with <code>JsonSlurper</code> to return parsed JSON data.
     */
    public static BiFunction<byte[], DecodingContext, Object> parseJson = (bytes, decodingContext) -> new JsonSlurper().parse(hasContent(bytes) ? bytes : "{}".getBytes());

    /**
     * Decoder that converts request content bytes in a url-encoded format into a map of name/value pairs.
     */
    public static BiFunction<byte[], DecodingContext, Object> urlEncoded = (bytes, decodingContext) -> {
        if (hasContent(bytes)) {
            final Map<String, Object> map = new HashMap<>();

            for (final String nvp : new String(bytes, StandardCharsets.UTF_8).split("&")) {
                try {
                    final String[] parts = nvp.split("=");
                    map.put(URLDecoder.decode(parts[0], StandardCharsets.UTF_8.name()), URLDecoder.decode(parts[1], StandardCharsets.UTF_8.name()));
                } catch (UnsupportedEncodingException e) {
                    log.error("Unable to url-decode content ({}): {}", nvp, e.getMessage());
                }
            }

            return map;
        }

        return emptyMap();
    };

    /**
     * Decoder that converts request content bytes into a <code>MultipartRequestContent</code> object populated with the multipart request content.
     */
    public static BiFunction<byte[], DecodingContext, Object> multipart = (bytes, decodingContext) -> {
        try {
            final List<FileItem> parts = new FileUpload(new DiskFileItemFactory(10_000, tempDir())).parseRequest(
                new UploadContext() {
                    @Override public long contentLength() {
                        return decodingContext.getContentLength();
                    }

                    @Override public String getCharacterEncoding() {
                        return decodingContext.getCharacterEncoding();
                    }

                    @Override public String getContentType() {
                        return decodingContext.getContentType();
                    }

                    @Override public int getContentLength() {
                        return (int) decodingContext.getContentLength();
                    }

                    @Override public InputStream getInputStream() throws IOException {
                        return new ByteArrayInputStream(bytes);
                    }
                }
            );

            MultipartRequestContent multipartRequest = new MultipartRequestContent();

            parts.forEach(part -> {
                DecodingContext partCtx = new DecodingContext(part.getSize(), part.getContentType(), null, decodingContext.getDecoderChain());

                if (part.isFormField()) {
                    multipartRequest.part(part.getFieldName(), TEXT_PLAIN, decodingContext.getDecoderChain().resolve(TEXT_PLAIN).apply(part.get(), partCtx));
                } else {
                    multipartRequest.part(part.getFieldName(), part.getName(), part.getContentType(), decodingContext.getDecoderChain().resolve(part.getContentType()).apply(part.get(), partCtx));
                }
            });

            return multipartRequest;

        } catch (Exception ex) {
            log.error("Unable to decode multipart request: {}", ex.getMessage());
            return null;
        }
    };

    private static File tempDir() {
        try {
            return Files.createTempDirectory("upload-").toFile();
        } catch (Exception e) {
            log.error("Unable to create temporary directory: {}", e.getMessage());
            return null;
        }
    }

    private static boolean hasContent(byte[] bytes) {
        return bytes != null && bytes.length > 0;
    }
}
