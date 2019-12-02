/**
 * Copyright (C) 2019 Christopher J. Stehno
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stehno.ersatz.junit;

import com.stehno.ersatz.ErsatzServer;

import java.lang.reflect.Field;
import java.util.function.Supplier;

import static java.util.Arrays.stream;

/**
 * JUnit test behavior for Ersatz, used by both versions.
 */
class TestingHarness {

    /**
     * Finds a field in the test instance of type <code>ErsatzServer</code>, creating an instance if one does not
     * already exist. The <code>ErsatzServer</code> instance will then have <code>start()</code> called on it.
     *
     * @param testInstance the instance of the test class
     * @throws Exception if there is a problem performing the operations
     */
    void before(final Object testInstance) throws Exception {
        findInstance(testInstance, true).start();
    }

    /**
     * Finds a field in the test instance of type <code>ErsatzServer</code>. If an instance exists it will call
     * <code>close()</code> and <code>clearExpectations()</code> on it, otherwise, no action will be taken.
     *
     * @param testInstance the instance of the test class
     * @throws Exception if there is a problem performing the operations
     */
    void after(final Object testInstance) throws Exception {
        final var ersatzInstance = findInstance(testInstance, false);
        if (ersatzInstance != null) {
            ersatzInstance.close();
            ersatzInstance.clearExpectations();
        }
    }

    private static ErsatzServer findInstance(final Object testInstance, final boolean create) throws Exception {
        try {
            final Field field = findField(testInstance);
            ErsatzServer instance = (ErsatzServer) field.get(testInstance);

            if (instance == null && create) {
                instance = new ErsatzServer();
                field.set(testInstance, instance);
            }

            return instance;

        } catch (Throwable throwable) {
            throw new Exception(throwable);
        }
    }

    private static Field findField(final Object testInstance) throws Throwable {
        final Field field = stream(testInstance.getClass().getDeclaredFields())
            .filter(f -> f.getType().equals(ErsatzServer.class))
            .findFirst()
            .orElseThrow((Supplier<Throwable>) () -> new IllegalArgumentException("An ErsatzServer field must be specified."));

        field.setAccessible(true);
        return field;
    }
}
