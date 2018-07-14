package com.stehno.ersatz

/**
 * FIXME: document
 */
interface ReceivedMessage {

    // generally a string or byte array
    ReceivedMessage payload(Object obj)

    ReceivedMessage messageType(WsMessageType type)

    MessageReaction reaction(Object payload)

    MessageReaction reaction(Object payload, WsMessageType messageType)

    MessageReaction reaction(@DelegatesTo(MessageReaction) Closure closure)
}

interface MessageReaction {

    MessageReaction payload(Object obj)

    MessageReaction messageType(WsMessageType type)
}

class MessageReactionImpl implements MessageReaction {

    Object payload
    WsMessageType messageType

    @Override
    MessageReaction payload(Object obj) {
        payload = obj
        this
    }

    @Override
    MessageReaction messageType(WsMessageType type) {
        messageType = type
        this
    }
}