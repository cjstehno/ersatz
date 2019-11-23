/**
 * Copyright (C) 2019 Christopher J. Stehno
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
package com.stehno.ersatz.server;

public interface UnderlyingServer {

    void start() throws Exception;

    int getHttpPort();

    int getHttpsPort();

    default String getHttpUrl(){
        return "http://localhost:" + getHttpPort();
    }

    default String getHttpsUrl(){
        return "https://localhost:" + getHttpsPort();
    }

    default String getWsUrl(){
        return "ws://localhost:" + getHttpPort();
    }

    default String getHttpUrl(final String path){
        return getHttpUrl() + path;
    }

    default String getHttpsUrl(final String path){
        return getHttpsUrl() + path;
    }

    void stop();
}
