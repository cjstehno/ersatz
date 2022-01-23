package io.github.cjstehno.mtchr;

public class StringDescriber implements Describer {

    private final StringBuffer buffer = new StringBuffer();

    @Override public Describer append(final String string) {
        buffer.append(string);
        return this;
    }

    @Override public String describe() {
        return buffer.toString();
    }
}
