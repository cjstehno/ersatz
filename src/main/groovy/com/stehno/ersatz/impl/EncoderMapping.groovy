/*
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
package com.stehno.ersatz.impl

import groovy.transform.Immutable

import javax.activation.MimeType
import java.util.function.Function

/**
 * Immutable mapping of a content-type and object type to an encoder.
 */
@Immutable(knownImmutableClasses = [MimeType, Function])
class EncoderMapping {

    MimeType contentType
    Class objectType
    Function<Object, String> encoder
}
