/*
 * Copyright (C) 2020 Christopher J. Stehno
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package space.jasan.support.groovy.closure

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

import java.util.stream.Stream

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue
import static org.junit.jupiter.params.provider.Arguments.arguments

/**
 * Specification for GroovyClosure helper.
 */
class GroovyClosureTest {

    @ParameterizedTest @DisplayName('test delegate method') @MethodSource('methodProvider')
    void delegateMethod(final String method) {
        Object token = new Object()
        Closure tester = {}

        "$method"(tester, token)

        assertEquals Closure.DELEGATE_FIRST, tester.resolveStrategy
        assertEquals token, tester.delegate
    }

    private static Stream<Arguments> methodProvider() {
        List<Arguments> ret = [
            arguments('setDelegateInterfaceTest'),
            arguments('setClosureTest')
        ]

        if (!(GroovySystem.version ==~ /2\.[01]\..*/)) {
            ret << arguments('setDelegateSAMTest')
        }

        ret.stream()
    }

    @Test @DisplayName('clone with top level owner - manual delegate')
    void cloneWithTopLevelOwner() {
        Level1 level1 = new Level1()
        level1.level2ManualDelegate {
            level3 { 'hello' }
        }

        assertTrue level1.level2.closure.owner instanceof GroovyClosureTest
    }

    @Test @DisplayName('clone with top level owner - method with delegate')
    void cloneWithDelegate(){
        Level1 level1 = new Level1()
        level1.level2MehodWithDelegate {
            level3 { 'hello' }
        }

        assertTrue level1.level2.closure.owner instanceof GroovyClosureTest
    }

    static void setDelegateInterfaceTest(Runnable consumer, Object token) {
        GroovyClosure.setDelegate(consumer, token)
    }

    static void setDelegateSAMTest(SAM consumer, Object token) {
        GroovyClosure.setDelegate(consumer, token)
    }

    static void setClosureTest(Closure consumer, Object token) {
        GroovyClosure.setDelegate(consumer, token)
    }
}

abstract class SAM {
    abstract void testIt()
}

class Level1 {

    Level2 level2 = new Level2()

    void level2ManualDelegate(Closure closure) {
        Closure c = GroovyClosure.cloneWithTopLevelOwner closure
        c.delegate = level2
        c.call(level2)
    }

    void level2MehodWithDelegate(Closure closure) {
        Closure c = GroovyClosure.cloneWithTopLevelOwner closure, level2
        c.call(level2)
    }

}

class Level2 {

    Closure closure

    void level3(Closure closure) {
        this.closure = GroovyClosure.cloneWithTopLevelOwner closure
    }

}