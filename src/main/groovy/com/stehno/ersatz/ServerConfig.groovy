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
package com.stehno.ersatz

import groovy.transform.CompileStatic

import java.util.function.Consumer

/**
 * Configuration interface for an Ersatz server instance.
 */
@CompileStatic
interface ServerConfig {

    ServerConfig feature(ServerFeature feature)

    ServerConfig expectations(final Consumer<Expectations> expects)

    ServerConfig expectations(@DelegatesTo(Expectations) final Closure closure)

    Expectations expects()

    void start()
}
