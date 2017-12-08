package com.stehno.ersatz

import groovy.lang.Closure

// Note: This is experimental code for kotlin support - it may not look anything like this in the end

object KotlinDsl {

    fun kotlinConfig(conf: ServerConfig.() -> Unit): Closure<Unit> = delegateClosureOf(conf)

    fun kotlinExpectations(expects: Expectations.() -> Unit): Closure<Unit> = delegateClosureOf(expects)

    fun kotlinResponse(resp: Response.() -> Unit): Closure<Unit> = delegateClosureOf(resp)
}
