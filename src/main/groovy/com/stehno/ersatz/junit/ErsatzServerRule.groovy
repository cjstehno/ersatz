/*
 * Copyright (C) 2017 Christopher J. Stehno
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
package com.stehno.ersatz.junit

import com.stehno.ersatz.ErsatzServer
import com.stehno.ersatz.ServerConfig
import groovy.transform.CompileStatic
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

import java.util.function.Consumer

/**
 * JUnit Rule implementation to simplify the use of Ersatz in JUnit tests. It may be used with <code>@Rule</code> or <code>@ClassRule</code>. It's is
 * a drop-in replacement for the <code>ErsatzServer</code> class which provides all of its configuration functionality and adds an automatic call to
 * <code>stop()</code> after each test method. The <code>start()</code> method must still be called manually by the test methods.
 */
@CompileStatic
class ErsatzServerRule extends ErsatzServer implements TestRule {

    /**
     * Creates a server rul with the provided server configuration.
     *
     * @param closure the configuration closure delegating to <code>ServerConfig</code>.
     */
    ErsatzServerRule(@DelegatesTo(ServerConfig) final Closure closure = null) {
        super(closure)
    }

    /**
     * Creates a server rul with the provided server configuration.
     *
     * @param consumer the configuration consumer based on a <code>ServerConfig</code> instance.
     */
    ErsatzServerRule(final Consumer<ServerConfig> consumer) {
        super(consumer)
    }

    @SuppressWarnings('UnusedMethodParameter')
    Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override void evaluate() throws Throwable {
                try {
                    base.evaluate()
                } finally {
                    stop()
                }
            }
        }
    }
}
