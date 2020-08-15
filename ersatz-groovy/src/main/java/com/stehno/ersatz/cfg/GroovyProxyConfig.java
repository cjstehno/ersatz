package com.stehno.ersatz.cfg;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import space.jasan.support.groovy.closure.ConsumerWithDelegate;

import static groovy.lang.Closure.DELEGATE_FIRST;

public interface GroovyProxyConfig extends ProxyConfig {

    /**
     * Used to configure the proxy server expectations with a Groovy Closure, which delegates to an instance of ProxyExpectations.
     *
     * @param closure the Groovy closure
     * @return a reference to this configuration
     */
    default ProxyConfig expectations(@DelegatesTo(value = ProxyExpectations.class, strategy = DELEGATE_FIRST) Closure closure) {
        return expectations(ConsumerWithDelegate.create(closure));
    }
}
