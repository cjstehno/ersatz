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
package io.github.cjstehno.ersatz.cfg;

import static io.github.cjstehno.ersatz.match.BodyMatcher.bodyMatching;
import static io.github.cjstehno.ersatz.match.BodyParamMatcher.bodyParamMatching;
import static org.hamcrest.Matchers.equalTo;

import io.github.cjstehno.ersatz.encdec.DecodingContext;
import io.github.cjstehno.ersatz.match.BodyMatcher;
import io.github.cjstehno.ersatz.match.BodyParamMatcher;
import java.util.function.BiFunction;
import org.hamcrest.Matcher;

/**
 * Expectation configuration for a request with body content.
 */
public interface RequestWithContent extends Request {

    /**
     * Configures the expected body content of the request with the specified content type.
     *
     * @param body        the body content
     * @param contentType the body content type
     * @return a reference to this request
     */
    default RequestWithContent body(final Object body, String contentType) {
        return body(body instanceof Matcher ? (Matcher<Object>) body : equalTo(body), contentType);
    }

    /**
     * Configures the expected body content of the request with the specified content type.
     *
     * @param body        the body content matcher
     * @param contentType the body content type
     * @return a reference to this request
     */
    default RequestWithContent body(final Matcher<Object> body, String contentType) {
        return body(bodyMatching(body, contentType));
    }

    /**
     * Configures the expected body content of the request with the specified content type.
     *
     * @param body        the body content
     * @param contentType the body content type
     * @return a reference to this request
     */
    default RequestWithContent body(final Object body, final ContentType contentType) {
        return body(body, contentType.getValue());
    }

    /**
     * Configures the expected body content of the request with the specified content type.
     *
     * @param body        the body content matcher
     * @param contentType the body content type
     * @return a reference to this request
     */
    default RequestWithContent body(final Matcher<Object> body, final ContentType contentType) {
        return body(body, contentType.getValue());
    }

    /**
     * Configures the expected body content of the request.
     *
     * @param bodyMatcher the body matcher
     * @return a reference to this request
     */
    RequestWithContent body(final BodyMatcher bodyMatcher);

    /**
     * Specifies a custom body content converter function. The function will have the client request body content as a byte array and it will be
     * converted into the specified output type. Generally the conversion is used when comparing the client request with the configured request
     * body expectation.
     *
     * @param contentType the content type that the convert will handle
     * @param decoder     the conversion function
     * @return a reference to this request
     */
    RequestWithContent decoder(final String contentType, final BiFunction<byte[], DecodingContext, Object> decoder);

    /**
     * Specifies a custom body content converter function. The function will have the client request body content as a
     * byte array and it will be converted into the specified output type. Generally the conversion is used when
     * comparing the client request with the configured request body expectation.
     *
     * @param contentType the content type that the convert will handle
     * @param decoder     the conversion function
     * @return a reference to this request
     */
    default RequestWithContent decoder(final ContentType contentType, final BiFunction<byte[], DecodingContext, Object> decoder) {
        return decoder(contentType.getValue(), decoder);
    }

    /**
     * Configures an expectation matching parameters contained in the request body. The specified value must exist in
     * the list of parameters for the provided key.
     *
     * @param name  the request body parameter name
     * @param value the expected parameter value
     * @return a reference to this request
     */
    default RequestWithContent param(final String name, final String value) {
        return param(bodyParamMatching(name, value));
    }

    /**
     * Configures an expectation matching parameters contained in the request body. The specified values must exist in
     * the list of parameters for the provided key.
     *
     * @param name   the request body parameter name
     * @param values the expected parameter values
     * @return a reference to this request
     */
    default RequestWithContent param(final String name, final Iterable<? super String> values) {
        return param(bodyParamMatching(name, values));
    }

    /**
     * Configures an expectation matching parameters contained in the request body. The specified matchers must be
     * satisfied by the parameters mapped to the provided named parameter.
     *
     * @param name    the request body parameter name
     * @param matcher the matcher for the expected parameter values
     * @return a reference to this request
     */
    default RequestWithContent param(final String name, final Matcher<Iterable<? super String>> matcher) {
        return param(bodyParamMatching(name, matcher));
    }

    /**
     * Configures an expectation matching request body parameters.
     *
     * @param bodyParamMatcher the request body parameter matcher
     * @return a reference to this request
     */
    RequestWithContent param(final BodyParamMatcher bodyParamMatcher);
}
