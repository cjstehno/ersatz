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
package io.github.cjstehno.ersatz;

import lombok.NoArgsConstructor;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE) @SuppressWarnings("unused")
public final class TestArguments {

    public static Stream<Arguments> httpAndHttpsWithContent() {
        return Stream.of(
            Arguments.of(false, "insecure content"),
            Arguments.of(true, "secure content")
        );
    }

    public static Stream<Arguments> httpAndHttps() {
        return Stream.of(
            Arguments.of(false),
            Arguments.of(true)
        );
    }

}
