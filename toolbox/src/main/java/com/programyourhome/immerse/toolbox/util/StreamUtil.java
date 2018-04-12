package com.programyourhome.immerse.toolbox.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;

public class StreamUtil {

    private StreamUtil() {
    }

    public static <T> StreamEx<T> optionalToStream(Optional<T> optional) {
        return optional.isPresent() ? StreamEx.of(optional.get()) : StreamEx.empty();
    }

    public static <T> EntryStream<T, T> sameKeyAndValue(T[] input) {
        return sameKeyAndValue(Arrays.asList(input));
    }

    public static <T> EntryStream<T, T> sameKeyAndValue(Collection<T> input) {
        return StreamEx.of(input).mapToEntry(obj -> obj, obj -> obj);
    }

    public static <K, V> EntryStream<K, V> toMapFixedValue(Collection<K> input, V value) {
        return StreamEx.of(input).mapToEntry(obj -> value);
    }

}
