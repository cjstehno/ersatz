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
package io.github.cjstehno.ersatz.junit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation used by the <code>ErsatzServerExtension</code> to denote that a test method should use the specified
 * configuration provider, rather than any default configuration.
 */
@Target({METHOD, TYPE})
@Retention(RUNTIME)
@Documented
public @interface ApplyServerConfig {

    /**
     * The name of the method that should be used to configure an instance of the <code>ServerConfig</code> interface.
     * The should return no value (<code>void</code>), and accept a <code>ServerConfig</code> value as its only
     * parameter.
     *
     * The body of the method should perform any configuration operations on the <code>ServerConfig</code> instance
     * provided.
     *
     * @return the configuration method name (defaults to "serverConfig").
     */
    String value() default "serverConfig";
}
