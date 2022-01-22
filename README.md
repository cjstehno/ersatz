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
* Twitter: [@ErsatzServer](https://twitter.com/ersatzserver)

## Introduction

The Ersatz Server is a HTTP client testing tool which allows for request/response expectations to be configured in a 
flexible manner. The expectations will respond in a configured manner to requests and allow testing with different 
responses and/or error conditions without having to write a lot of boiler-plate code.

> **Warning:** v3.x is **NOT** directly backwards compatible with the 2.x releases. Some lesser-used features have been 
> removed and the Groovy DSL has been extracted into its own extension library - see the 
> [What's New in 3.0](http://cjstehno.github.io/ersatz/docs/user_guide.html/#_in_3.0) section of the User Guide for 
> details and a migration guide.

> **Warning:** v2.x has removed some unused features of the library and extracted the Groovy support into it's own 
> extension library. See the [What's New in 2.0](http://cjstehno.github.io/ersatz/docs/user_guide.html/#_in_2.0) section 
> of the User Guide for details and a migration guide.

## Build Instructions

Ersatz is built using Gradle:

    ./gradlew clean build

Or, if the "shadow" artifact is desired:

    ./gradlew clean build shadowJar

If you are interested in building the web site, with all documentation and reports, you can run the following:

    ./gradlew site

In order to build specific reports, run the appropriate one of the following:

    ./gradlew asciidoctor
    ./gradlew javadoc
    ./gradlew jacocoTestReport
    ./gradlew test
    
## License

```
Copyright (C) 2022 Christopher J. Stehno

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
