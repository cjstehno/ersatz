package com.stehno.ersatz

import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KotlinUsageTest {

    @Test fun ersatzUsage() {
        val ersatz = ErsatzServer { config -> config.autoStart(true) }

        ersatz.expectations { expectations ->
            expectations.get("/kotlin").called(1).responder { response ->
                response.content("Hello Kotlin!", ContentType.TEXT_PLAIN).code(200)
            }
        }

        val http = OkHttpClient.Builder().build()
        val request: okhttp3.Request = okhttp3.Request.Builder().url("${ersatz.httpUrl}/kotlin").build()
        assertEquals("Hello Kotlin!", http.newCall(request).execute().body().string())

        assertTrue(ersatz.verify())
    }
}