
# New Reporting

- record all requests (matched and unmatched)
  - with un/matched expectations

- should display on console/log/error
- verify will probably drive the new reporting 
  - currently it is not required, but should be used by every test
  - will be required for reporting (DOCUMENT)
  - tests will still get back unexpected results even without verify call

What defines a "test run"?

you can create expectations in the server config and in the test method itself
the clear expectations method can reset, but what starts it
verification can be considered the end, but what starts it
the server start method is not viable

maybe I need to enforce more of a state machine


CONFIGURATION -> MATCHING -> VERIFICATION -> CLEANUP

which should become 

CONFIGURATION --start--> MATCHING -> VERIFICATION -> CLEANUP --stop-->


                        
Configuration is made up of server config (which may be done once per test-class or once per test-method), and the 
test method configuration (done each test-method)

Matching is when the server is in request matching mode (for a single test-run) recording its mis/match information in
the report

Verification is when the test actions are done and the verify call is made to ensure that the call and request matches
have been reported. The report is built and provided here

Cleanup is when the expectation matches are reset (and those for the test method are cleared)


I think I need two layers of expectations
server - the matches should be reset but the expecattions should not be cleared (these should not START the server)
test - the matches and expecations are cleared between runs


----

Server Configuration
- server config
- expectations/requirements at server level
- server can be started here

Test Configuration
- expectations at the test level
- server started
- if no expectations, then must call begin()

Matching
- report is build and populated
- testing goes in here

Verification
- call the verify method
- report generated and acceptance determined

Test Cleanup
- call end() method to end testing
- resets the server expectations
- removes the test expecattions
- may return to Test Configuration to apply new test config

Server Cleanup
- call cleanup() to clear/reset the whole server
- call stop() to end all testing

Stopped
- can be restarted (but clean)



NEEDS:
- layered expectations config
- ability to clear by layer