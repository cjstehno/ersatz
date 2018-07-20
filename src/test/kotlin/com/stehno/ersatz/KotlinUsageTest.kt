/**
 * Copyright (C) 2018 Christopher J. Stehno
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

import com.stehno.ersatz.ContentType.TEXT_PLAIN
import com.stehno.ersatz.KotlinDsl.kotlinConfig
import com.stehno.ersatz.KotlinDsl.kotlinExpectations
import com.stehno.ersatz.KotlinDsl.kotlinResponse
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KotlinUsageTest {

    @Test
    fun ersatzUsage() {
        val ersatz = ErsatzServer { config -> config.autoStart(true) }

        ersatz.expectations { expectations ->
            expectations.get("/kotlin").called(1).responder { response ->
                response.content("Hello Kotlin!", TEXT_PLAIN).code(200)
            }
        }

        val http = OkHttpClient.Builder().build()
        val request: okhttp3.Request = okhttp3.Request.Builder().url("${ersatz.httpUrl}/kotlin").build()
        assertEquals("Hello Kotlin!", http.newCall(request).execute().body()?.string())

        assertTrue(ersatz.verify())
    }

    @Test
    fun ersatzUsageWithClosure() {
        val ersatz = ErsatzServer(kotlinConfig {
            expectations(kotlinExpectations {
                get("/kotlin").called(1).responder(kotlinResponse {
                    content("Hello Kotlin!", TEXT_PLAIN)
                    code(200)
                })
            })
        })

        val http = OkHttpClient.Builder().build()
        val request: okhttp3.Request = okhttp3.Request.Builder().url("${ersatz.httpUrl}/kotlin").build()
        assertEquals("Hello Kotlin!", http.newCall(request).execute().body()?.string())

        assertTrue(ersatz.verify())
    }
}

