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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.LinkedList;
import java.util.function.Function;

import static com.stehno.ersatz.util.ByteArrays.join;
import static groovy.json.JsonOutput.toJson;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Reusable response content encoders. An encoder is simply a <code>Function&lt;Object,String&gt;</code> which is used to convert the configured response
 * content object into the String of response output.
 */
public class Encoders {

    private static final Logger log = LoggerFactory.getLogger(Encoders.class);

    /**
     * Encodes the object to JSON using the Groovy <code>JsonObject.toJson(Object)</code> method.
     */
    public static final Function<Object, byte[]> json = obj -> (obj != null ? toJson(obj) : "{}").getBytes(UTF_8);

    /**
     * Encodes the object as a String of text.
     */
    public static final Function<Object, byte[]> text = obj ->( obj != null ? obj.toString() : "").getBytes(UTF_8);

    /**
     * Encodes a byte array, InputStream or other object with a "getBytes()" method into a base-64 string.
     */
    public static final Function<Object, byte[]> binaryBase64 = obj -> obj == null ? "".getBytes(UTF_8) : Base64.getEncoder().encode(toBytes(obj));

    /**
     * Encodes the bytes read from an InputStream.
     */
    public static final Function<Object, byte[]> inputStream = o -> {
        try {
            return ((InputStream) o).readAllBytes();
        } catch (IOException e) {
            log.warn("Unable to fully read bytes: {}", e.getMessage());
            return new byte[0];
        }
    };

    /**
     * Encodes a <code>MultipartResponseContent</code> object to its multipart string representation. The generated multipart content is a simple
     * message implementing the minimal multipart content specification - you may want to find a more robust implementation if you require a more
     * detailed multipart API.
     */
    public static final Function<Object, byte[]> multipart = obj -> {
        if (!(obj instanceof MultipartResponseContent)) {
            throw new IllegalArgumentException(obj.getClass() + " found, MultipartRequestContent is required.");
        }

        final ErsatzMultipartResponseContent mrc = (ErsatzMultipartResponseContent) obj;

        final var arrays = new LinkedList<byte[]>();

        mrc.parts().forEach(p -> {
            arrays.add( ("--" + mrc.getBoundary() + "\r\n").getBytes(UTF_8));

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