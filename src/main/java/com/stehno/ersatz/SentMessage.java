package com.stehno.ersatz;

/**
 * Defines a message sent by the web socket server.
 */
@SuppressWarnings("ConfusingMethodName")
public interface SentMessage {

    /**
     * Defines the message payload.
     *
     * @param obj the payload object
     * @return a reference to this SentMessage
     */
    SentMessage payload(Object obj);

    /**
     * Defines the message type.
     *
     * @param type the payload type
     * @return a reference to this SentMessage
     */
    SentMessage messageType(WsMessageType type);
}
