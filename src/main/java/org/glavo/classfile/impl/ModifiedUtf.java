/*
 * Copyright (c) 2025, Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2024, Alibaba Group Holding Limited. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package org.glavo.classfile.impl;

/// Provides modified UTF-8 length calculation and encoding utilities.
final class ModifiedUtf {
    /// The maximum encoded byte length of a constant-pool UTF-8 entry.
    static final int CONSTANT_POOL_UTF8_MAX_BYTES = 65535;

    /// Prevents instantiation.
    private ModifiedUtf() {
    }

    /// Writes one UTF-16 code unit to a pre-sized modified UTF-8 buffer.
    ///
    /// @param buffer the destination buffer
    /// @param offset the first destination offset
    /// @param value the code unit to encode
    /// @return the offset immediately following the encoded value
    static int putChar(byte[] buffer, int offset, char value) {
        if (value != 0 && value < 0x80) {
            buffer[offset++] = (byte) value;
        } else if (value >= 0x800) {
            buffer[offset] = (byte) (0xE0 | value >> 12 & 0x0F);
            buffer[offset + 1] = (byte) (0x80 | value >> 6 & 0x3F);
            buffer[offset + 2] = (byte) (0x80 | value & 0x3F);
            offset += 3;
        } else {
            buffer[offset] = (byte) (0xC0 | value >> 6 & 0x1F);
            buffer[offset + 1] = (byte) (0x80 | value & 0x3F);
            offset += 2;
        }
        return offset;
    }

    /// Returns the modified UTF-8 length of the string.
    ///
    /// @param value the input string
    /// @return the encoded byte length
    static long utfLen(String value) {
        return utfLen(value, 0);
    }

    /// Returns the modified UTF-8 length after skipping a known single-byte prefix.
    ///
    /// @param value the input string
    /// @param countNonZeroAscii the number of initial nonzero ASCII characters
    /// @return the encoded byte length
    static long utfLen(String value, int countNonZeroAscii) {
        long length = value.length();
        for (int i = (int) length - 1; i >= countNonZeroAscii; i--) {
            int ch = value.charAt(i);
            if (ch >= 0x80 || ch == 0) {
                length += ch >= 0x800 ? 2L : 1L;
            }
        }
        return length;
    }

    /// Returns whether the string fits in a constant-pool UTF-8 entry.
    ///
    /// @param value the input string
    /// @return whether its encoded length is at most 65535 bytes
    static boolean isValidLengthInConstantPool(String value) {
        int length = value.length();
        if (length <= CONSTANT_POOL_UTF8_MAX_BYTES / 3) {
            return true;
        }
        return length <= CONSTANT_POOL_UTF8_MAX_BYTES
                && utfLen(value) <= CONSTANT_POOL_UTF8_MAX_BYTES;
    }
}
