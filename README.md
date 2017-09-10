# Ersatz Server
            
[![Download](https://api.bintray.com/packages/cjstehno/stehno/ersatz/images/download.svg)](https://bintray.com/cjstehno/stehno/ersatz/_latestVersion) [![Build Status](https://travis-ci.org/cjstehno/ersatz.svg?branch=master)](https://travis-ci.org/cjstehno/ersatz) [![Coverage Status](https://coveralls.io/repos/github/cjstehno/ersatz/badge.svg?branch=master)](https://coveralls.io/github/cjstehno/ersatz?branch=master) [![Twitter Follow](https://img.shields.io/twitter/follow/ersatz.svg?style=social&label=Follow)]()

## Quick Links

* Site: http://stehno.com/ersatz
* Project: https://github.com/cjstehno/ersatz
* User Guide: http://stehno.com/ersatz/asciidoc/html5/
* Groovy Docs: http://stehno.com/ersatz/groovydoc/
* Twitter: [@ErsatzServer](https://twitter.com/ersatzserver)

## Introduction

The Ersatz Server is a HTTP client testing tool, which allows for request/response expectations to be configured in a flexible manner. The expectations
will respond in a configured manner to requests and allow testing with different responses and/or error conditions without having to write a lot of
boiler-plate code.

## Artifacts

Project artifacts are available via the JCenter (Bintray) and Maven Central repositories.

For Gradle:

    testCompile 'com.stehno.ersatz:ersatz:1.5.0'

For Maven:

    <dependency>
        <groupId>com.stehno.ersatz</groupId>
        <artifactId>ersatz</artifactId>
        <version>1.5.0</version>
        <scope>test</scope>
    </dependency>

## Build Instructions

Ersatz is built using Gradle with no custom build tasks:

    ./gradlew clean build


## License

```
Copyright (C) 2017 Christopher J. Stehno

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
