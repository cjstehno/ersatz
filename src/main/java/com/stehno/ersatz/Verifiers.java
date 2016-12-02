/**
 * Copyright (C) 2016 Christopher J. Stehno
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
package com.stehno.ersatz;

import java.util.function.Function;

/**
 * Created by cjstehno on 12/2/16.
 */
public class Verifiers {

    public static Function<Integer, Boolean> any() {
        return count -> true;
    }

    public static Function<Integer, Boolean> atLeast(final int min) {
        return count -> count >= min;
    }

    public static Function<Integer, Boolean> atMost(final int max) {
        return count -> count <= max;
    }

    public static Function<Integer, Boolean> exactly(final int n) {
        return count -> count == n;
    }

    public static Function<Integer, Boolean> once() {
        return exactly(1);
    }

    public static Function<Integer, Boolean> never() {
        return count -> count == 0;
    }
}
