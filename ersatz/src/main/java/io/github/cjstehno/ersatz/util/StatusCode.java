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
package io.github.cjstehno.ersatz.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static java.util.Arrays.stream;
import static lombok.AccessLevel.PRIVATE;

/**
 * Some available HTTP response status codes.
 */
@RequiredArgsConstructor(access = PRIVATE)
public enum StatusCode {

    // 100s
    CONTINUE(100),
    SWITCHING_PROTOCOLS(101),
    PROCESSING(102),

    // 200s
    OK(200),
    CREATED(201),
    ACCEPTED(202),
    NON_AUTHORITATIVE_INFORMATION(203),
    NO_CONTENT(204),
    RESET_CONTENT(205),
    PARTIAL_CONTENT(206),
    MULTI_STATUS(207),
    ALREADY_REPORTED(208),

    // 300s
    MULTIPLE_CHOICE(300),
    MOVED_PERMANENTLY(301),
    FOUND(302),
    SEE_OTHER(303),
    NOT_MODIFIED(304),
    TEMPORARY_REDIRECT(307),
    PERMANENT_REDIRECT(308),

    // 400s
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    PAYMENT_REQUIRED(402),
    FORBIDDEN(403),
    NOT_FOUND(404),
    METHOD_NOT_ALLOWED(405),
    NOT_ACCEPTABLE(406),
    REQUEST_TIMEOUT(408),
    CONFLICT(409),
    GONE(410),
    LENGTH_REQUIRED(411),
    PRECONDITION_FAILED(412),
    PAYLOAD_TOO_LARGE(413),
    URI_TOO_LONG(414),
    UNSUPPORTED_MEDIA_TYPE(415),
    MISDIRECTED_REQUEST(421),
    UNPROCESSABLE_ENTITY(422),
    LOCKED(423),
    FAILED_DEPENDENCY(424),
    UPGRADE_REQUIRED(426),
    PRECONDITION_REQUIRED(428),
    TOO_MANY_REQUESTS(429),
    REQUEST_HEADER_FIELDS_TOO_LARGE(431),
    UNAVAILABLE_FOR_LEGAL_REASONS(451),

    // 500s
    INTERNAL_SERVER_ERROR(500),
    NOT_IMPLEMENTED(501),
    BAD_GATEWAY(502),
    SERVICE_UNAVAILABLE(503),
    GATEWAY_TIMEOUT(504),
    HTTP_VERSION_NOT_SUPPORTED(505),
    INSUFFICIENT_STORAGE(507),
    LOOP_DETECTED(508),
    NOT_EXTENDED(510),
    NETWORK_AUTHENTICATION_REQUIRED(511);

    /**
     * The numerical status value.
     */
    @Getter private final int value;

    /**
     * Retrieves the enum value for the provided status code number value.
     *
     * @param value the status number value
     * @return the StatusCode for the number
     * @throws IllegalArgumentException if there is no enum value for the number value
     */
    public static StatusCode of(final int value) {
        return stream(values()).filter(sc -> sc.value == value).findAny().orElseThrow();
    }

    /**
     * Returns true if the status code is "informational" (100-199).
     *
     * @param code the status code enum
     * @return true if the code is informational
     */
    public static boolean isInformational(final StatusCode code) {
        return isBetween(code, 100, 199);
    }

    /**
     * Returns true if the status code is "successful" (200-299).
     *
     * @param code the status code enum
     * @return true if the code is successful
     */
    public static boolean isSuccessful(final StatusCode code) {
        return isBetween(code, 200, 299);
    }

    /**
     * Returns true if the status code is "redirection" (300-399).
     *
     * @param code the status code enum
     * @return true if the code is redirection
     */
    public static boolean isRedirection(final StatusCode code) {
        return isBetween(code, 300, 399);
    }

    /**
     * Returns true if the status code is a "client error" (400-499).
     *
     * @param code the status code enum
     * @return true if the code is a client error
     */
    public static boolean isClientError(final StatusCode code) {
        return isBetween(code, 400, 499);
    }

    /**
     * Returns true if the status code is a "server error" (500-599).
     *
     * @param code the status code enum
     * @return true if the code is a server error
     */
    public static boolean isServerError(final StatusCode code) {
        return isBetween(code, 500, 599);
    }

    private static boolean isBetween(final StatusCode code, int lowInclusive, int highInclusive) {
        return code.value >= lowInclusive && code.value <= highInclusive;
    }
}
