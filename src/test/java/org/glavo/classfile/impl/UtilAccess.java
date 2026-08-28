/*
 * Copyright (c) 2024, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 */
package org.glavo.classfile.impl;

public final class UtilAccess {
    public static int significantOctalDigits() {
        return Util.SIGNIFICANT_OCTAL_DIGITS;
    }

    public static int powersIndex(int digit, int index) {
        return Util.powersIndex(digit, index);
    }

    public static int[] powersTable() {
        return Util.powers;
    }

    public static int reverse31() {
        return Util.INVERSE_31;
    }

    private UtilAccess() {
    }
}
