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
package io.github.cjstehno.ersatz.encdec;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.LinkedList;
import java.util.function.Function;

import static io.github.cjstehno.ersatz.util.ByteArrays.join;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readAllBytes;

/**
 * Reusable response content encoders. An encoder is simply a <code>Function&lt;Object,String&gt;</code> which is used to convert the configured response
 * content object into the response output bytes.
 */
public interface Encoders {

    /**
     * Encodes the object as a String of text (UTF-8).
     */
    Function<Object, byte[]> text = text(UTF_8);

    /**
     * Encodes the object as a String of text with the specified charset.
     *
     * @param charset the charset name
     * @return the encoded object as a String
     */
    static Function<Object, byte[]> text(final String charset) {
        return text(Charset.forName(charset));
    }

    /**
     * Encodes the object as a String of text with the specified charset.
     *
     * @param charset the charset object
     * @return the encoded object as a String
     */
    static Function<Object, byte[]> text(final Charset charset) {
        return obj -> (obj != null ? obj.toString() : "").getBytes(charset);
    }

    /**
     * Encodes a byte array, InputStream or other object with a "getBytes()" method into a base-64 string.
     */
    Function<Object, byte[]> binaryBase64 = obj -> obj == null ? "".getBytes(UTF_8) : Base64.getEncoder().encode(toBytes(obj));

    /**
     * Loads the content at the specified destination and returns it as a byte array. The content destination may be
     * specified by any of the following:
     * <p>
     * InputStream - will load the contents of the stream
     * String - will load the contents of the path from the classloader.
     * Path - will load the contents of the path as a file on the filesystem
     * File - will load the contents of the path as a file on the filesystem
     * URI - will load the contents of the URI
     * URL - will load the contents of the URL
     */
    Function<Object, byte[]> content = obj -> {
        try {
            if (obj instanceof InputStream) {
                return readStream((InputStream) obj);

            } else if (obj instanceof Path) {
                return readAllBytes((Path) obj);

            } else if (obj instanceof File) {
                return readAllBytes(((File) obj).toPath());

            } else if (obj instanceof URI) {
                return readStream(((URI) obj).toURL().openStream());

            } else if (obj instanceof URL) {
                return readStream(((URL) obj).openStream());

            } else if (obj instanceof CharSequence) {
                return readAllBytes(Paths.get(Encoders.class.getResource(obj.toString()).toURI()));

            } else {
                throw new IllegalArgumentException("Content must be specified as an InputSteam, String, Path, File, URI, or URL instance.");
            }

        } catch (final RuntimeException re) {
            throw re;
        } catch (final Exception ex) {
            throw new IllegalArgumentException("Unable to resolve content due to error: " + ex.getMessage());
        }
    };

    private static byte[] readStream(final InputStream stream) throws IOException {
        try (stream) {
            return stream.readAllBytes();
        }
    }

    /**
     * Encodes a <code>MultipartResponseContent</code> object to its multipart string representation. The generated
     * multipart content is a simple message implementing the minimal multipart content specification - you may want to
     * find a more robust implementation if you require a more detailed multipart API.
     */
    Function<Object, byte[]> multipart = obj -> {
        if (!(obj instanceof MultipartResponseContent)) {
            throw new IllegalArgumentException(obj.getClass().getName() + " found, MultipartRequestContent is required.");
        }

        final ErsatzMultipartResponseContent mrc = (ErsatzMultipartResponseContent) obj;

        final var arrays = new LinkedList<byte[]>();

        mrc.parts().forEach(p -> {
            arrays.add(("--" + mrc.getBoundary() + "\r\n").getBytes(UTF_8));

            if (p.getFileName() != null) {
                arrays.add(("Content-Disposition: form-data; name=\"" + p.getFieldName() + "\"; filename=\"" + p.getFileName() + "\"\r\n").getBytes(UTF_8));
            } else {
                arrays.add(("Content-Disposition: form-data; name=\"" + p.getFieldName() + "\"\r\n").getBytes(UTF_8));
            }

            if (p.getTransferEncoding() != null) {
                arrays.add(("Content-Transfer-Encoding: " + p.getTransferEncoding() + "\r\n").getBytes(UTF_8));
            }

            arrays.add(("Content-Type: " + p.getContentType() + "\r\n\r\n").getBytes(UTF_8));

            final Function<Object, byte[]> encoderFn = mrc.encoder(p.getContentType(), p.getValue().getClass());
            final var encoded = encoderFn.apply(p.getValue());
            arrays.add(encoded);
            arrays.add("\r\n".getBytes(UTF_8));
        });

        arrays.add(("--" + mrc.getBoundary() + "--\r\n").getBytes(UTF_8));

        return join(arrays);
    };

    private static byte[] toBytes(final Object obj) {
        if (obj instanceof byte[]) {
            return (byte[]) obj;
        } else if (obj instanceof InputStream) {
            try {
                return readStream((InputStream) obj);
            } catch (IOException e) {
                throw new IllegalArgumentException("Unable to read InputStream: " + e.getMessage());
            }
        } else {
            return obj.toString().getBytes(UTF_8);
        }
    }
}