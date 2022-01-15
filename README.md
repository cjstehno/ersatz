# Ersatz Server
            
[![Download](https://api.bintray.com/packages/cjstehno/stehno/ersatz/images/download.svg)](https://bintray.com/cjstehno/stehno/ersatz/_latestVersion) [![Build Status](https://travis-ci.org/cjstehno/ersatz.svg?branch=master)](https://travis-ci.org/cjstehno/ersatz) [![Coverage Status](https://coveralls.io/repos/github/cjstehno/ersatz/badge.svg?branch=master)](https://coveralls.io/github/cjstehno/ersatz?branch=master) [![Twitter Follow](https://img.shields.io/twitter/follow/ersatz.svg?style=social&label=Follow)]()

## Quick Links

* Site: https://cjstehno.github.io/ersatz
* Project: https://github.com/cjstehno/ersatz
* User Guide: https://cjstehno.github.io/ersatz/guide/
* Java Docs: https://cjstehno.github.io/ersatz/javadoc/
* Twitter: [@ErsatzServer](https://twitter.com/ersatzserver)

## Introduction

The Ersatz Server is a HTTP client testing tool which allows for request/response expectations to be configured in a 
flexible manner. The expectations will respond in a configured manner to requests and allow testing with different 
responses and/or error conditions without having to write a lot of boiler-plate code.

> **Warning:** v2.0 is **NOT** directly backwards compatible with the 1.x codebase. See the 
> [Migrating to 2.0](https://cjstehno.github.io/ersatz/guide/#_migrating_to_2.0) section of the User Guide for details.

## Artifacts

Project artifacts are available via the JCenter (Bintray) and Maven Central repositories.

For Gradle:

    testCompile 'com.stehno.ersatz:ersatz:2.0.0'

For Maven:

    <dependency>
        <groupId>com.stehno.ersatz</groupId>
        <artifactId>ersatz</artifactId>
        <version>2.0.0</version>
        <scope>test</scope>
    </dependency>
    
Alternately, there is a `safe` (shadowed) version of the library available, which is useful in cases where you already 
have a version of Undertow in use (to avoid version collisions). See the [Shadow Jar](https://cjstehno.github.io/ersatz/guide/#_shadow_jar) 
section of the User Guide for more information.

## Build Instructions

Ersatz is built using Gradle with no custom build tasks:

    ./gradlew clean build
    
## License

```
Copyright (C) 2020 Christopher J. Stehno

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
