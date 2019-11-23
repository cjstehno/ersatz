package com.stehno.ersatz;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

import java.util.function.Consumer;

import static groovy.lang.Closure.DELEGATE_FIRST;

/**
 * Defines the web socket expectations.
 */
@SuppressWarnings("ConfusingMethodName")
public interface WebSocketExpectations {

    /**
     * Configures an expectation that the web socket connection will receive a message with the specified payload - the payload type
     * being determined by the object type.
     *
     * @param payload the payload object
     * @return a reference to this ReceivedMessage
     */
    ReceivedMessage receive(Object payload);

    /**
     * Configures an expectation that the web socket connection will receive a message with the specified payload, with the specified
     * payload type.
     *
     * @param payload     the payload object
     * @param messageType the message type
     * @return a reference to this ReceivedMessage
     */
    ReceivedMessage receive(Object payload, WsMessageType messageType);

    /**
     * Configures an expectation that the web socket connection will receive a message configured by the closure.
     *
     * @param closure the configuration closure
     * @return a reference to this ReceivedMessage
     */
    ReceivedMessage receive(@DelegatesTo(value = ReceivedMessage.class, strategy = DELEGATE_FIRST) Closure closure);

    /**
     * Configures an expectation that the web socket connection will receive a message configured by the consumer.
     *
     * @param config the configuration consumer
     * @return a reference to this ReceivedMessage
     */
    ReceivedMessage receive(Consumer<ReceivedMessage> config);

    /**
     * Configures a web socket message which will be sent to the client after it connects.
     *
     * @param payload the message payload
     * @return a reference to this ReceivedMessage
     */
    SentMessage send(Object payload);

    /**
     * Configures a web socket message which will be sent to the client after it connects.
     *
     * @param payload     the message payload
     * @param messageType the message type
     * @return a reference to this ReceivedMessage
     */
    SentMessage send(Object payload, WsMessageType messageType);

    /**
     * Configures a web socket message which will be sent to the client after it connects.
     *
     * @param closure the message configuration closure
     * @return a reference to this ReceivedMessage
     */
    SentMessage send(@DelegatesTo(value = SentMessage.class, strategy = DELEGATE_FIRST) Closure closure);

    /**
     * Configures a web socket message which will be sent to the client after it connects.
     *
     * @param config the configuration consumer
     * @return a reference to this ReceivedMessage
     */
    SentMessage send(Consumer<SentMessage> config);
}
