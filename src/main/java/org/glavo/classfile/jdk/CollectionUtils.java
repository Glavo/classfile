package org.glavo.classfile.jdk;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public final class CollectionUtils {

    @SuppressWarnings("unchecked")
    public static <T> List<T> listFromTrustedArray(Object[] arr) {
        return (List<T>) List.of(arr);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> listFromTrustedArrayNullsAllowed(Object[] arr) {
        //noinspection Java9CollectionFactory
        return arr.length == 0
                ? Collections.emptyList()
                : (List<T>) Collections.unmodifiableList(Arrays.asList(arr));
    }

    private static int calculateHashMapCapacity(int numMappings) {
        return (int) Math.ceil(numMappings / 0.75);
    }

    public static <K, V> HashMap<K, V> newHashMap(int numMappings) {
        return new HashMap<>(calculateHashMapCapacity(numMappings));
    }
}
