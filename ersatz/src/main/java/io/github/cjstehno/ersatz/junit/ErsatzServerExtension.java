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
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.ReflectionSupport;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.create;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;
import static org.junit.platform.commons.support.HierarchyTraversalMode.TOP_DOWN;
import static org.junit.platform.commons.support.ModifierSupport.isNotStatic;
import static org.junit.platform.commons.support.ReflectionSupport.findFields;
import static org.junit.platform.commons.support.ReflectionSupport.findMethod;

/**
 * JUnit 5 Extension used to provide a simple means of managing an ErsatzServer instance during testing.

 * BeforeEach test - the expectations will be cleared.
 * AfterEach test - the server will be stopped.
 *
 * See the <a href="https://cjstehno.github.io/ersatz/docs/user_guide.html">User Guide</a> for more details.
 * <p>
 * Note: the <code>verify()</code> method is intentionally NOT called by this extension so that it may be called
 * only when needed.
 */
@Slf4j
public class ErsatzServerExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private static final String FIELD_SUFFIX = "ErsatzServer";
    private static final String SERVER_KEY = "instance";
    private static final Namespace NAMESPACE = create("ersatz-server", "server");

    /**
     * Called before each test method is executed - an instance of ErsatzServer is located or instantiated, then started.
     *
     * @param context the current extension context; never {@code null}
     * @throws Exception if there is a problem locating or creating the server instance
     */
    @Override
    public void beforeEach(final ExtensionContext context) throws Exception {
        val ersatzServer = resolveAppliedConfig(context)
            .map(cfg -> findServerField(context)
                .map(field -> buildServer(context, field, cfg))
                .orElseGet(() -> new ErsatzServer(cfg)))
            .orElseGet(() -> findServerField(context)
                .map(field -> buildServer(context, field))
                .orElseThrow(() -> new IllegalArgumentException("No server or configuration has been provided.")));

        // store the server in context
        context.getStore(NAMESPACE).put(SERVER_KEY, ersatzServer);

        // start the server
        ersatzServer.start();
    }

    @Override
    public boolean supportsParameter(final ParameterContext paramContext, final ExtensionContext extContext) throws ParameterResolutionException {
        return paramContext.getParameter().getType().getSimpleName().endsWith(FIELD_SUFFIX);
    }

    @Override
    public Object resolveParameter(final ParameterContext paramContext, final ExtensionContext extContext) throws ParameterResolutionException {
        return extContext.getStore(NAMESPACE).get(SERVER_KEY);
    }

    /**
     * Called after each test method has executed - closes the server instance and clears its expectations.
     *
     * @param context the current extension context; never {@code null}
     * @throws Exception if there is a problem shutting down the server
     */
    @Override public void afterEach(final ExtensionContext context) throws Exception {
        val ersatzServer = (ErsatzServer) context.getStore(NAMESPACE).get(SERVER_KEY);
        if (ersatzServer != null) {
            ersatzServer.close();
            ersatzServer.clearExpectations();
        }
    }

    private static ErsatzServer buildServer(final ExtensionContext context, final Field field, final ServerConfig serverConfig) {
        try {
            if (field.get(context.getRequiredTestInstance()) != null) {
                log.warn("An ErsatzServer instance field is configured - it will be overwritten.");
            }

            // configure it with resolved config
            val server = instantiateServer(field.getType(), serverConfig);

            // update the field value
            field.set(context.getRequiredTestInstance(), server);

            return (ErsatzServer) server;

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static ErsatzServer buildServer(final ExtensionContext context, final Field field) {
        try {
            val server = field.get(context.getRequiredTestInstance());
            if (server != null) {
                return (ErsatzServer) server;

            } else {
                // create default
                val newServer = instantiateServer(field.getType());

                // update the field value
                field.set(context.getRequiredTestInstance(), newServer);

                return (ErsatzServer) newServer;
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static Object instantiateServer(final Class<?> type) {
        if (type.getSimpleName().equals("GroovyErsatzServer")) {
            return ReflectionSupport.newInstance(type);
        } else {
            return new ErsatzServer();
        }
    }

    private static Object instantiateServer(final Class<?> type, final ServerConfig serverConfig) {
        if (type.getSimpleName().equals("GroovyErsatzServer")) {
            return ReflectionSupport.newInstance(type, serverConfig);
        } else {
            return new ErsatzServer(serverConfig);
        }
    }

    private static Optional<Field> findServerField(final ExtensionContext context) {
        val fields = findFields(
            context.getRequiredTestClass(),
            f -> isNotStatic(f) && f.getType().getSimpleName().endsWith(FIELD_SUFFIX),
            TOP_DOWN
        );

        if (fields.isEmpty()) {
            return Optional.empty();
        } else {
            val field = fields.get(0);
            field.setAccessible(true);
            return Optional.of(field);
        }
    }

    private static Optional<ServerConfig> resolveAppliedConfig(final ExtensionContext context) {
        // check for annotation on test method -> then check class
        return findAnnotation(context.getRequiredTestMethod(), ApplyServerConfig.class)
            .map(applyServerConfig -> buildServerConfig(context, applyServerConfig))
            .or(() -> findAnnotation(context.getRequiredTestClass(), ApplyServerConfig.class)
                .map(applyServerConfig -> buildServerConfig(context, applyServerConfig)));
    }

    private static ServerConfig buildServerConfig(final ExtensionContext context, final ApplyServerConfig anno) {
        return findMethod(context.getRequiredTestClass(), anno.value(), ServerConfig.class)
            .map(meth -> {
                val serverConfig = new ServerConfigImpl();
                try {
                    meth.setAccessible(true);
                    meth.invoke(context.getRequiredTestInstance(), serverConfig);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
                return serverConfig;
            })
            .orElseThrow(() -> new IllegalArgumentException("No method \"void " + anno.value() + "(ServerConfig)\" exists."));
    }
}