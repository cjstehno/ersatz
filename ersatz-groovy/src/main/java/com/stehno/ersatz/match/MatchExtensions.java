package com.stehno.ersatz.match;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import space.jasan.support.groovy.closure.ConsumerWithDelegate;

import static groovy.lang.Closure.DELEGATE_FIRST;

public class MatchExtensions {

    /**
     * Configures the cookie matcher with a closure delegating to a <code>CookieMatcher</code> instance.
     *
     * @param closure the configuration closure
     * @return the configured matcher
     */
    public static CookieMatcher cookieMatcher(
        final CookieMatcher type,
        @DelegatesTo(value = CookieMatcher.class, strategy = DELEGATE_FIRST) final Closure closure
    ) {
        return CookieMatcher.cookieMatcher(ConsumerWithDelegate.create(closure));
    }

    /**
     * Creates a new multipart matcher with a Groovy DSL closure (delegating to <code>MultipartRequestMatcher</code>).
     *
     * @param closure the configuration closure
     * @return a configured matcher instance
     */
    public static MultipartRequestMatcher multipartMatcher(
        final MultipartRequestMatcher type,
        @DelegatesTo(value = MultipartRequestMatcher.class, strategy = DELEGATE_FIRST) final Closure closure
    ) {
        return MultipartRequestMatcher.multipartMatcher(ConsumerWithDelegate.create(closure));
    }
}
