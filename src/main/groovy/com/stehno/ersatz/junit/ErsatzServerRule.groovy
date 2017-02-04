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
     * Creates a server rule with the default server configuration.
     */
    ErsatzServerRule() {
        super()
    }

    /**
     * Creates a server rul with the provided server configuration.
     *
     * @param closure the configuration closure delegating to <code>ServerConfig</code>.
     */
    ErsatzServerRule(@DelegatesTo(ServerConfig) final Closure closure) {
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
