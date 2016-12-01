package com.stehno.ersatz;

import java.util.function.Consumer;

/**
 * Created by cjstehno on 12/1/16.
 */
public class ErsatzServer {

    private final ServerExpectations expectations = new ServerExpectations();

    void requesting(final Consumer<ServerExpectations> expects){
        expects.accept(expectations);
    }

    public ServerExpectations getExpectations() {
        return expectations;
    }
}
