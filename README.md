# Ersatz Server

> _noun_ An artificial substance or article used to replace something natural or genuine; a substitute.

A "mock" HTTP server library for testing HTTP/REST clients.

[![Build Status](https://travis-ci.org/cjstehno/ersatz.svg?branch=master)](https://travis-ci.org/cjstehno/ersatz)
[![Coverage Status](https://coveralls.io/repos/github/cjstehno/ersatz/badge.svg?branch=master)](https://coveralls.io/github/cjstehno/ersatz?branch=master)

## Features

* Uses embedded Undertow to setup an HTTP server for unit testing HTTP client code
* compatible with Java and Groovy (and JVM language)
* compatible with JUnit and Spock testing frameworks
* Supports chained builder, Java 8 functions, and a Groovy DSL

## Atrifacts

    compile 'com.stehno.ersatz:ersats:<CURRENT_VERSION>'

> WARNING: This is very much a work in progress and not yet ready for general use.
