package io.github.cjstehno.mtchr;

public interface Mtchr {

    boolean matches(final Object object);

    void describe(final Describer describer);
}
