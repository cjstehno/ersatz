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
package com.stehno.ersatz;

import com.stehno.ersatz.impl.ErsatzMultipartResponseContent;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.function.Function;

import static groovy.json.JsonOutput.toJson;

/**
 * Reusable response content encoders. An encoder is simply a <code>Function<Object,String></code> which is used to convert the configured response
 * content object into the String of response output.
 */
public class Encoders {

    /**
     * Encodes the object to JSON using the Groovy <code>JsonObject.toJson(Object)</code> method.
     */
    public static final Function<Object, String> json = obj -> obj != null ? toJson(obj) : "{}";

    /**
     * Encodes the object as a String of text.
     */
    public static final Function<Object, String> text = obj -> obj != null ? obj.toString() : "";

    /**
     * Encodes a byte array, InputStream or other object with a "getBytes()" method into a base-64 string.
     */
    public static final Function<Object, String> binaryBase64 = obj -> obj == null ? "" : Base64.getEncoder().encodeToString(toBytes(obj));

    private static byte[] toBytes(final Object obj){
        if( obj instanceof byte[]){
            return (byte[]) obj;
        } else if( obj instanceof ByteArrayInputStream){
            return ((ByteArrayInputStream)obj).readAllBytes();
        } else {
            return obj.toString().getBytes();
        }
    }

    /**
     * Encodes a <code>MultipartResponseContent</code> object to its multipart string representation. The generated multipart content is a simple
     * message implementing the minimal multipart content specification - you may want to find a more robust implementation if you require a more
     * detailed multipart API.
     */
    public static final Function<Object, String> multipart = obj -> {
        if (!(obj instanceof MultipartResponseContent)) {
            // TODO: better
            throw new IllegalArgumentException("MultipartRequestContent is required.");
        }

        final ErsatzMultipartResponseContent mrc = (ErsatzMultipartResponseContent) obj;

        final StringBuilder out = new StringBuilder();

        mrc.parts().forEach(p -> {
            out.append("--").append(mrc.getBoundary()).append("\r\n");

            if (p.getFileName() != null) {
                out.append("Content-Disposition: form-data; name=\"").append(p.getFieldName()).append("\"; filename=\"").append(p.getFileName()).append("\"\r\n");
            } else {
                out.append("Content-Disposition: form-data; name=\"").append(p.getFieldName()).append("\"\r\n");
            }

            if (p.getTransferEncoding() != null) {
                out.append("Content-Transfer-Encoding: ").append(p.getTransferEncoding()).append("\r\n");
            }

            out.append("Content-Type: ").append(p.getContentType()).append("\r\n\r\n");

            final Function<Object, String> encoderFn = mrc.encoder(p.getContentType(), p.getValue().getClass());
            final String encoded = encoderFn.apply(p.getValue());
            out.append(encoded).append("\r\n");
        });

        out.append("--").append(mrc.getBoundary()).append("--\r\n");

        return out.toString();
    };
}