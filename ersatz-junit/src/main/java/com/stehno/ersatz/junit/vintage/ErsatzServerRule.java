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
package com.stehno.ersatz.junit.vintage;

import com.stehno.ersatz.junit.TestingHarness;
import org.junit.rules.ExternalResource;

/**
 * FIXME: document
 */
public class ErsatzServerRule extends ExternalResource {

    private final TestingHarness harness = new TestingHarness();
    private final Object testInstance;

    public ErsatzServerRule(final Object testInstance) {
        this.testInstance = testInstance;
    }

    @Override protected void before() throws Throwable {
        harness.before(testInstance);
    }

    @Override protected void after() {
        try {
            harness.after(testInstance);
        } catch (Exception e) {
            // ignore
        }
    }
}
