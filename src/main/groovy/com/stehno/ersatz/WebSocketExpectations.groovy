package com.stehno.ersatz

interface WebSocketExpectations {

    ReceivedMessage receive(Object payload)

    ReceivedMessage receive(Object payload, WsMessageType messageType)

    ReceivedMessage receive(@DelegatesTo(ReceivedMessage) Closure closure)
}