/*
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
package com.stehno.ersatz.issues

import com.stehno.ersatz.ErsatzServer
import com.stehno.ersatz.encdec.DecodingContext
import com.stehno.ersatz.junit.ErsatzServerExtension
import com.stehno.ersatz.util.HttpClient
import okhttp3.MediaType
import okhttp3.Response
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

import javax.xml.parsers.DocumentBuilderFactory

import static com.stehno.ersatz.cfg.ContentType.TEXT_XML
import static com.stehno.ersatz.encdec.Decoders.utf8String
import static com.stehno.ersatz.encdec.Encoders.text
import static okhttp3.RequestBody.create
import static org.hamcrest.CoreMatchers.equalTo
import static org.hamcrest.xml.HasXPath.hasXPath
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

@ExtendWith(ErsatzServerExtension)
class BodyContentMatcherTest {

    private ErsatzServer server
    private HttpClient http

    @BeforeEach void beforeEach() {
        http = new HttpClient()
    }

    @Test @DisplayName('matching all of body content')
    void matchingAllBodyContent() {
        String requestXml = '<request><node foo="bar"/></request>'
        String responseXml = '<response>OK</response>'

        server.expectations {
            POST('/posting') {
                decoder 'text/xml; charset=utf-8', utf8String
                body requestXml, 'text/xml; charset=utf-8'
                responder {
                    body responseXml, TEXT_XML
                    encoder TEXT_XML, String, text
                }
            }
        }

        Response response = http.post(server.httpUrl('/posting'), create(MediaType.get('text/xml; charset=utf-8'), requestXml))

        assertEquals responseXml, response.body().string()
    }

    @Test @DisplayName('matching part of body content')
    void matchingPartOfBody() {
        String requestXml = '<request><node foo="bar"/></request>'
        String responseXml = '<response>OK</response>'

        server.expectations {
            POST('/posting') {
                decoder('text/xml; charset=utf-8') { byte[] bytes, DecodingContext ctx ->
                    DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(bytes))
                }
                body hasXPath('string(//request/node/@foo)', equalTo('bar')), 'text/xml; charset=utf-8'
                called 1
                responder {
                    body responseXml, TEXT_XML
                    encoder TEXT_XML, String, text
                }
            }
        }

        Response response = http.post(server.httpUrl('/posting'), create(MediaType.get('text/xml; charset=utf-8'), requestXml))

        assertEquals responseXml, response.body().string()

        response = http.post(server.httpUrl('/posting'), create(MediaType.get('text/xml; charset=utf-8'), '<request><node foo="blah"/></request>'))

        assertEquals 404, response.code()

        assertTrue server.verify()
    }
}
