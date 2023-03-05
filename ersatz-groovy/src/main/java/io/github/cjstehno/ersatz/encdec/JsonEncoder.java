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

import groovy.json.JsonOutput;

import java.util.function.Function;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * An encoder based on the Groovy <code>JsonOutput</code> serializer.
 */
public class JsonEncoder implements Function<Object, byte[]> {

    /**
     * A helper method to create an instance of the JsonEncoder.
     *
     * @return an instance of the encoder
     */
    public static Function<Object, byte[]> jsonEncoder() {
        return new JsonEncoder();
    }

    /**
     * Converts the provided object to an array of bytes containing the JSON representation of the object.
     *
     * @param obj the object being encoded
     * @return the encoded bytes of the object as JSON
     */
    @Override public byte[] apply(final Object obj) {
        return JsonOutput.toJson(obj).getBytes(UTF_8);
    }
}
