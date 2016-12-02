package com.stehno.ersatz;

import java.util.function.Function;

/**
 * Created by cjstehno on 12/2/16.
 */
public class Verifiers {

    public static Function<Integer, Boolean> any() {
        return count -> true;
    }

    public static Function<Integer, Boolean> atLeast(final int min) {
        return count -> count >= min;
    }

    public static Function<Integer, Boolean> atMost(final int max) {
        return count -> count <= max;
    }

    public static Function<Integer, Boolean> exactly(final int n) {
        return count -> count == n;
    }

    public static Function<Integer, Boolean> once() {
        return exactly(1);
    }

    public static Function<Integer, Boolean> never() {
        return count -> count == 0;
    }
}
