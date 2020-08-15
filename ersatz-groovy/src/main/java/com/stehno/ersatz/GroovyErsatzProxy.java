package com.stehno.ersatz;

import com.stehno.ersatz.cfg.ProxyConfig;
import com.stehno.ersatz.impl.GroovyProxyConfigImpl;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import space.jasan.support.groovy.closure.ConsumerWithDelegate;

import static groovy.lang.Closure.DELEGATE_FIRST;

public class GroovyErsatzProxy extends ErsatzProxy {

    /**
     * Creates a new proxy server with the specified configuration. The configuration closure will delegate to an instance of <code>ProxyConfig</code>
     * for the actual configuration.
     * <p>
     * If auto-start is not disabled, the server will be started upon creation.
     *
     * @param closure the configuration closure.
     */
    public GroovyErsatzProxy(@DelegatesTo(value = ProxyConfig.class, strategy = DELEGATE_FIRST) final Closure closure) {
        super(new GroovyProxyConfigImpl(), ConsumerWithDelegate.create(closure));
    }
}
