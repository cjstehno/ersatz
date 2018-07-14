package com.stehno.ersatz

interface ReceivedMessage {

    ReceivedMessage payload(Object obj)

    ReceivedMessage messageType(WsMessageType type)

//    ReceivedMessage count(int n)
    
//    MessageReactions reaction(alias to send)
//    MessageReactions reactions(alias to send)

    // FIXME: decoder?
}