package com.stehno.ersatz;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

import java.util.function.Consumer;

import static groovy.lang.Closure.DELEGATE_FIRST;

/**
 * Represents the expectation of a web service message sent by the client.
 */
@SuppressWarnings("ConfusingMethodName")
public interface ReceivedMessage {

    /**
     * The message payload, which will be resolved as either TEXT or BINARY type based on the object type.
     *
     * @param obj the payload object
     * @return a reference to this ReceivedMessage
     */
    ReceivedMessage payload(Object obj);

    /**
     * The message payload type.
     *
     * @param type the payload type
     * @return a reference to this ReceivedMessage
     */
    ReceivedMessage messageType(WsMessageType type);

    /**
     * Used to specify a reaction message to be sent after receiving this message. The message type will be determined
     * by the payload type.
     *
     * @param payload the payload object
     * @return a reference to this ReceivedMessage
     */
    MessageReaction reaction(Object payload);

    /**
     * Used to specify a reaction message to be sent after receiving this message.
     *
     * @param payload     the payload object
     * @param messageType the message type
     * @return a reference to this ReceivedMessage
     */
    MessageReaction reaction(Object payload, WsMessageType messageType);

    /**
     * Used to specify a reaction message to be sent after receiving this message.
     *
     * @param closure the reaction configuration closure
     * @return a reference to this ReceivedMessage
     */
    MessageReaction reaction(@DelegatesTo(value = MessageReaction.class, strategy = DELEGATE_FIRST) Closure closure);

    /**
     * Used to specify a reaction message to be sent after receiving this message.
     *
     * @param config the reaction configuration consumer
     * @return a reference to this ReceivedMessage
     */
    MessageReaction reaction(Consumer<MessageReaction> config);
}
