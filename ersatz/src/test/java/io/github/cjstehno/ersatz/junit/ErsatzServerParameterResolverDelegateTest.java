/**
 * Copyright (C) 2023 Christopher J. Stehno
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
package io.github.cjstehno.ersatz.junit;

import io.github.cjstehno.ersatz.ErsatzServer;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Parameter;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) @SuppressWarnings({"rawtypes", "unchecked"})
class ErsatzServerParameterResolverDelegateTest {

    // Testing is limited by the lack of access to the GroovyErsatzServer class.

    private ErsatzServerParameterResolverDelegate resolver;

    @BeforeEach void beforeEach() {
        resolver = new ErsatzServerParameterResolverDelegate("foo");
    }

    @ParameterizedTest @MethodSource("parameters")
    void supportedParameter(final Class type, final boolean expected) {
        val param = mock(Parameter.class);
        when(param.getType()).thenReturn(type);

        val pCtx = mock(ParameterContext.class);
        when(pCtx.getParameter()).thenReturn(param);

        assertEquals(expected, resolver.supportsParameter(pCtx, null));
    }

     @Test void resolvingParameter(){
        final Class type = ErsatzServer.class;

        val param = mock(Parameter.class);
        when(param.getType()).thenReturn(type);

        val pCtx = mock(ParameterContext.class);
        when(pCtx.getParameter()).thenReturn(param);

        val storedObj = new Object();

        val store = mock(ExtensionContext.Store.class);
        when(store.get("foo")).thenReturn(storedObj);

        val eCtx = mock(ExtensionContext.class);
        when(eCtx.getStore(Namespace.create("io.github.cjstehno", "ersatz"))).thenReturn(store);

         assertEquals(storedObj, resolver.resolveParameter(pCtx, eCtx));
    }

    private static Stream<Arguments> parameters() {
        return Stream.of(
            arguments(ErsatzServer.class, true),
            arguments(String.class, false)
        );
    }
}