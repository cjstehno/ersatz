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
package com.stehno.ersatz.junit5;

import com.stehno.ersatz.ErsatzServer;
import com.stehno.ersatz.util.HttpClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;

import static com.stehno.ersatz.ContentType.TEXT_PLAIN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(ErsatzServerSupport.class)
class Junit5Test {

    private ErsatzServer server = new ErsatzServer();
    private final HttpClient http = new HttpClient();

    @Test @DisplayName("Testing JUnit 5 Support")
    void testing_junit5_support() throws IOException {
        server.expectations(expects -> {
            expects.get("/junit5").responds().code(200).body("Hi", TEXT_PLAIN);
        });

        assertThat(http.get(server.httpUrl("/junit5")).body().string(), equalTo("Hi"));
    }

    @Test @DisplayName("Another JUnit 5 Test")
    void another_junit5_test() throws IOException {
        server.expectations(expects -> {
            expects.get("/junit5").responds().code(200).body("Hello Again", TEXT_PLAIN);
        });

        assertThat(http.get(server.httpUrl("/junit5")).body().string(), equalTo("Hello Again"));
    }
}

