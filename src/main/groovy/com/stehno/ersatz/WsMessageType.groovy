package com.stehno.ersatz

import groovy.transform.CompileStatic

/**
 * Enumeration of the supported WebSocket message types.
 */
@CompileStatic
enum WsMessageType {
    BINARY, TEXT
}