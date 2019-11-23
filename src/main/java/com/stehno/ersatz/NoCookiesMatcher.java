package com.stehno.ersatz;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.util.Map;

/**
 * A Hamcrest matcher for matching the case when there should be no cookies configured in a request.
 */
public class NoCookiesMatcher extends BaseMatcher<Map<String, Cookie>> {

    public static NoCookiesMatcher noCookies() {
        return new NoCookiesMatcher();
    }

    @Override public boolean matches(final Object item) {
        if (!(item instanceof Map)) {
            return false;
        }

        return ((Map) item).isEmpty();
    }

    @Override public void describeTo(Description description) {
        description.appendText("NoCookiesMatcher: ");
    }
}
