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
package com.stehno.ersatz.junit.jupiter;

import com.stehno.ersatz.junit.TestingHarness;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit 5 Extension used to provide a simple means of managing an ErsatzServer instance during testing.
 * <p>
 * A field of type <code>ErsatzServer</code> must be specified in the test class.
 * <p>
 * BeforeEach test - the expectations will be cleared.
 * AfterEach test - the server will be stopped.
 */
public class ErsatzServerExtension implements BeforeEachCallback, AfterEachCallback {

    private final TestingHarness harness = new TestingHarness();

    @Override public void beforeEach(final ExtensionContext context) throws Exception {
        harness.before(context.getRequiredTestInstance());
    }

    @Override public void afterEach(final ExtensionContext context) throws Exception {
        harness.after(context.getRequiredTestInstance());
    }
}