package com.stehno.ersatz;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cjstehno on 12/1/16.
 */
public class ServerExpectations {

    private final List<GetRequest> requests = new ArrayList<>();

    GetRequest get(final String path) {
        GetRequest request = new GetRequest(path);
        requests.add(request);
        return request;
    }

    public Iterable<GetRequest> requests(){
        return requests;
    }
}
