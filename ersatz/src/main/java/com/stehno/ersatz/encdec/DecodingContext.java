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
package com.stehno.ersatz.encdec;

/**
 * Request-specific contextual information used by request content decoders.
 */
public class DecodingContext {

    private final long contentLength;
    private final String contentType;
    private final String characterEncoding;
    private final DecoderChain decoderChain;

    /**
     * Creates a new decoding context with the provided parameters.
     *
     * @param contentLength the content-length
     * @param contentType the content-type
     * @param characterEncoding the character-encoding
     * @param decoderChain the available decoder chain
     */
    public DecodingContext(long contentLength, String contentType, String characterEncoding, DecoderChain decoderChain) {
        this.contentLength = contentLength;
        this.contentType = contentType;
        this.characterEncoding = characterEncoding;
        this.decoderChain = decoderChain;
    }

    /**
     * Used to retrieve the content-length.
     *
     * @return the content-length
     */
    public long getContentLength() {
        return contentLength;
    }

    /**
     * Used to retrieve the content-type.
     *
     * @return the content-type
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Used to retrieve the character-encoding.
     *
     * @return the character-encoding
     */
    public String getCharacterEncoding() {
        return characterEncoding;
    }

    /**
     * Used to retrieve the available decoder chain.
     *
     * @return the available decoder chain
     */
    public DecoderChain getDecoderChain() {
        return decoderChain;
    }
}
