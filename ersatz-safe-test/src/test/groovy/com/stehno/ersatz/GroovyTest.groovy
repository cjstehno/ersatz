package com.stehno.ersatz

import com.stehno.ersatz.junit.ErsatzServerExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

import static com.stehno.ersatz.cfg.ContentType.TEXT_PLAIN
import static com.stehno.ersatz.match.ErsatzMatchers.stringIterableMatcher
import static org.hamcrest.CoreMatchers.containsStringIgnoringCase

@ExtendWith(ErsatzServerExtension)
class GroovyTest {

    // FIXME: test the whole DSL

    private GroovyErsatzServer server

    @Test
    void testing() {
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
