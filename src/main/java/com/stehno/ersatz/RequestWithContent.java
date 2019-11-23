package com.stehno.ersatz;

import org.hamcrest.Matcher;

import java.util.function.BiFunction;

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
    RequestWithContent body(final Object body, String contentType);

    /**
     * Configures the expected body content of the request with the specified content type.
     *
     * @param body        the body content matcher
     * @param contentType the body content type
     * @return a reference to this request
     */
    RequestWithContent body(final Matcher<Object> body, String contentType);

    /**
     * Configures the expected body content of the request with the specified content type.
     *
     * @param body        the body content
     * @param contentType the body content type
     * @return a reference to this request
     */
    RequestWithContent body(final Object body, ContentType contentType);

    /**
     * Configures the expected body content of the request with the specified content type.
     *
     * @param body        the body content matcher
     * @param contentType the body content type
     * @return a reference to this request
     */
    RequestWithContent body(final Matcher<Object> body, ContentType contentType);

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
     * Specifies a custom body content converter function. The function will have the client request body content as a byte array and it will be
     * converted into the specified output type. Generally the conversion is used when comparing the client request with the configured request
     * body expectation.
     *
     * @param contentType the content type that the convert will handle
     * @param decoder     the conversion function
     * @return a reference to this request
     */
    RequestWithContent decoder(final ContentType contentType, final BiFunction<byte[], DecodingContext, Object> decoder);

    /**
     * Configures a parent collection of decoders to be searched when a decoder is not configured on the request itself.
     *
     * @param decoders the parent decoder collection
     * @return a reference to this request
     * @deprecated Use one of the other decoder configuration methods
     */
    @Deprecated RequestWithContent decoders(final RequestDecoders decoders);

    /**
     * Configures an expectation matching parameters contained in the request body. The specified value must exist in the list
     * of parameters for the provided key.
     *
     * @param name  the parameter name
     * @param value the expected parameter value
     * @return a reference to this request
     */
    RequestWithContent param(final String name, final String value);

    /**
     * Configures an expectation matching parameters contained in the request body. The specified values must exist in the list
     * of parameters for the provided key.
     *
     * @param name   the parameter name
     * @param values the expected parameter values
     * @return a reference to this request
     */
    RequestWithContent param(final String name, final Iterable<String> values);

    /**
     * Configures an expectation matching parameters contained in the request body. The specified matchers must be satisifed
     * by the parameters mapped to the provided named parameter.
     *
     * @param name     the parameter name
     * @param matchers the expected parameter value matchers
     * @return a reference to this request
     */
    RequestWithContent param(final String name, final Matcher<Iterable<? extends String>> matchers);
}
