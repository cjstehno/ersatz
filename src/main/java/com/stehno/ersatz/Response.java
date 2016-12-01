package com.stehno.ersatz;

/**
 * Created by cjstehno on 12/1/16.
 */
public class Response {

    private Object body;

    Response body(final Object content){
        this.body = content;
        return this;
    }

    public Object getBody() {
        return body;
    }
}
