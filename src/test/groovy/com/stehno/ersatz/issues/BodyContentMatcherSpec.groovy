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
package com.stehno.ersatz.issues

import com.stehno.ersatz.ErsatzServer
import com.stehno.ersatz.encdec.DecodingContext
import com.stehno.ersatz.util.HttpClient
import okhttp3.MediaType
import okhttp3.Response
import spock.lang.AutoCleanup
import spock.lang.Specification

import javax.xml.parsers.DocumentBuilderFactory

import static com.stehno.ersatz.cfg.ContentType.TEXT_XML
import static com.stehno.ersatz.encdec.Decoders.utf8String
import static com.stehno.ersatz.encdec.Encoders.text
import static okhttp3.RequestBody.create
import static org.hamcrest.CoreMatchers.equalTo
import static org.hamcrest.xml.HasXPath.hasXPath

class BodyContentMatcherSpec extends Specification {

    @AutoCleanup private ErsatzServer server = new ErsatzServer()
    private HttpClient http = new HttpClient()

    void 'matching all of body content'() {
        setup:
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

        when:
        Response response = http.post(server.httpUrl('/posting'), create(MediaType.get('text/xml; charset=utf-8'), requestXml))

        then:
        response.body().string() == responseXml
    }

    void 'matching part of body content'() {
        setup:
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

        when:
        Response response = http.post(server.httpUrl('/posting'), create(MediaType.get('text/xml; charset=utf-8'), requestXml))

        then:
        response.body().string() == responseXml

        when:
        response = http.post(server.httpUrl('/posting'), create(MediaType.get('text/xml; charset=utf-8'), '<request><node foo="blah"/></request>'))

        then:
        response.code() == 404

        and:
        server.verify()
    }
}
