package com.stehno.ersatz.model

import com.stehno.ersatz.ContentRequest
import com.stehno.ersatz.Request

/**
 * Created by cjstehno on 12/3/16.
 */
trait RequestWithContent implements ContentRequest {

    private Object body

    @Override
    Request body(Object body) {
        this.body = body
        this
    }

    Object getBody() {
        body
    }
}