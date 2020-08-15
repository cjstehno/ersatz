package com.stehno.ersatz


import com.stehno.ersatz.test.Http
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

import static com.stehno.ersatz.cfg.ContentType.TEXT_PLAIN
import static org.junit.jupiter.api.Assertions.assertEquals

class GroovyErsatzServerTest {

    /* FIxNME: test
        - groovy ctor with java config
        - groovy expectations
        - test with mixed java/groovy style
     */

    private GroovyErsatzServer server

    @AfterEach void afterEach() {
        server?.close()
    }

    @Test @DisplayName('empty ctor')
    void ctor() {
        server = new GroovyErsatzServer()

        server.expectations {
            GET('/foo') {
                header 'alpha', 'bravo'
                responder {
                    body 'Groovy, baby!', TEXT_PLAIN
                }
            }
        }

        def value = new Http(server).GET('/foo', alpha: 'bravo').body()
        assertEquals('Groovy, baby!', value)
    }

    @Test @DisplayName('ctor with config')
    void ctorConfig() {
        server = new GroovyErsatzServer({
            expectations {
                GET('/foo') {
                    header 'fun', 'times'
                    responder {
                        body 'Hello, Groovy!', TEXT_PLAIN
                    }
                }
            }
        })

        def value = new Http(server).GET('/foo', fun: 'times').body()
        assertEquals('Hello, Groovy!', value)
    }
}