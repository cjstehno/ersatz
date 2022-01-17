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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.LinkedList;
import java.util.function.Function;

import static com.stehno.ersatz.util.ByteArrays.join;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Reusable response content encoders. An encoder is simply a <code>Function&lt;Object,String&gt;</code> which is used to convert the configured response
 * content object into the response output bytes.
 */
public interface Encoders {

    // FIXME: these and the decoders need to be unit tested

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
     * Encodes the bytes read from an InputStream.
     */
    Function<Object, byte[]> inputStream = o -> {
        try {
            return ((InputStream) o).readAllBytes();
        } catch (IOException e) {
            // TODO: something better here?
            e.printStackTrace();
            return new byte[0];
        }
    };

    /**
     * Loads the content at the specified destination and returns it as a byte array. The content destination may be
     * specified by any of the following: String, Path, File, URI, URL, where the String instance is a resource path on
     * the classpath.
     */
    Function<Object, byte[]> content = obj -> {
        final Path path;

        if (obj instanceof Path) {
            path = (Path) obj;

        } else if (obj instanceof File) {
            path = ((File) obj).toPath();

        } else if (obj instanceof URI) {
            path = Paths.get((URI) obj);

        } else if (obj instanceof URL) {
            try {
                path = Paths.get(((URL) obj).toURI());
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("URL (" + obj + ") is not a value URI.");
            }

        } else if (obj instanceof CharSequence) {
            try {
                path = Paths.get(Encoders.class.getResource(obj.toString()).toURI());
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Resource path (" + obj + ") is not a value URI.");
            }

        } else {
            throw new IllegalArgumentException("Content must be specified as a String, Path, File, URI, or URL instance.");
        }

        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new RuntimeException("Unable to read content from path (" + path + "): " + e.getMessage(), e);
        }
    };

    /**
     * Encodes a <code>MultipartResponseContent</code> object to its multipart string representation. The generated multipart content is a simple
     * message implementing the minimal multipart content specification - you may want to find a more robust implementation if you require a more
     * detailed multipart API.
     */
    Function<Object, byte[]> multipart = obj -> {
        if (!(obj instanceof MultipartResponseContent)) {
            throw new IllegalArgumentException(obj.getClass() + " found, MultipartRequestContent is required.");
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
        } else if (obj instanceof ByteArrayInputStream) {
            return ((ByteArrayInputStream) obj).readAllBytes();
        } else {
            return obj.toString().getBytes(UTF_8);
        }
    }
}