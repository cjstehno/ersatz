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
package com.stehno.ersatz

import com.stehno.ersatz.junit5.ErsatzServerSupport
import com.stehno.ersatz.util.HttpClient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

import static org.awaitility.Awaitility.await
import static org.hamcrest.CoreMatchers.equalTo

@ExtendWith(ErsatzServerSupport)
class MultiThreadedUsageTest {

    private final ErsatzServer server = new ErsatzServer({
        reportToConsole()
    })
    private HttpClient client = new HttpClient();

    @BeforeEach void beforeEach() {
        client = new HttpClient();
    }

    @Test void 'Multiple concurrent calls'() {
        int requestCount = 8

        server.expectations {
            get('/something') {
                called requestCount
                responder {
                    code 200
                }
            }
        }

        def responses = new AtomicReference<List<Integer>>(new LinkedList<Integer>())

        requestCount.times {
            client.getAsync(server.httpUrl('/something')).thenAccept { response ->
                responses.get().add(response.code())
            }
        }

        await().until {
            responses.get().size() == requestCount
        }

        assert server.verify()
        assert responses.get().size() == requestCount
        assert responses.get().every { it == 200 }
    }

    @Test void 'Multiple concurrent calls with listener'() {
        int requestCount = 8
        def counter = new AtomicInteger(0)

        server.expectations {
            get('/something') {
                called requestCount
                listener {
                    counter.incrementAndGet()
                }
                responder {
                    code 200
                }
            }
        }

        def responses = new AtomicReference<List<Integer>>(new LinkedList<Integer>())

        requestCount.times {
            client.getAsync(server.httpUrl('/something')).thenAccept { response ->
                responses.get().add(response.code())
            }
        }

        await().untilAtomic(counter, equalTo(requestCount))

        assert server.verify()
        assert responses.get().size() == requestCount
        assert responses.get().every { it == 200 }
    }
}
