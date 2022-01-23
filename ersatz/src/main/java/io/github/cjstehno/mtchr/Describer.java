package io.github.cjstehno.mtchr;

public interface Describer {

    Describer append(final String string);

    String describe();
}
