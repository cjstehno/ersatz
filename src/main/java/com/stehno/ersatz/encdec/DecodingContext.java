/*
 * Copyright (C) 2019 Christopher J. Stehno
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

    public DecodingContext(long contentLength, String contentType, String characterEncoding, DecoderChain decoderChain) {
        this.contentLength = contentLength;
        this.contentType = contentType;
        this.characterEncoding = characterEncoding;
        this.decoderChain = decoderChain;
    }

    public long getContentLength() {
        return contentLength;
    }

    public String getContentType() {
        return contentType;
    }

    public String getCharacterEncoding() {
        return characterEncoding;
    }

    public DecoderChain getDecoderChain() {
        return decoderChain;
    }
}
