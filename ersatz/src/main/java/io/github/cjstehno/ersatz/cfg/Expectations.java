/**
 * Copyright (C) 2022 Christopher J. Stehno
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
package io.github.cjstehno.ersatz.cfg;

/**
 * The <code>Expectations</code> interface is the root element of the expectation configuration, which provides the
 * ability to define request expectations and responses for test interactions.
 * <p>
 * Internal expectation matching is done using <a href="http://hamcrest.org/" target="_blank">Hamcrest</a>
 * <code>Matcher</code>s - the methods without explicit Matches provide one as a convenience based on the property and
 * value type (see method description). All configured matchers must match for a specific expectation to be considered
 * a match and if there are multiple matching expectations, the first one configured will be the one considered as the
 * match.
 */
public interface Expectations extends AnyExpectations, GetExpectations, HeadExpectations, PostExpectations, PutExpectations, DeleteExpectations, PatchExpectations, OptionsExpectations {
    // just the place they all come together.
}
