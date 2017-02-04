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

import com.stehno.ersatz.impl.ErsatzMultipartResponseContent

import java.util.function.Function

import static groovy.json.JsonOutput.toJson

/**
 * Reusable response content encoders. An encoder is simply a <code>Function<Object,String></code> which is used to convert the configured response
 * content object into the String of response output.
 */
@SuppressWarnings('PropertyName')
class Encoders {

    /**
     * Encodes the object to JSON using the Groovy <code>JsonObject.toJson(Object)</code> method.
     */
    static final Function<Object, String> json = { obj -> obj != null ? toJson(obj) : '{}' }

    /**
     * Encodes the object as a String of text.
     */
    static final Function<Object, String> text = { obj -> obj ? obj as String : '' }

    /**
     * Encodes a byte array, InputStream or other object with a "getBytes()" method into a base-64 string.
     */
    static final Function<Object, String> binaryBase64 = { obj ->
        if (obj) {
            return (obj instanceof byte[] ? obj as byte[] : obj.bytes).encodeBase64() as String
        }
        return ''
    }

    /**
     * Encodes a <code>MultipartResponseContent</code> object to its multipart string representation. The generated multipart content is a simple
     * message implementing the minimal multipart content specification - you may want to find a more robust implementation if you require a more
     * detailed multipart API.
     */
    static final Function<Object, String> multipart = new Function<Object, String>() {
        @Override
        String apply(final Object obj) {
            assert obj instanceof MultipartResponseContent

            ErsatzMultipartResponseContent mrc = obj as ErsatzMultipartResponseContent

            StringBuilder out = new StringBuilder()

            mrc.parts().each { p ->
                out.append("--${mrc.boundary}\r\n")

                if (p.fileName) {
                    out.append("Content-Disposition: form-data; name=\"${p.fieldName}\"; filename=\"${p.fileName}\"\r\n")
                } else {
                    out.append("Content-Disposition: form-data; name=\"${p.fieldName}\"\r\n")
                }

                if (p.transferEncoding) {
                    out.append("Content-Transfer-Encoding: ${p.transferEncoding}\r\n")
                }

                out.append("Content-Type: ${p.contentType}\r\n\r\n")

                out.append(mrc.encoder(p.contentType as String, p.value.class).apply(p.value)).append('\r\n')
            }

            out.append("--${mrc.boundary}--\r\n")

            out.toString()
        }
    }
}