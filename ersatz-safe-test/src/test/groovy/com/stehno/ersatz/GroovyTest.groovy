package com.stehno.ersatz


import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static com.stehno.ersatz.cfg.ContentType.TEXT_PLAIN
import static com.stehno.ersatz.match.ErsatzMatchers.stringIterableMatcher
import static org.hamcrest.CoreMatchers.containsStringIgnoringCase

//@ExtendWith(ErsatzServerExtension) FIXME: issues with reflection
class GroovyTest {

    private GroovyErsatzServer server

    @BeforeEach void beforeEach() {
        server = new GroovyErsatzServer()
    }

    @AfterEach void afterEach() {
        server.close()
    }

    @Test void testing() {
        server.expectations {
            GET('/foo') {
                query 'name', stringIterableMatcher([containsStringIgnoringCase('groovy')])
                responder {
                    code 200
                    body 'something', TEXT_PLAIN
                }
            }
        }

        assert 'something' == server.httpUrl('/foo?name=groovy').toURL().text
    }
}
