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
import lombok.val;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Field;
import java.util.function.Supplier;

import static java.util.Arrays.stream;

/**
 * JUnit 5 Extension used to provide a simple means of managing an ErsatzServer instance during testing.
 * <p>
 * A field of type <code>ErsatzServer</code> must be specified in the test class.
 * <p>
 * BeforeEach test - the expectations will be cleared.
 * AfterEach test - the server will be stopped.
 * <p>
 * Note: the <code>verify()</code> method is intentionally NOT called by this extension so that it may be called
 * only when needed.
 */
public class ErsatzServerExtension implements BeforeEachCallback, AfterEachCallback {

    @Override public void beforeEach(final ExtensionContext context) throws Exception {
        findInstance(context.getRequiredTestInstance(), true).start();
    }

    @Override public void afterEach(final ExtensionContext context) throws Exception {
        val ersatzInstance = findInstance(context.getRequiredTestInstance(), false);
        if (ersatzInstance != null) {
            ersatzInstance.close();
            ersatzInstance.clearExpectations();
        }
    }

    private static ErsatzServer findInstance(final Object testInstance, final boolean create) throws Exception {
        val field = findField(testInstance);
        Object instance = field.get(testInstance);

        if (instance == null && create) {
            instance = field.getType().getDeclaredConstructor().newInstance();
            field.set(testInstance, instance);
        }

        return (ErsatzServer) instance;
    }

    private static Field findField(final Object testInstance) throws Exception {
        val field = stream(testInstance.getClass().getDeclaredFields())
            .filter(f -> f.getType().getSimpleName().endsWith("ErsatzServer"))
            .findFirst()
            .orElseThrow((Supplier<Exception>) () -> new IllegalArgumentException("An ErsatzServer field must be specified."));

        field.setAccessible(true);
        return field;
    }
}