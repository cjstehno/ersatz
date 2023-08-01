/**
 * Copyright (C) 2023 Christopher J. Stehno
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
package io.github.cjstehno.ersatz.impl;

import io.github.cjstehno.ersatz.cfg.RequestRequirement;
import io.github.cjstehno.ersatz.cfg.Requirements;
import io.github.cjstehno.ersatz.match.HttpMethodMatcher;
import io.github.cjstehno.ersatz.match.PathMatcher;
import io.github.cjstehno.ersatz.server.ClientRequest;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.val;

/**
 * Implementation of the Requirements interface used to perform the configuration of the request requirements.
 */
public class RequirementsImpl implements Requirements {

    @Getter private final List<ErsatzRequestRequirement> requirements = new LinkedList<>();

    @Override
    public RequestRequirement that(final HttpMethodMatcher methodMatcher, final PathMatcher pathMatcher, final Consumer<RequestRequirement> config) {
        val requirement = new ErsatzRequestRequirement(methodMatcher, pathMatcher);

        if (config != null) {
            config.accept(requirement);
        }

        requirements.add(requirement);

        return requirement;
    }

    /**
     * Clears the configured requirements.
     */
    public void clear() {
        requirements.clear();
    }

    /**
     * Checks the request against the configured requirements and returns true if all requirements are met.
     *
     * @param clientRequest the request
     * @return a value of true if the configured requirements are met
     */
    public boolean check(final ClientRequest clientRequest) {
        if (!requirements.isEmpty()) {
            val applicable = requirements.stream().filter(r -> r.matches(clientRequest)).toList();
            if (!applicable.isEmpty()) {
                return applicable.stream().allMatch(r -> r.check(clientRequest));
            } else {
                return true;
            }
        } else {
            return true;
        }
    }
}
