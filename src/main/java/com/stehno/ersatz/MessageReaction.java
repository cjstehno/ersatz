package com.stehno.ersatz;

/**
 * Describes a web socket reaction message - a message sent as an asynchronous "response" to a connection or another message.
 */
@SuppressWarnings("ConfusingMethodName")
public interface MessageReaction {

    /**
     * Defines the message payload.
     *
     * @param obj the payload object
     * @return a reference to this MessageReaction
     */
    MessageReaction payload(Object obj);

    /**
     * Defines the message type.
     *
     * @param type the payload type
     * @return a reference to this MessageReaction
     */
    MessageReaction messageType(WsMessageType type);
}
