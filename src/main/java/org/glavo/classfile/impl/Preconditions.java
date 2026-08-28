/*
 * Copyright (c) 2016, 2022, Oracle and/or its affiliates. All rights reserved.
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

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/// Provides bounds checks with caller-selected exception types.
final class Preconditions {
    /// Formats failed array bounds checks as [ArrayIndexOutOfBoundsException].
    static final BiFunction<String, List<Number>, ArrayIndexOutOfBoundsException> AIOOBE_FORMATTER =
            outOfBoundsExceptionFormatter(ArrayIndexOutOfBoundsException::new);

    /// Prevents instantiation.
    private Preconditions() {
    }

    /// Creates an exception formatter for bounds checks.
    ///
    /// @param factory the exception factory
    /// @param <X> the exception type
    /// @return the formatter
    static <X extends RuntimeException> BiFunction<String, List<Number>, X> outOfBoundsExceptionFormatter(
            Function<String, X> factory) {
        return (checkKind, arguments) -> factory.apply(outOfBoundsMessage(checkKind, arguments));
    }

    /// Checks that an index is in the half-open range `[0, length)`.
    ///
    /// @param index the index
    /// @param length the range length
    /// @param formatter the exception formatter
    /// @param <X> the exception type
    /// @return `index`
    static <X extends RuntimeException> int checkIndex(
            int index, int length, BiFunction<String, List<Number>, X> formatter) {
        if (index < 0 || index >= length) {
            throw formatter.apply("checkIndex", List.of(index, length));
        }
        return index;
    }

    /// Checks that a subrange is within `[0, length)`.
    ///
    /// @param fromIndex the first index
    /// @param size the subrange size
    /// @param length the enclosing range length
    /// @param formatter the exception formatter
    /// @param <X> the exception type
    /// @return `fromIndex`
    static <X extends RuntimeException> int checkFromIndexSize(
            int fromIndex, int size, int length, BiFunction<String, List<Number>, X> formatter) {
        if ((length | fromIndex | size) < 0 || size > length - fromIndex) {
            throw formatter.apply("checkFromIndexSize", List.of(fromIndex, size, length));
        }
        return fromIndex;
    }

    /// Formats the standard message for a failed bounds check.
    ///
    /// @param checkKind the check name
    /// @param arguments the check arguments
    /// @return the formatted message
    private static String outOfBoundsMessage(String checkKind, List<? extends Number> arguments) {
        if ("checkIndex".equals(checkKind) && arguments.size() == 2) {
            return "Index %s out of bounds for length %s".formatted(arguments.get(0), arguments.get(1));
        }
        if ("checkFromIndexSize".equals(checkKind) && arguments.size() == 3) {
            return "Range [%s, %s + %s) out of bounds for length %s".formatted(
                    arguments.get(0), arguments.get(0), arguments.get(1), arguments.get(2));
        }
        return "Range check failed: %s %s".formatted(checkKind, arguments);
    }
}
