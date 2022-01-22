# Ersatz Groovy Extension

> This is the Groovy extension to the Ersatz library, providing some helpful extensions (and DSLs) when you are using Groovy.

## Artifacts

Project artifacts are available via the Maven Central repository.

For Gradle:

    testImplementation 'io.github.cjstehno.ersatz-groovy:ersatz:3.0.0'

For Maven:

    <dependency>
        <groupId>io.github.cjstehno.ersatz</groupId>
        <artifactId>ersatz-groovy</artifactId>
        <version>3.0.0</version>
        <scope>test</scope>
    </dependency>
    
Alternately, there is a `safe` (shadowed) version of the library available, which is useful in cases where you already have a version of Undertow in use (to avoid version collisions). See the [Shadow Jar](http://cjstehno.github.io/ersatz/docs/user_guide.html#_shadow_jar) 
section of the User Guide for more information.