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

import static java.lang.Class.forName;
import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.create;
import static org.junit.platform.commons.support.ReflectionSupport.newInstance;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * A delegate used to perform the ErsatzServer parameter resolution in an abstracted manner.
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class ErsatzServerParameterResolverDelegate implements ParameterResolver {

    private static final String FIELD_SUFFIX = "ErsatzServer";
    private static final String GROOVY_ERSATZ_SERVER = "GroovyErsatzServer";
    private static final String GROOVY_ERSATZ_SERVER_CLASS = "io.github.cjstehno.ersatz." + GROOVY_ERSATZ_SERVER;
    private static final Namespace NAMESPACE = create("io.github.cjstehno", "ersatz");
    private final String storageKey;

    @Override
    public boolean supportsParameter(final ParameterContext paramContext, final ExtensionContext extContext) throws ParameterResolutionException {
        return paramContext.getParameter().getType().getSimpleName().endsWith(FIELD_SUFFIX);
    }

    @Override
    public Object resolveParameter(final ParameterContext paramContext, final ExtensionContext extContext) throws ParameterResolutionException {
        if (paramContext.getParameter().getType().getSimpleName().equals(GROOVY_ERSATZ_SERVER)) {
            val server = extContext.getStore(NAMESPACE).get(storageKey);
            if (server.getClass().getSimpleName().equals(GROOVY_ERSATZ_SERVER)) {
                return server;
            } else {
                try {
                    return newInstance(forName(GROOVY_ERSATZ_SERVER_CLASS), server);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            return extContext.getStore(NAMESPACE).get(storageKey);
        }
    }
}
