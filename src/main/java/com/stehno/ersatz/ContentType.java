package com.stehno.ersatz;

import java.util.Objects;

import static java.lang.String.format;

/**
 * A content-type representation. Some of the standard content-types are provided as static constants for use elsewhere, others may be created as
 * instances of this class as needed.
 */
public class ContentType {

    public static final String CONTENT_TYPE_HEADER = "Content-Type";
    public static final ContentType TEXT_PLAIN = new ContentType("text/plain");
    public static final ContentType TEXT_HTML = new ContentType("text/html");
    public static final ContentType TEXT_JAVASCRIPT = new ContentType("text/javascript");
    public static final ContentType APPLICATION_JAVASCRIPT = new ContentType("application/javascript");
    public static final ContentType TEXT_XML = new ContentType("text/xml");
    public static final ContentType APPLICATION_XML = new ContentType("application/xml");
    public static final ContentType APPLICATION_JSON = new ContentType("application/json");
    public static final ContentType TEXT_JSON = new ContentType("text/json");
    public static final ContentType APPLICATION_URLENCODED = new ContentType("application/x-www-form-urlencoded");
    public static final ContentType MULTIPART_FORMDATA = new ContentType("multipart/form-data");
    public static final ContentType MULTIPART_MIXED = new ContentType("multipart/mixed");
    public static final ContentType IMAGE_JPG = new ContentType("image/jpeg");
    public static final ContentType IMAGE_PNG = new ContentType("image/png");
    public static final ContentType IMAGE_GIF = new ContentType("image/gif");
    public static final ContentType MESSAGE_HTTP = new ContentType("message/http");

    private final String value;

    public ContentType(String value) {
        this.value = value;
    }

    public final String getValue() {
        return value;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContentType that = (ContentType) o;
        return Objects.equals(value, that.value);
    }

    @Override public int hashCode() {
        return Objects.hash(value);
    }

    @Override public String toString() {
        return format("ContentType{value='%s'}", value);
    }
}
