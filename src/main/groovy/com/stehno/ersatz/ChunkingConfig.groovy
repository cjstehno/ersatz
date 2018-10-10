package com.stehno.ersatz

/**
 * Configuration for a chunked response. The response content-type will be specified as "chunked" and the response itself
 * will be written as partial chunks using the <code>chunks</code> property to determine the number of chunks, with the
 * <code>delay</code> property used to determine the delay between each chunk - a range of values may be specified to cause
 * a random delay based on a value within the range (milliseconds).
 */
class ChunkingConfig {

    private int chunks = 2
    private IntRange range = 50..50

    /**
     * Used to specify the number of chunks which the response will be broken into,
     *
     * @param value the number of chunks
     * @return a reference to the config
     */
    ChunkingConfig chunks(final int value){
        chunks = value
        this
    }

    /**
     * Used to specify the delay time between each chunk, fixed to the specified milliseconds.
     *
     * @param value the delay value in milliseconds
     * @return a reference to the config
     */
    ChunkingConfig delay(final int value){
        range = value..value
        this
    }

    /**
     * Used to specify the delay time between each chunk as a range of millisecond values. The actual delay value will
     * be determined at runtime as a random value within the specified range.
     *
     * @param delayRange the delay value range in milliseconds
     * @return a reference to the config
     */
    ChunkingConfig delay(final IntRange delayRange){
        range = delayRange
        this
    }
}
