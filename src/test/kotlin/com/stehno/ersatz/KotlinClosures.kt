package com.stehno.ersatz

import groovy.lang.Closure

// NOTE: This code was extracted from https://github.com/gradle/kotlin-dsl/blob/master/provider/src/main/kotlin/org/gradle/kotlin/dsl/GroovyInteroperability.kt
// This seems like something useful in an external library, but perhaps Groovy-Kotlin interop is a bit of an edge case :-)
// In the future I am planning on providing a deeper Kotlin integration and this may be the basis of it

/**
 * Adapts a Kotlin function to a single argument Groovy [Closure].
 *
 * @param T the expected type of the single argument to the closure.
 * @param action the function to be adapted.
 *
 * @see [KotlinClosure1]
 */
fun <T : Any> Any.closureOf(action: T.() -> Unit): Closure<Any?> =
    KotlinClosure1(action, this, this)

/**
 * Adapts a Kotlin function to a Groovy [Closure] that operates on the
 * configured Closure delegate.
 *
 * @param T the expected type of the delegate argument to the closure.
 * @param action the function to be adapted.
 *
 * @see [KotlinClosure1]
 */
@Suppress("UNCHECKED_CAST")
fun <T> Any.delegateClosureOf(action: T.() -> Unit) =
    object : Closure<Unit>(this, this) {
        @Suppress("unused") // to be called dynamically by Groovy
        fun doCall() = (delegate as T).action()
    }


/**
 * Adapts a parameterless Kotlin function to a parameterless Groovy [Closure].
 *
 * @param V the return type.
 * @param function the function to be adapted.
 * @param owner optional owner of the Closure.
 * @param thisObject optional _this Object_ of the Closure.
 *
 * @see [Closure]
 */
open class KotlinClosure0<V : Any>(
    val function: () -> V?,
    owner: Any? = null,
    thisObject: Any? = null) : groovy.lang.Closure<V?>(owner, thisObject) {

    @Suppress("unused") // to be called dynamically by Groovy
    fun doCall(): V? = function()
}


/**
 * Adapts an unary Kotlin function to an unary Groovy [Closure].
 *
 * @param T the type of the single argument to the closure.
 * @param V the return type.
 * @param function the function to be adapted.
 * @param owner optional owner of the Closure.
 * @param thisObject optional _this Object_ of the Closure.
 *
 * @see [Closure]
 */
class KotlinClosure1<in T : Any, V : Any>(
    val function: T.() -> V?,
    owner: Any? = null,
    thisObject: Any? = null) : Closure<V?>(owner, thisObject) {

    @Suppress("unused") // to be called dynamically by Groovy
    fun doCall(it: T): V? = it.function()
}


/**
 * Adapts a binary Kotlin function to a binary Groovy [Closure].
 *
 * @param T the type of the first argument.
 * @param U the type of the second argument.
 * @param V the return type.
 * @param function the function to be adapted.
 * @param owner optional owner of the Closure.
 * @param thisObject optional _this Object_ of the Closure.
 *
 * @see [Closure]
 */
class KotlinClosure2<in T : Any, in U : Any, V : Any>(
    val function: (T, U) -> V?,
    owner: Any? = null,
    thisObject: Any? = null) : Closure<V?>(owner, thisObject) {

    @Suppress("unused") // to be called dynamically by Groovy
    fun doCall(t: T, u: U): V? = function(t, u)
}


operator fun <T> Closure<T>.invoke(): T = call()

operator fun <T> Closure<T>.invoke(x: Any?): T = call(x)

operator fun <T> Closure<T>.invoke(vararg xs: Any?): T = call(*xs)

