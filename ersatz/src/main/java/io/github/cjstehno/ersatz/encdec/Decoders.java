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
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.UploadContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;

import static io.github.cjstehno.ersatz.cfg.ContentType.TEXT_PLAIN;
import static java.net.URLDecoder.decode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.createTempDirectory;
import static lombok.AccessLevel.PRIVATE;

/**
 * Reusable request content decoder functions. Decoders are simply implementations of the <code>BiFunction&lt;byte[], DecodingContext, Object&gt;</code>
 * interface, which takes the request content as a byte array along with the <code>DecodingContext</code> to return an <code>Object</code>
 * representation of the request data.
 */
@NoArgsConstructor(access = PRIVATE)
public final class Decoders {

    private static final String TEMP_DIR = "ersatz-multipart-";

    /**
     * Decoder that simply passes the content bytes through as an array of bytes.
     */
    public static final BiFunction<byte[], DecodingContext, Object> passthrough = (content, ctx) -> content;

    /**
     * Decoder that converts request content bytes into a UTF-8 string.
     */
    public static final BiFunction<byte[], DecodingContext, Object> utf8String = string(UTF_8);

    /**
     * Decoder that converts request content bytes into a UTF-8 string.
     *
     * @return the decoded bytes as a string
     */
    public static BiFunction<byte[], DecodingContext, Object> string() {
        return string(UTF_8);
    }

    /**
     * Decoder that converts request content bytes into a string of the specified charset.
     *
     * @param charset the name of the charset
     * @return the decoded bytes as a string
     */
    public static BiFunction<byte[], DecodingContext, Object> string(final String charset) {
        return string(Charset.forName(charset));
    }

    /**
     * Decoder that converts request content bytes into a string of the specified charset.
     *
     * @param charset the charset to be used
     * @return the decoded bytes as a string
     */
    public static BiFunction<byte[], DecodingContext, Object> string(final Charset charset) {
        return (content, ctx) -> content != null ? new String(content, charset) : "";
    }

    /**
     * Decoder that converts request content bytes in an url-encoded format into a map of name/value pairs.
     */
    public static BiFunction<byte[], DecodingContext, Object> urlEncoded = (content, ctx) -> {
        val map = new HashMap<String, String>();

        if (content != null) {
            for (val nvp : new String(content, UTF_8).split("&")) {
                val parts = nvp.split("=");
                try {
                    if (parts.length == 2) {
                        map.put(decode(parts[0], UTF_8), decode(parts[1], UTF_8));
                    }
                } catch (Exception e) {
                    throw new IllegalArgumentException(e.getMessage());
                }
            }
        }

        return map;
    };

    /**
     * Decoder that converts request content bytes into a <code>MultipartRequestContent</code> object populated with the multipart request content.
     */
    public static BiFunction<byte[], DecodingContext, Object> multipart = (content, ctx) -> {
        val parts = resolveFileParts(content, ctx);
        val multipartRequest = new MultipartRequestContent();

        parts.forEach(part -> {
            val partCtx = new DecodingContext(part.getSize(), part.getContentType(), null, ctx.getDecoderChain());

            if (part.isFormField()) {
                multipartRequest.part(part.getFieldName(), TEXT_PLAIN, ctx.getDecoderChain().resolve(TEXT_PLAIN).apply(part.get(), partCtx));
            } else {
                multipartRequest.part(
                    part.getFieldName(),
                    part.getName(),
                    part.getContentType(),
                    ctx.getDecoderChain().resolve(part.getContentType()).apply(part.get(), partCtx)
                );
            }
        });

        return multipartRequest;
    };

    private static List<FileItem> resolveFileParts(final byte[] content, final DecodingContext ctx) {
        try {
            return new FileUpload(new DiskFileItemFactory(10_000, createTempDirectory(TEMP_DIR).toFile()))
                .parseRequest(new ErsatzUploadContext(content, ctx));
        } catch (final Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @RequiredArgsConstructor(access = PRIVATE) @SuppressWarnings("ClassCanBeRecord")
    private static final class ErsatzUploadContext implements UploadContext {

        private final byte[] content;
        private final DecodingContext ctx;

        @Override
        public long contentLength() {
            return ctx.getContentLength();
        }

        @Override
        public String getCharacterEncoding() {
            return ctx.getCharacterEncoding();
        }

        @Override
        public String getContentType() {
            return ctx.getContentType();
        }

        @Override
        public int getContentLength() {
            return (int) ctx.getContentLength();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(content);
        }
    }
}
