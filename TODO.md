
- server should be restartable (to apply additional configuration)
- need parallel testing of the Java builder API
- separate builders from underlying model (will be used by DSL too)

- setup Travis for CI builds
- setup Coveralls for coverage

# GET Method Support

- request cookies
- response cookies

- call verification
    - once
    - never
    - atMost
    - called {}
    - onCall | onRest
    
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

