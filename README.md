# Ersatz Server
            
[![Download](https://api.bintray.com/packages/cjstehno/stehno/ersatz/images/download.svg)](https://bintray.com/cjstehno/stehno/ersatz/_latestVersion) [![Build Status](https://travis-ci.org/cjstehno/ersatz.svg?branch=master)](https://travis-ci.org/cjstehno/ersatz) [![Coverage Status](https://coveralls.io/repos/github/cjstehno/ersatz/badge.svg?branch=master)](https://coveralls.io/github/cjstehno/ersatz?branch=master) [![Twitter Follow](https://img.shields.io/twitter/follow/ersatz.svg?style=social&label=Follow)]()

## Quick Links

* Site: http://stehno.com/ersatz
* Project: https://github.com/cjstehno/ersatz
* User Guide: http://stehno.com/ersatz/guide/
* Java Docs: http://stehno.com/ersatz/javadoc/
* Twitter: [@ErsatzServer](https://twitter.com/ersatzserver)

## Introduction

The Ersatz Server is a HTTP client testing tool which allows for request/response expectations to be configured in a 
flexible manner. The expectations will respond in a configured manner to requests and allow testing with different 
responses and/or error conditions without having to write a lot of boiler-plate code.

> **Warning:** v2.1 has removed some unused features of the library and extracted the Groovy support into it's own 
> extension library. See the [What's New in 2.1](http://stehno.com/ersatz/guide/#_whats_new_in_2.1) section of the User 
> Guide for details.

> **Warning:** v2.0 is **NOT** directly backwards compatible with the 1.x codebase. See the 
> [Migrating to 2.0](http://stehno.com/ersatz/guide/#_migrating_to_2.0) section of the User Guide for details.

## Build Instructions

Ersatz is built using Gradle:

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
