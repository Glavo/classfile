package org.glavo.classfile.jdk;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class CollectionUtils {
    @SuppressWarnings("unchecked")
    public static <T> List<T> listFromTrustedArrayNullsAllowed(Object[] arr) {
        //noinspection Java9CollectionFactory
        return arr.length == 0 ? List.of() : (List<T>) Collections.unmodifiableList(Arrays.asList(arr));
    }
}
