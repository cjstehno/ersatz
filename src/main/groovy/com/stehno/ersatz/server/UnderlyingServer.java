package com.stehno.ersatz.server;

public interface UnderlyingServer {

    void start() throws Exception;

    int getHttpPort();

    int getHttpsPort();

    default String getHttpUrl(){
        return "http://localhost:" + getHttpPort();
    }

    default String getHttpsUrl(){
        return "https://localhost:" + getHttpsPort();
    }

    default String getWsUrl(){
        return "ws://localhost:" + getHttpPort();
    }

    default String getHttpUrl(final String path){
        return getHttpUrl() + path;
    }

    default String getHttpsUrl(final String path){
        return getHttpsUrl() + path;
    }

    void stop();
}
