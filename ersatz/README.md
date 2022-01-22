# Ersatz 

> This is he core Ersatz library, usable with any Java-based language.

## Artifacts

Project artifacts are available via the Maven Central repository.

For Gradle:

    testImplementation 'io.github.cjstehno.ersatz:ersatz:3.0.0'

For Maven:

    <dependency>
        <groupId>io.github.cjstehno.ersatz</groupId>
        <artifactId>ersatz</artifactId>
        <version>3.0.0</version>
        <scope>test</scope>
    </dependency>
    
Alternately, there is a `safe` (shadowed) version of the library available, which is useful in cases where you already  have a version of Undertow in use (to avoid version collisions). See the [Shadow Jar](http://cjstehno.github.io/ersatz/docs/user_guide.html#_shadow_jar) 
section of the User Guide for more information.