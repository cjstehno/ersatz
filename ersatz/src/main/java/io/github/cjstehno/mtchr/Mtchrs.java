package io.github.cjstehno.mtchr;

import java.util.Arrays;

public interface Mtchrs {
    // FIXME: this is just a prototype
    // FIXME: create adapter method for Hamcrest
    // FIXME: OR matcher

    static Mtchr startsWith(final String prefix) {
        return new Mtchr() {
            @Override public boolean matches(final Object object) {
                return object.toString().startsWith(prefix);
            }

            @Override public void describe(final Describer describer) {
                describer.append("a string starting with \"" + prefix + "\"");
            }
        };
    }

    static Mtchr not(final Mtchr mtchr){
        return new Mtchr() {
            @Override public boolean matches(Object object) {
                return !mtchr.matches(object);
            }

            @Override public void describe(Describer describer) {
                describer.append("not ");
                mtchr.describe(describer);
            }
        };
    }

    static Mtchr endsWith(final String suffix) {
        return new Mtchr() {
            @Override public boolean matches(final Object object) {
                return object.toString().endsWith(suffix);
            }

            @Override public void describe(final Describer describer) {
                describer.append("a string ending with \"" + suffix + "\"");
            }
        };
    }

    static Mtchr and(final Mtchr... mtchrs) {
        return new Mtchr() {
            @Override public boolean matches(final Object object) {
                return Arrays.stream(mtchrs).allMatch(p -> p.matches(object));
            }

            @Override public void describe(final Describer describer) {
                for (int m = 0; m < mtchrs.length - 1; m++) {
                    mtchrs[m].describe(describer);
                    describer.append(" and ");
                }
                mtchrs[mtchrs.length - 1].describe(describer);
            }
        };
    }
}
