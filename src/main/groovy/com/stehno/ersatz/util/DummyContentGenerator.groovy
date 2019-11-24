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
package com.stehno.ersatz.util

import groovy.transform.CompileStatic

/**
 * Provides a means to generate dummy request/response content for testing.
 */
@CompileStatic
class DummyContentGenerator {

    /**
     * Generates a byte array with the specified size (based on units). The byte array will consist of <code>1 as byte</code> values.
     *
     * @param size the number
     * @param unit the units
     * @return the generated byte array
     */
    static byte[] generate(final double size, final StorageUnit unit) {
        int count = StorageUnit.BYTES.approximate(size, unit) as int
        byte[] bytes = new byte[count]
        Arrays.fill(bytes, 1 as byte)
        bytes
    }
}
