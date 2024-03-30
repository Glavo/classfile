package org.glavo.classfile.jdk;

public final class ArrayUtils {
    public static int signedHashCode(int result, byte[] a, int fromIndex, int length) {
        int end = fromIndex + length;
        for (int i = fromIndex; i < end; i++) {
            result = 31 * result + (a[i] & 0xff);
        }
        return result;
    }

    private ArrayUtils() {
    }
}
