package com.stehno.ersatz.server;

public interface UnderlyingProxyServer {

    void start();

    void stop();

    String getUrl();

    int getActualPort();
}
