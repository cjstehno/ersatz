/**
 * Copyright (C) 2016 Christopher J. Stehno
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

    List<GetRequest> requests() {
        return requests;
    }

    boolean verify() {
        return requests.stream().allMatch(GetRequest::verify);
    }
}