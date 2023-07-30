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
package io.github.cjstehno.ersatz.junit;

import io.github.cjstehno.ersatz.ErsatzServer;
import io.github.cjstehno.ersatz.cfg.ServerConfig;
import io.github.cjstehno.ersatz.impl.ServerConfigImpl;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.extension.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.create;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;
import static org.junit.platform.commons.support.ReflectionSupport.findMethod;

/**
 * JUnit 5 extension used to provide a simple means of managing an ErsatzServer instance during testing, similar to the
 * <code>ErsatzServerExtension</code> - however, this extension differs in that this extension creates the server
 * instance in the <code>beforeAll</code> method, and destroys it in the <code>afterAll</code> method.
 * <p>
 * The test expectations are cleared in the <code>afterEach</code> method.
 * <p>
 * Configuration of the server instance may be done using a class-level <code>@ApplyServerConfig</code> annotation (not
 * method level), or, if none is provided, the default configuration will be used to create the server.
 * <p>
 * Test methods should add an <code>ErsatzServer</code> typed parameter to the test methods that require access to the
 * server instance.
 * <p>
 * Note: if you need more configuration flexibility, the <code>ErsatzServerExtension</code> allows more, but with the
 * caveat that the server is created and torn down with each test method.
 */
@Slf4j
public class SharedErsatzServerExtension implements BeforeAllCallback, AfterEachCallback, AfterAllCallback, ParameterResolver {

    private static final String SERVER_KEY = "shared-instance";
    private static final ExtensionContext.Namespace NAMESPACE = create("io.github.cjstehno", "ersatz");
    private static final ParameterResolver SERVER_PARAM_RESOLVER = new ErsatzServerParameterResolverDelegate(SERVER_KEY);
    private static final String DEFAULT_METHOD_NAME = "serverConfig";

    @Override public void beforeAll(final ExtensionContext context) throws Exception {
        // find configuration and configure server
        val server = resolveAppliedConfig(context)
            .map(sc -> {
                log.info("Creating server instance with provided config.");
                return new ErsatzServer(sc);
            })
            .orElseGet(() -> {
                log.info("Create server instance with no config.");
                return new ErsatzServer();
            });

        context.getStore(NAMESPACE).put(SERVER_KEY, server);

        server.start();
    }

    @Override public void afterEach(final ExtensionContext context) throws Exception {
        val server = (ErsatzServer) context.getStore(NAMESPACE).get(SERVER_KEY);
        if (server != null) {
            server.clearExpectations();
        }
    }

    @Override public void afterAll(final ExtensionContext context) throws Exception {
        val server = (ErsatzServer) context.getStore(NAMESPACE).remove(SERVER_KEY);
        if (server != null) {
            server.stop();
        }
    }

    @Override
    public boolean supportsParameter(final ParameterContext paramContext, final ExtensionContext extContext) throws ParameterResolutionException {
        return SERVER_PARAM_RESOLVER.supportsParameter(paramContext, extContext);
    }

    @Override
    public Object resolveParameter(final ParameterContext paramContext, final ExtensionContext extContext) throws ParameterResolutionException {
        return SERVER_PARAM_RESOLVER.resolveParameter(paramContext, extContext);
    }

    private static Optional<ServerConfig> resolveAppliedConfig(final ExtensionContext context) {
        return findAnnotation(context.getRequiredTestClass(), ApplyServerConfig.class)
            .map(sc -> {
                log.info("Using server config from method named: {}", sc.value());
                return buildServerConfig(context, sc.value());
            })
            .orElseGet(() -> {
                log.info("Using server config from method named: {}", DEFAULT_METHOD_NAME);
                return buildServerConfig(context, DEFAULT_METHOD_NAME);
            });
    }

    private static Optional<ServerConfig> buildServerConfig(final ExtensionContext context, final String configMethodName) {
        return findMethod(context.getRequiredTestClass(), configMethodName, ServerConfig.class)
            .map(meth -> {
                val serverConfig = new ServerConfigImpl();
                try {
                    meth.setAccessible(true);
                    meth.invoke(context.getRequiredTestClass(), serverConfig);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
                return serverConfig;
            });
    }
}
