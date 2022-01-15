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


import org.junit.jupiter.api.Test

import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer

import static org.junit.jupiter.api.Assertions.assertEquals

/**
 * Tests for ConsumerWithDelegate
 */
class ConsumerWithDelegateTest {

    private static final String SOMETHING = 'smtg'

    @Test void useAsConsumer() {
        Consumer<ConsumerFoo> consumer = ConsumerWithDelegate.create {
            foo = 'FOO'
        }
        assertEquals 'FOO', AcceptsConsumer.testMe(consumer).foo
    }

    @Test void ownerIsSetFromPropagator() {
        Object o = null

        ConsumerWithDelegate.create {
            ConsumerWithDelegate.create {
                o = foo
            }.accept(it)
        }.accept(new ConsumerFoo())

        assertEquals 'foo', o
    }

    @Test void ownerIsSet() {
        AtomicReference<String> reference = new AtomicReference<>()

        ConsumerWithDelegate.create({
            reference.set(foo)
        }, new FunctionFoo()).accept(SOMETHING)

        assertEquals 'foo', reference.get()
    }
}

class FunctionFoo {
    String foo = 'foo'
    String bar = 'bar'
}

class ConsumerFoo {
    String foo = 'foo'
    String bar = 'bar'
}

class AcceptsConsumer {

    static ConsumerFoo testMe(Consumer<ConsumerFoo> consumer) {
        ConsumerFoo foo = new ConsumerFoo()
        consumer.accept(foo)
        foo
    }

}