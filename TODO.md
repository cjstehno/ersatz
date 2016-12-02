
- server should be restartable (to apply additional configuration)
- need parallel testing of the Java builder API
- probably want to pull out a builder to build the underlying model and then use that for the DSL as well

- setup Travis for CI builds
- setup Coveralls for coverage

# GET Method Support

- headers
- content type
- cookies

- response
    - body
    - headers
    - cookies
    - code
    
other assertion criteria

server.requesting {
   get('/some/path').query('alpha').eq('42').responds { resp->
        resp.status(200).header('X-Something','445::X92').contentType('application/json').body('{foo:101}')
   }
  
   get('/some/path'){
       query 'alpha' eq '42'
       query 'bravo' ne 'hello'
       query { q->
           q.charlie == 'something'
       }
       responds(200){
           header 'X-Something', '445::X92'
           header 'C'
           contentType 'application/json'
           body '{foo:42}'
       }
   }

   // default is .any() 
   get(...){ … }.times(2)

   ….once()
   ….never()
   ….atLeast() | atMost()
   ….called { n-> n > 2 && n < 10 }


   ...onCall(n) | onCall(Closure) | onRest()
}

Calling server start on started server will restart
There should be a method to clear config to start fresh
Should be a method to get server host:port information
Should be a way to test/support HTTPS

Consider how to write expectations for requests:
* in certain orders (onCall)
* that change with each time they are called
* groups of requests that must happen in a specified sequence (might not be worth it)
* that must never occur (never())
