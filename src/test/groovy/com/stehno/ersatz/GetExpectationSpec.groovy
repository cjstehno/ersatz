package com.stehno.ersatz

import spock.lang.Specification

class GetExpectationSpec extends Specification {

    // TODO: this stuff...
    // similar testing in Java JUnit test (?)
    // Request get(String path)
    // Request get(String path, @DelegatesTo(Request) Closure closure)
    // Request get(String path, Consumer<Request> config)

    // FIXME: an empty request (GET/HEAD) should not have a request content type

    /*
        -- requests --

        with(out) headers
        different content types
        with(out) queries (single and map)
        with(out) cookies (single and map)
        listener
        verifier
        responds()
        responder(consumer)
        responder(closure)
        condition(function)
        condition(closure)

        - test closures with external variables

        -- responses --


     */
}
