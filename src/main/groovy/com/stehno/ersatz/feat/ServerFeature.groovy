package com.stehno.ersatz.feat

import groovy.transform.CompileStatic
import io.undertow.server.HttpHandler

/**
 * Created by cjstehno on 12/4/16.
 */
@CompileStatic
interface ServerFeature {

    HttpHandler apply(HttpHandler handler)
}