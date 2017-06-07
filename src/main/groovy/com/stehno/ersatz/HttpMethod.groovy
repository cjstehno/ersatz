package com.stehno.ersatz

import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor

/**
 * Enumeration of the supported HTTP request methods.
 */
@CompileStatic @TupleConstructor
enum HttpMethod {

    ANY('*'),
    GET('GET'),
    HEAD('HEAD'),
    POST('POST'),
    PUT('PUT'),
    DELETE('DELETE'),
    PATCH('PATCH'),
    OPTIONS('OPTIONS')

    final String value

    @Override String toString() {
        value
    }
}