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
package com.stehno.ersatz.junit;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.rules.ExternalResource;

/**
 * A JUnit Vintage (4.x) test rule providing a simplified framework for using <code>ErsatzServer</code> in unit tests.
 *
 * Note: If you are using JUnit 5+ you must use the <code>ErsatzServerExtension</code> class instead.
 */
public class ErsatzServerRule extends ExternalResource {

    private final TestingHarness harness = new TestingHarness();
    private final Object testInstance;

    /**
     * Creates the rule with a reference to the enclosing test.
     *
     * @param testInstance the enclosing test instance
     */
    public ErsatzServerRule(final Object testInstance) {
        this.testInstance = testInstance;
    }

    /**
     * Finds a field of type <code>ErsatzServer</code> in the enclosing test class. If it is <code>null</code>, an instance
     * of <code>ErsatzServer</code> will be created. The <code>clearExpectations()</code> method will then be called.
     *
     * @throws Throwable if there is a problem setting up the test components
     */
    @Override protected void before() throws Throwable {
        harness.before(testInstance);
    }

    /**
     * Finds a field of type <code>ErsatzServer</code> and calls <code>close()</code> on it if it exists.
     */
    @Override @SuppressFBWarnings("DE_MIGHT_IGNORE")
    protected void after() {
        try {
            harness.after(testInstance);
        } catch (Exception e) {
            // ignore
        }
    }
}
