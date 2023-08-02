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
package io.github.cjstehno.ersatz.cfg;

import lombok.SneakyThrows;

import java.net.URI;
import java.net.URL;
import java.util.function.Consumer;

/**
 * Defines the server configuration options available for the proxy server.
 */
@SuppressWarnings("checkstyle:MagicNumber")
public interface ProxyServerConfig {

    /**
     * Toggles the server auto-start feature. By default the proxy server will start once it is configured.
     *
     * @param auto enable/disable auto-start
     * @return a reference to this configuration
     */
    ProxyServerConfig autoStart(boolean auto);

    /**
     * Specifies the target URI for the proxy server.
     *
     * @param value the target URI
     * @return a reference to this configuration
     */
    @SneakyThrows
    default ProxyServerConfig target(final String value) {
        return target(new URI(value));
    }

    /**
     * Specifies the target URI for the proxy server.
     *
     * @param value the target URI
     * @return a reference to this configuration
     */
    ProxyServerConfig target(URI value);

    /**
     * Specifies the target URI for the proxy server.
     *
     * @param value the target URI
     * @return a reference to this configuration
     */
    @SneakyThrows
    default ProxyServerConfig target(final URL value) {
        return target(value.toURI());
    }

    /**
     * Used to configure the proxy server expectations with a Consumer, which will have an instance of
     * ProxyExpectations passed into it.
     *
     * @param consumer the configuration consumer
     * @return a reference to this configuration
     */
    ProxyServerConfig expectations(Consumer<ProxyServerExpectations> consumer);

    /**
     * Allows the configuration of the number of IO and Worker threads to be used by the underlying server.
     *
     * @param io the number of IO threads (should be fewer than workers; default is 1)
     * @param worker the number of worker threads (should be more than io; default is 4)
     * @return a reference to this server configuration
     */
    ProxyServerConfig serverThreads(int io, int worker);

    /**
     * Allows the configuration of the number of IO threads to be used by the underlying server. The worker threads will
     * be configured to 4 times the configured io number.
     *
     * @param io the number of IO threads (should be a small number; default is 1)
     * @return a reference to this server configuration
     */
    default ProxyServerConfig serverThreads(final int io) {
        return serverThreads(io, io * 4);
    }
}
