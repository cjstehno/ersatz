package com.stehno.ersatz;

/**
 * Created by cjstehno on 12/1/16.
 */
public class GetRequest {

    private String path;
    private Response response;

    public GetRequest(final String path){
        this.path = path;
    }

    Response responds() {
        response = new Response();
        return response;
    }

    public String getPath() {
        return path;
    }

    public Response getResponse() {
        return response;
    }
}
