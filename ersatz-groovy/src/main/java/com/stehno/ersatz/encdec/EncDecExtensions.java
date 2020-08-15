package com.stehno.ersatz.encdec;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import space.jasan.support.groovy.closure.ConsumerWithDelegate;

import static groovy.lang.Closure.DELEGATE_FIRST;

public class EncDecExtensions {

    /**
     * Used to configure a request cookie with a configuration closure.
     *
     * @param closure the configuration closure
     * @return the configured cookie
     */
    public static Cookie cookie(
        final Cookie type,
        @DelegatesTo(value = Cookie.class, strategy = DELEGATE_FIRST) final Closure closure
    ) {
        return Cookie.cookie(ConsumerWithDelegate.create(closure));
    }

    public static ResponseEncoders encoders(
        final ResponseEncoders type,
        @DelegatesTo(value = ResponseEncoders.class, strategy = DELEGATE_FIRST) Closure closure
    ) {
        return ResponseEncoders.encoders(ConsumerWithDelegate.create(closure));
    }

    public static RequestDecoders decoders(
        final RequestDecoders type,
        @DelegatesTo(value = RequestDecoders.class, strategy = DELEGATE_FIRST) Closure closure
    ) {
        return RequestDecoders.decoders(ConsumerWithDelegate.create(closure));
    }

    /**
     * Creates and configures a multipart request object using the Groovy DSL closure (delegated to an instance of MultipartRequestContent).
     *
     * @param closure the configuration closure
     * @return a configured instance of MultipartRequestContent
     */
    public static MultipartRequestContent multipartRequest(
        final MultipartResponseContent type,
        @DelegatesTo(value = MultipartRequestContent.class, strategy = DELEGATE_FIRST) final Closure closure
    ) {
        return MultipartRequestContent.multipartRequest(ConsumerWithDelegate.create(closure));
    }

    /**
     * Creates a new multipart response content object with the optional boundary (random default) and a Closure used to configure the parts.
     *
     * @param closure the configuration closure (Delegates to MultipartContent instance)
     * @return a reference to this MultipartResponseContent instance
     */
    public static MultipartResponseContent multipartResponse(
        final MultipartResponseContent type,
        final @DelegatesTo(value = MultipartResponseContent.class, strategy = DELEGATE_FIRST) Closure closure
    ) {
        return MultipartResponseContent.multipartResponse(ConsumerWithDelegate.create(closure));
    }
}
