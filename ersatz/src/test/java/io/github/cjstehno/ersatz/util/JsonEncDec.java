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
