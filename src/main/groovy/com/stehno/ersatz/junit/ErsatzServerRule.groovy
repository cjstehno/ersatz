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
 * <code>stop()</code> after each test method.
 *
 * FIXME: document this in the user guide and site features list
 */
@CompileStatic
class ErsatzServerRule extends ErsatzServer implements TestRule {

    /**
     * FIXME: document
     */
    ErsatzServerRule() {
        super()
    }

    /**
     * FIXME: document
     */
    ErsatzServerRule(@DelegatesTo(ServerConfig) final Closure closure) {
        super(closure)
    }

    /**
     * FIXME: document
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
