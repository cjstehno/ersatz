/*
 * Copyright (C) 2019 Christopher J. Stehno
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

import spock.lang.Specification
import spock.lang.Unroll

/**
 * Specification for GroovyClosure helper.
 */
@Unroll class GroovyClosureSpec extends Specification {

    void "test delegate #method"() {
        Object token = new Object()
        Closure tester = { }

        when:
        "$method"(tester, token)

        then:
        tester.resolveStrategy == Closure.DELEGATE_FIRST
        tester.delegate == token

        where:
        method << testMethods
    }

    @SuppressWarnings(['Indentation', 'Instanceof'])
    void 'clone with top level owner - manual delegate'() {
        when:
        Level1 level1 = new Level1()
        level1.level2ManualDelegate {
            level3 { 'hello' }
        }
        then:
        level1.level2.closure.owner instanceof GroovyClosureSpec
    }

    @SuppressWarnings(['Indentation', 'Instanceof'])
    void 'clone with top level owner - method with delegate'() {
        when:
        Level1 level1 = new Level1()
        level1.level2MehodWithDelegate {
            level3 { 'hello' }
        }
        then:
        level1.level2.closure.owner instanceof GroovyClosureSpec
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

    private static List<String> getTestMethods() {
        List<String> ret = ['setDelegateInterfaceTest', 'setClosureTest']
        if (!(GroovySystem.version ==~ /2\.[01]\..*/)) {
            ret << 'setDelegateSAMTest'
        }
        ret
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