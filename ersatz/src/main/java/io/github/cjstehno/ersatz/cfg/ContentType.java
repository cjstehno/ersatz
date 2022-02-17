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
package io.github.cjstehno.ersatz.cfg;

import lombok.EqualsAndHashCode;

import java.nio.charset.Charset;

/**
 * A content-type representation. Some standard content-types are provided as static constants for use elsewhere,
 * others may be created as instances of this class as needed.
 */
@EqualsAndHashCode
public class ContentType {

    /**
     * The name key for the HTTP content-type header.
     */
    public static final String CONTENT_TYPE_HEADER = "Content-Type";

    /**
     * Constant content-type for application/javascript.
     */
    public static final ContentType APPLICATION_JAVASCRIPT = new ContentType("application/javascript");

    /**
     * Constant content-type for application/json.
     */
    public static final ContentType APPLICATION_JSON = new ContentType("application/json");

    /**
     * Constant content-type for application/x-www-form-urlencoded.
     */
    public static final ContentType APPLICATION_URLENCODED = new ContentType("application/x-www-form-urlencoded");

    /**
     * Constant content-type for application/xml.
     */
    public static final ContentType APPLICATION_XML = new ContentType("application/xml");

    /**
     * Constant content-type for audio/mpeg.
     */
    public static final ContentType AUDIO_MPEG = new ContentType("audio/mpeg");

    /**
     * Constant content-type for audio/ogg.
     */
    public static final ContentType AUDIO_OGG = new ContentType("audio/ogg");

    /**
     * Constant content-type for audio/wav.
     */
    public static final ContentType AUDIO_WAV = new ContentType("audio/wav");

    /**
     * Constant content-type for image/gif.
     */
    public static final ContentType IMAGE_GIF = new ContentType("image/gif");

    /**
     * Constant content-type for image/jpeg.
     */
    public static final ContentType IMAGE_JPG = new ContentType("image/jpeg");

    /**
     * Constant content-type for image/png.
     */
    public static final ContentType IMAGE_PNG = new ContentType("image/png");

    /**
     * Constant content-type for image/svg+xml.
     */
    public static final ContentType IMAGE_SVG_XML = new ContentType("image/svg+xml");

    /**
     * Constant content-type for message/http.
     */
    public static final ContentType MESSAGE_HTTP = new ContentType("message/http");

    /**
     * Constant content-type for multipart/form-data.
     */
    public static final ContentType MULTIPART_FORMDATA = new ContentType("multipart/form-data");

    /**
     * Constant content-type for multipart/mixed.
     */
    public static final ContentType MULTIPART_MIXED = new ContentType("multipart/mixed");

    /**
     * Constant content-type for text/css.
     */
    public static final ContentType TEXT_CSS = new ContentType("text/css");

    /**
     * Constant content-type for text/CSV.
     */
    public static final ContentType TEXT_CSV = new ContentType("text/CSV");

    /**
     * Constant content-type for text/html.
     */
    public static final ContentType TEXT_HTML = new ContentType("text/html");

    /**
     * Constant content-type for text/javascript.
     */
    public static final ContentType TEXT_JAVASCRIPT = new ContentType("text/javascript");

    /**
     * Constant content-type for text/json.
     */
    public static final ContentType TEXT_JSON = new ContentType("text/json");

    /**
     * Constant content-type for text/plain.
     */
    public static final ContentType TEXT_PLAIN = new ContentType("text/plain");

    /**
     * Constant content-type for text/xml.
     */
    public static final ContentType TEXT_XML = new ContentType("text/xml");

    /**
     * Constant content-type for video/mpeg.
     */
    public static final ContentType VIDEO_MPEG = new ContentType("video/mpeg");

    /**
     * Constant content-type for video/ogg.
     */
    public static final ContentType VIDEO_OGG = new ContentType("video/ogg");

    private final String value;

    /**
     * Creates a content-type with the specified value.
     *
     * @param value the content-type value
     */
    public ContentType(final String value) {
        this.value = value;
    }

    /**
     * Creates a content-type with the specified value and charset (as <code>[content-type]; charset=[charset]</code>).
     *
     * @param value the content-type value
     * @param charset the content-type charset
     */
    public ContentType(final String value, final String charset) {
        this.value = value + "; charset=" + charset;
    }

    /**
     * Used to retrieve the value of the content-type.
     *
     * @return the String value of the content-type
     */
    public String getValue() {
        return value;
    }

    /**
     * Creates a new ContentType object from this one with the specified "charset" appended to it.
     *
     * @param charset the charset to be applied
     * @return the content-type and charset wrapped in a ContentType object
     */
    public ContentType withCharset(final String charset) {
        return new ContentType(value, charset);
    }

    /**
     * Creates a new ContentType object from this one with the specified "charset" appended to it.
     *
     * @param charset the charset to be applied
     * @return the content-type and charset wrapped in a ContentType object
     */
    public ContentType withCharset(final Charset charset) {
        return new ContentType(value, charset.name().toLowerCase());
    }

    /**
     * An alias for <code>getValue()</code>.
     *
     * @return a String representation of the content-type
     */
    @Override public String toString() {
        return value;
    }
}
