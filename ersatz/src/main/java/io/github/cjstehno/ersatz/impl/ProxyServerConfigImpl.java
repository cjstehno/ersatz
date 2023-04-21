/**
 * Copyright (C) 2023 Christopher J. Stehno
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
package io.github.cjstehno.ersatz.impl;

import io.github.cjstehno.ersatz.cfg.ProxyServerConfig;
import io.github.cjstehno.ersatz.cfg.ProxyServerExpectations;
import io.github.cjstehno.ersatz.cfg.ServerConfig;
import lombok.Getter;

import java.net.URI;
import java.util.function.Consumer;

/**
 * The implementation of the <code>ProxyServerConfig</code> interface.
 */
@Getter
public class ProxyServerConfigImpl implements ProxyServerConfig {

    private static final String HTTP = "HTTP";
    private static final String HTTP_ONLY_MESSAGE = "Only HTTP targets are supported at this time.";
    private boolean autoStart = true;
    private URI targetUri;
    private int ioThreads = 1;
    private int workerThreads = 4;

    private final ProxyServerExpectationsImpl expectations = new ProxyServerExpectationsImpl();

    @Override public ProxyServerConfig autoStart(final boolean auto) {
        this.autoStart = auto;
        return this;
    }

    @Override public ProxyServerConfig target(final URI value) {
        if (value.getScheme().equalsIgnoreCase(HTTP)) {
            this.targetUri = value;
            return this;
        } else {
            throw new IllegalArgumentException(HTTP_ONLY_MESSAGE);
        }
    }

    @Override public ProxyServerConfig expectations(final Consumer<ProxyServerExpectations> consumer) {
        consumer.accept(expectations);
        return this;
    }

    @Override public ProxyServerConfig serverThreads(int io, int worker) {
        ioThreads = io;
        workerThreads = worker;
        return this;
    }
}
