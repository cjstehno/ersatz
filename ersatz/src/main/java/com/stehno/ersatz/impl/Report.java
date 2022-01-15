/**
 * Copyright (C) 2020 Christopher J. Stehno
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
package com.stehno.ersatz.impl;

/**
 * Defines a match-error report.
 */
public interface Report {

    /**
     * Console code for the color RED.
     */
    String RED = "\u001B[31m";

    /**
     * Console code for the color GREEN.
     */
    String GREEN = "\u001B[32m";

    /**
     * Console code to reset the color.
     */
    String RESET = "\u001B[0m";

    /**
     * A "checkmark" symbol.
     */
    String CHECKMARK = "+";

    /**
     * Renders the report as a String.
     *
     * @return the report, as a String
     */
    String render();
}
