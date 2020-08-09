/**
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
package com.stehno.ersatz

import com.stehno.ersatz.cfg.Expectations
import com.stehno.ersatz.cfg.Request
import com.stehno.ersatz.cfg.Response
import com.stehno.ersatz.cfg.ServerConfig

// Note: This is experimental code for kotlin support - it may not look anything like this in the end


fun ersatzServer(conf: ServerConfig.() -> Unit) : ErsatzServer {
    return ErsatzServer(conf)
}

fun ServerConfig.expect(expects: Expectations.() -> Unit) {
    this.expectations(expects)
}

fun Request.respond(resp: Response.() -> Unit) {
    this.responder(resp)
}
