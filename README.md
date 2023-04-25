# Ersatz Server

> If you use this project and would like to help keep it running, please consider making a donation.
>
> [☕ Buy me a coffee?](https://www.paypal.com/donate/?hosted_button_id=JA246LUCNUDHC)

## Quick Links

* Site: https://cjstehno.github.io/ersatz
* Project: https://github.com/cjstehno/ersatz
* User Guide: https://cjstehno.github.io/ersatz/docs/user_guide.html
* Java API Docs: https://cjstehno.github.io/ersatz/javadoc/
* Groovy API Docs: https://cjstehno.github.io/ersatz-groovy/javadoc/

## Introduction

The Ersatz Server is a HTTP client testing tool which allows for request/response expectations to be configured in a 
flexible manner. The expectations will respond in a configured manner to requests and allow testing with different 
responses and/or error conditions without having to write a lot of boiler-plate code.

> **Warning:** v3.x is **NOT** directly backwards compatible with the 2.x releases. Some lesser-used features have been 
> removed and the Groovy DSL has been extracted into its own extension library - see the 
> [What's New in 3.0](http://cjstehno.github.io/ersatz/docs/user_guide.html#_in_3.0) section of the User Guide for 
> details and a migration guide.

> **Warning:** v2.x has removed some unused features of the library and extracted the Groovy support into it's own 
> extension library. See the [What's New in 2.0](http://cjstehno.github.io/ersatz/docs/user_guide.html#_in_2.0) section 
> of the User Guide for details and a migration guide.

## Getting Started

Project artifacts are available via the Maven Central repository. Below are the dependency coordinates for Gradle and 
Maven, more details are provided in the 
[Getting Started](http://cjstehno.github.io/ersatz/docs/user_guide.html#_getting_started) section of the User Guide.

### Gradle

    testImplementation 'io.github.cjstehno.ersatz:ersatz:3.2.0'

    // or, for the Groovy DSL extensions
    testImplementation 'io.github.cjstehno.ersatz:ersatz-groovy:3.2.0'

### Maven

    <dependency>
        <groupId>io.github.cjstehno.ersatz</groupId>
        <artifactId>ersatz</artifactId>
        <version>3.2.0</version>
        <scope>test</scope>
    </dependency>
    
    <!-- or, for the Groovy DSL extensions -->
    <dependency>
        <groupId>io.github.cjstehno.ersatz</groupId>
        <artifactId>ersatz-groovy</artifactId>
        <version>3.2.0</version>
        <scope>test</scope>
    </dependency>

### Shadowed

There is a `safe` (shadowed) version of each library available, which is useful in cases where you already  have a version of Undertow in use (to avoid version collisions). See the [Shadow Jar](http://cjstehno.github.io/ersatz/docs/user_guide.html#_shadow_jar)
section of the User Guide for more information.

## Build Instructions

Ersatz is built using Gradle:

    ./gradlew clean build

Or, if the "shadow" artifact is desired:

    ./gradlew clean build shadowJar

If you are interested in building the website, with all documentation and reports, you can run the following:

    ./gradlew site

In order to build specific reports, run the appropriate one of the following:

    ./gradlew asciidoctor
    ./gradlew javadoc
    ./gradlew jacocoTestReport
    ./gradlew test
    
## Publishing

### To Local Maven Repo

You can publish the all the source, javadoc, "safe" and regular jars to your local maven repository (`~/.m2/repository` directory) using the following command:

    ./gradlew publishToMavenLocal -x signErsatzGroovyPublication signErsatzPublication
    
    // or, if you have not built recently
    ./gradlew clean build publishToMavenLocal -x signErsatzGroovyPublication signErsatzPublication

The `-x sign` skips the signing step, which requires signing information. See the section on "signing" below if you need 
to have the locally published artifacts signed.

### To Maven Central

Before you publish a release, be sure to generate a release build (see signing section for details):

    ./gradlew clean build shadowJar signErsatzGroovyPublication signErsatzPublication -Psigning.gnupg.keyName=<key-id> -Psigning.gnupg.passphrase=<key-pass>

Then, to publish the artifacts to the Maven Central Repository, run

    ./gradlew publish -PossrhUser=<jira-user> -PossrhPass=<jira-pass> -Psigning.gnupg.keyName=<key-id> -Psigning.gnupg.passphrase=<key-pass>

The additional "signing." properties are required to sign the artifacts, see the Signing section below for more details.

Once the artifacts have been published, sign-in to https://s01.oss.sonatype.org and navigate to the "Staging Repositories" 
and "Close" the published artifacts - this may take some time to appear. If there are errors, you can "Drop" it, fix them and publish again.

After you have successfully "closed" the staging repository, you can release it by pressing the "Release" button.

#### Signing

When publishing the artifacts to the Maven Central Repository, they need to be signed. In order to keep the signing 
information secret, the properties are added only when the publishing task is executed, on the command line.

When you want to sign the published artifacts, add the following parameters to the command line:

    -Psigning.gnupg.keyName=<last-8-of-key> -Psigning.gnupg.passphrase=<key-password>

where `<last-8-of-key>` is the last 8 characters of the key, and `<key-password>` is the password for the key.

You can list the available keys using:

    gpg -k

## Documentation Site

### Building

You can build the documentation website using the following:

    ./gradlew site

Which will build all Javadocs, User Guide, build reports and the website itself.

### Publishing

Publishing the website is a bit of an odd process:

1. Create a separate clone of the ersatz project repo with a different name (e.g. `ersatz-site`).
2. In the `ersatz-site` repo checkout the `gh-pages` branch - you should see only website content in that repo now.
3. Publish the updated site content **from the main project** by running: `rsync -r build/site/* ../ersatz-site/`
4. In the `ersatz-site` project add, commit and push the changes into the `gh-pages` branch.

At this point the website will be published but it may take some time for GitHub to reload the changes.

## License

```
Copyright (C) 2023 Christopher J. Stehno

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
