/**
 * Copyright (C) 2024 Christopher J. Stehno
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
package io.github.cjstehno.ersatz.impl.matchers;

import io.github.cjstehno.ersatz.server.ClientRequest;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A matcher used to count the number of times the wrapped matcher is matched (true).
 */
@RequiredArgsConstructor(staticName = "countingMatcher")
public class MatchCountingMatcher extends BaseMatcher<ClientRequest> {

    private final Matcher<ClientRequest> matcher;
    private final AtomicInteger count = new AtomicInteger(0);

    /**
     * Retrieves the number of times the wrapped matcher was called and returned a positive match.
     *
     * @return the count of positive matches
     */
    public int getMatchedCount(){
        return count.get();
    }

    @Override public boolean matches(final Object actual) {
        val matched = matcher.matches(actual);

        if(matched){
            count.incrementAndGet();
        }

        return matched;
    }

    @Override public void describeTo(final Description description) {
        matcher.describeTo(description);
    }
}
