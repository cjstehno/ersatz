/*
 * Copyright (C) 2019 Christopher J. Stehno
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
package com.stehno.ersatz.impl

import com.stehno.ersatz.cfg.HttpMethod
import com.stehno.ersatz.impl.MockClientRequest
import com.stehno.ersatz.match.ProxyRequestMatcher
import org.hamcrest.Matchers
import spock.lang.Specification

class ProxyRequestMatcherSpec extends Specification {

    def 'matcher'() {
        setup:
        ProxyRequestMatcher matcher = new ProxyRequestMatcher(
            Matchers.equalTo(HttpMethod.POST),
            Matchers.endsWith('somewhere')
        )

        when:
        boolean matched = matcher.matches(new MockClientRequest(method: HttpMethod.GET, path: '/foo/bar'))

        then:
        !matched

        and:
        matcher.matchCount == 0

        when:
        matched = matcher.matches(new MockClientRequest(method: HttpMethod.POST, path: '/foo/bar/somewhere'))

        then:
        matched

        and:
        matcher.matchCount == 1
    }
}
