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
package com.stehno.ersatz.model;

import com.stehno.ersatz.ContentRequest;
import com.stehno.ersatz.Request;
import com.stehno.ersatz.Response;

/**
 * `Request` implementation supporting the configuration of HTTP Post requests.
 */
public class PostRequest extends AbstractRequest implements ContentRequest {

    private Object body;

    PostRequest(final String path) {
        super(path);
    }

    public Request body(final Object body) {
        this.body = body;
        return this;
    }

    public Object body() {
        return body;
    }

    @Override
    protected Response newResponse() {
        return new ContentResponse();
    }
}
