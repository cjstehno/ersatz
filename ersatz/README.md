# Ersatz 

> This is he core Ersatz library, usable with any Java-based language.

## Artifacts

Project artifacts are available via the JCenter (Bintray) and Maven Central repositories.

For Gradle:

    testCompile 'com.stehno.ersatz:ersatz:3.0.0'

For Maven:

    <dependency>
        <groupId>com.stehno.ersatz</groupId>
        <artifactId>ersatz</artifactId>
        <version>3.0.0</version>
        <scope>test</scope>
    </dependency>
    
Alternately, there is a `safe` (shadowed) version of the library available, which is useful in cases where you already 
have a version of Undertow in use (to avoid version collisions). See the [Shadow Jar](http://stehno.com/ersatz/guide/#_shadow_jar) 
section of the User Guide for more information.