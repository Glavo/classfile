package org.glavo.classfile.jdk;

/// Provides the byte-array operations used in place of newer JDK internals.
public final class ArrayUtils {
    /// Updates a hash with the unsigned values in a byte-array range.
    ///
    /// @param result the initial hash value
    /// @param array the source array
    /// @param fromIndex the first source index
    /// @param length the number of bytes
    /// @return the updated hash value
    public static int signedHashCode(int result, byte[] array, int fromIndex, int length) {
        int end = fromIndex + length;
        for (int i = fromIndex; i < end; i++) {
            result = 31 * result + (array[i] & 0xff);
        }
        return result;
    }

    /// Updates a hash with the unsigned values in a byte-array range.
    ///
    /// @param array the source array
    /// @param fromIndex the first source index
    /// @param length the number of bytes
    /// @param initialValue the initial hash value
    /// @return the updated hash value
    public static int hashCodeOfUnsigned(byte[] array, int fromIndex, int length, int initialValue) {
        return signedHashCode(initialValue, array, fromIndex, length);
    }

    /// Prevents instantiation.
    private ArrayUtils() {
    }
}
