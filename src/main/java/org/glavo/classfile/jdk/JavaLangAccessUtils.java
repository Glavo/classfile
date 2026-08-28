package org.glavo.classfile.jdk;

/// Provides string and byte operations used in place of newer JDK internals.
public final class JavaLangAccessUtils {
    /// Counts the leading nonnegative bytes in a range.
    ///
    /// @param array the source array
    /// @param offset the first source index
    /// @param length the number of bytes to inspect
    /// @return the number of leading nonnegative bytes
    public static int countPositives(byte[] array, int offset, int length) {
        int limit = offset + length;
        for (int i = offset; i < limit; i++) {
            if (array[i] < 0) {
                return i - offset;
            }
        }
        return length;
    }

    /// Inflates unsigned bytes into UTF-16 code units.
    ///
    /// @param source the source byte array
    /// @param sourceOffset the first source index
    /// @param destination the destination character array
    /// @param destinationOffset the first destination index
    /// @param length the number of elements to copy
    public static void inflateBytesToChars(byte[] source, int sourceOffset,
                                           char[] destination, int destinationOffset,
                                           int length) {
        for (int i = 0; i < length; i++) {
            destination[destinationOffset++] = (char) (source[sourceOffset++] & 0xff);
        }
    }

    /// Counts the leading nonzero ASCII characters in a string.
    ///
    /// @param value the string
    /// @return the number of leading characters in the range U+0001 through U+007F
    public static int countNonZeroAscii(String value) {
        int length = value.length();
        for (int i = 0; i < length; i++) {
            char ch = value.charAt(i);
            if (ch == 0 || ch >= 0x80) {
                return i;
            }
        }
        return length;
    }

    /// Prevents instantiation.
    private JavaLangAccessUtils() {
    }
}
