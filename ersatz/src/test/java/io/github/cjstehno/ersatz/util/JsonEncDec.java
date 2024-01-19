/**
 * Copyright (C) 2024 Christopher J. Stehno
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
package io.github.cjstehno.ersatz.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cjstehno.ersatz.encdec.DecodingContext;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * A reusable JSON encoder/decoder for testing.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JsonEncDec {

    public static final BiFunction<byte[], DecodingContext, Object> jsonDecoder = (content, ctx) -> {
        try {
            return new ObjectMapper().readValue(content != null ? content : "{}".getBytes(UTF_8), Map.class);
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    };

    public static final Function<Object, byte[]> jsonEncoder = obj -> {
        try {
            return new ObjectMapper().writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    };
}
