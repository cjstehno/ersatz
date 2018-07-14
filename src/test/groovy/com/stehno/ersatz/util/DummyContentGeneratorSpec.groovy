package com.stehno.ersatz.util

import spock.lang.Specification

import static com.stehno.ersatz.util.DummyContentGenerator.generate
import static com.stehno.ersatz.util.StorageUnit.*

class DummyContentGeneratorSpec extends Specification {

    def 'generate'() {
        expect:
        generate(10.5d, unit).length == result

        where:
        unit      || result
        BYTES     || 11
        KILOBYTES || 10752
        MEGABYTES || 11010048
    }
}
