package com.stehno.ersatz;

/**
 * Enumeration of the supported WebSocket message types.
 */
public enum WsMessageType {

    BINARY, TEXT;

    /**
     * Resolves the default message type for the specified object.
     *
     * @param obj the payload object
     * @return the message type determined by content
     */
    public static WsMessageType resolve(final Object obj) {
        return obj instanceof Byte[] ? BINARY : TEXT;
    }
}
