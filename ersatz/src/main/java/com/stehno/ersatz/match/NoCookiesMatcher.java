/**
 * Copyright (C) 2020 Christopher J. Stehno
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
package com.stehno.ersatz.match;

import com.stehno.ersatz.encdec.Cookie;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.util.Map;

/**
 * A Hamcrest matcher for matching the case when there should be no cookies configured in a request.
 */
public class NoCookiesMatcher extends BaseMatcher<Map<String, Cookie>> {

    /**
     * Provides a matcher which expects there to be no cookies.
     *
     * @return a matcher which expects there to be no cookies
     */
    public static NoCookiesMatcher noCookies() {
        return new NoCookiesMatcher();
    }

    @Override
    public boolean matches(final Object item) {
        if (!(item instanceof Map)) {
            return false;
        }

        return ((Map)item).isEmpty();
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("NoCookiesMatcher: ");
    }
}