package com.programyourhome.immerse.toolbox.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;

/**
 * Util methods for Stream(Ex).
 */
public class StreamUtil {

    private StreamUtil() {
    }

    /**
     * Convert an Optional into a Stream.
     * If present, a stream with that one element. If not, an empty stream.
     */
    public static <T> StreamEx<T> optionalToStream(Optional<T> optional) {
        return optional.isPresent() ? StreamEx.of(optional.get()) : StreamEx.empty();
    }

    /**
     * Create an entry stream with the same object for key and value, for each item in the input.
     */
    public static <T> EntryStream<T, T> sameKeyAndValue(T[] input) {
        return sameKeyAndValue(Arrays.asList(input));
    }

    /**
     * Create an entry stream with the same object for key and value, for each item in the input.
     */
    public static <T> EntryStream<T, T> sameKeyAndValue(Collection<T> input) {
        return StreamEx.of(input).mapToEntry(obj -> obj, obj -> obj);
    }

    /**
     * Create an entry stream with keys from the input and the same value object for each of them.
     */
    public static <K, V> EntryStream<K, V> toMapFixedValue(Collection<K> input, V value) {
        return StreamEx.of(input).mapToEntry(obj -> value);
    }

}
