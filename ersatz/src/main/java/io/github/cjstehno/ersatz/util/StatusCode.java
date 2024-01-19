/**
 * Copyright (C) 2024 Christopher J. Stehno
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
@RequiredArgsConstructor(access = PRIVATE) @SuppressWarnings("checkstyle:MagicNumber")
public enum StatusCode {

    // 100s

    /**
     * HTTP Status Code for Continue 100.
     */
    CONTINUE(100),

    /**
     * HTTP Status Code for Switching Protocols 101.
     */
    SWITCHING_PROTOCOLS(101),

    /**
     * HTTP Status Code for Processing 102.
     */
    PROCESSING(102),

    // 200s

    /**
     * HTTP Status Code for Ok 200.
     */
    OK(200),

    /**
     * HTTP Status Code for Created 201.
     */
    CREATED(201),

    /**
     * HTTP Status Code for Accepted 202.
     */
    ACCEPTED(202),

    /**
     * HTTP Status Code for Non-Authoritative Information 203.
     */
    NON_AUTHORITATIVE_INFORMATION(203),

    /**
     * HTTP Status Code for No Content 204.
     */
    NO_CONTENT(204),

    /**
     * HTTP Status Code for Reset Content 205.
     */
    RESET_CONTENT(205),

    /**
     * HTTP Status Code for Partial Content 206.
     */
    PARTIAL_CONTENT(206),

    /**
     * HTTP Status Code for Multi-Status 207.
     */
    MULTI_STATUS(207),

    /**
     * HTTP Status Code for Already Reported 208.
     */
    ALREADY_REPORTED(208),

    // 300s

    /**
     * HTTP Status Code for Multiple Choice 300.
     */
    MULTIPLE_CHOICE(300),

    /**
     * HTTP Status Code for Moved Permanently 301.
     */
    MOVED_PERMANENTLY(301),

    /**
     * HTTP Status Code for Found 302.
     */
    FOUND(302),

    /**
     * HTTP Status Code for See Other 303.
     */
    SEE_OTHER(303),

    /**
     * HTTP Status Code for Not Modified.
     */
    NOT_MODIFIED(304),

    /**
     * HTTP Status Code for Temporary Redirect 307.
     */
    TEMPORARY_REDIRECT(307),

    /**
     * HTTP Status Code for Permanent Redirect 308.
     */
    PERMANENT_REDIRECT(308),

    // 400s

    /**
     * HTTP Status Code for Bad Request 400.
     */
    BAD_REQUEST(400),

    /**
     * HTTP Status Code for Unauthorized 401.
     */
    UNAUTHORIZED(401),

    /**
     * HTTP Status Code for Payment Required 402.
     */
    PAYMENT_REQUIRED(402),

    /**
     * HTTP Status Code for Forbidden 403.
     */
    FORBIDDEN(403),

    /**
     * HTTP Status Code for Not Found 404.
     */
    NOT_FOUND(404),

    /**
     * HTTP Status Code for Method Not Allowed 405.
     */
    METHOD_NOT_ALLOWED(405),

    /**
     * HTTP Status Code for Not Acceptable 406.
     */
    NOT_ACCEPTABLE(406),

    /**
     * HTTP Status Code for Request Timeout 408.
     */
    REQUEST_TIMEOUT(408),

    /**
     * HTTP Status Code for Conflict 409.
     */
    CONFLICT(409),

    /**
     * HTTP Status Code for Gone 410.
     */
    GONE(410),

    /**
     * HTTP Status Code for Length Required 411.
     */
    LENGTH_REQUIRED(411),

    /**
     * HTTP Status Code for Precondition Failed.
     */
    PRECONDITION_FAILED(412),

    /**
     * HTTP Status Code for Payload Too Large 413.
     */
    PAYLOAD_TOO_LARGE(413),

    /**
     * HTTP Status Code for URI Too Long 414.
     */
    URI_TOO_LONG(414),

    /**
     * HTTP Status Code for Unsupported Media Type 415.
     */
    UNSUPPORTED_MEDIA_TYPE(415),

    /**
     * HTTP Status Code for Misdirected Request 421.
     */
    MISDIRECTED_REQUEST(421),

    /**
     * HTTP Status Code for Unprocessable Entity 422.
     */
    UNPROCESSABLE_ENTITY(422),

    /**
     * HTTP Status Code for Locked 423.
     */
    LOCKED(423),

    /**
     * HTTP Status Code for Failed Dependency 424.
     */
    FAILED_DEPENDENCY(424),

    /**
     * HTTP Status Code for Upgrade Required 426.
     */
    UPGRADE_REQUIRED(426),

    /**
     * HTTP Status Code for Precondition Required 428.
     */
    PRECONDITION_REQUIRED(428),

    /**
     * HTTP Status Code for Too Many Requests 429.
     */
    TOO_MANY_REQUESTS(429),

    /**
     * HTTP Status Code for Request Header Fields Too Large 431.
     */
    REQUEST_HEADER_FIELDS_TOO_LARGE(431),

    /**
     * HTTP Status Code for Unavailable for Legal Reasons 451.
     */
    UNAVAILABLE_FOR_LEGAL_REASONS(451),

    // 500s

    /**
     * HTTP Status Code for Internal Server Error 500.
     */
    INTERNAL_SERVER_ERROR(500),

    /**
     * HTTP Status Code for Not Implemented 501.
     */
    NOT_IMPLEMENTED(501),

    /**
     * HTTP Status Code for Bad Gateway 502.
     */
    BAD_GATEWAY(502),

    /**
     * HTTP Status Code for Service Unavailable 503.
     */
    SERVICE_UNAVAILABLE(503),

    /**
     * HTTP Status Code for Gateway Timeout 504.
     */
    GATEWAY_TIMEOUT(504),

    /**
     * HTTP Status Code for HTTP Version Not Supported 505.
     */
    HTTP_VERSION_NOT_SUPPORTED(505),

    /**
     * HTTP Status Code for Insufficient Storage 507.
     */
    INSUFFICIENT_STORAGE(507),

    /**
     * HTTP Status Code for Loop Detected 508.
     */
    LOOP_DETECTED(508),

    /**
     * HTTP Status Code for Not Extended 510.
     */
    NOT_EXTENDED(510),

    /**
     * HTTP Status Code for Network Authentication Requested 511.
     */
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

    private static boolean isBetween(final StatusCode code, final int lowInclusive, final int highInclusive) {
        return code.value >= lowInclusive && code.value <= highInclusive;
    }
}
