package com.stehno.ersatz.impl

import com.stehno.ersatz.ClientRequest
import com.stehno.ersatz.Request
import groovy.text.GStringTemplateEngine
import groovy.text.Template
import groovy.text.TemplateEngine
import groovy.transform.CompileStatic

/**
 * FIXME: document
 */
@CompileStatic
class UnmatchedRequestReport {

    private final Template requestTemplate
    private final Template expectationTemplate
    private final ClientRequest request
    private final List<Request> expectations

    UnmatchedRequestReport(final ClientRequest request, final List<Request> expectations) {
        this.request = request
        this.expectations = expectations

        TemplateEngine engine = new GStringTemplateEngine()
        requestTemplate = engine.createTemplate(UnmatchedRequestReport.getResource('/client-request-template.txt'))
        expectationTemplate = engine.createTemplate(UnmatchedRequestReport.getResource('/expectation-template.txt'))
    }


    @Override String toString() {
        """
# Unmatched Request:

${clientRequestToString(request)}

# Expected Requests:
        
        """.stripIndent()
    }

    // TODO: consider pulling this out into a helper object used by both impls (?)
    // ClientRequest is an interface with two very different implementations - doing to String here for now
    private String clientRequestToString(final ClientRequest cr) {
        requestTemplate.make(cr: cr)
    }

    private String requestToString(final Request req) {
        expectationTemplate.make(req: req)
    }
}
