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
package io.github.cjstehno.ersatz.cfg;

/**
 * Defines the available types of web socket messages.
 */
public enum MessageType {

    /**
     * Denotes a text-based message.
     */
    TEXT,

    /**
     * Denotes a byte-based message.
     */
    BINARY;

    /**
     * Resolves the default message type for the specified object.
     *
     * @param obj the payload object
     * @return the message type determined by content
     */
    public static MessageType resolve(final Object obj) {
        return obj instanceof byte[] ? BINARY : TEXT;
    }
}