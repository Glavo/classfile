/*
 * Copyright (c) 2022, Oracle and/or its affiliates. All rights reserved.
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
package org.glavo.classfile.instruction;

import org.glavo.classfile.CodeElement;
import org.glavo.classfile.CodeModel;
import org.glavo.classfile.Instruction;
import org.glavo.classfile.Opcode;
import org.glavo.classfile.TypeKind;
import org.glavo.classfile.impl.AbstractInstruction;
import org.glavo.classfile.impl.BytecodeHelpers;
import org.glavo.classfile.impl.Util;

/**
 * Models a local variable load instruction in the {@code code} array of a
 * {@code Code} attribute.  Corresponding opcodes will have a {@code kind} of
 * {@link Opcode.Kind#LOAD}.  Delivered as a {@link CodeElement} when
 * traversing the elements of a {@link CodeModel}.
 *
 * @since 22
 */
public sealed interface LoadInstruction extends Instruction
        permits AbstractInstruction.BoundLoadInstruction,
                AbstractInstruction.UnboundLoadInstruction {

    /**
     * {@return the local variable slot to load from}
     */
    int slot();

    /**
     * {@return the type of the value to be loaded}
     */
    TypeKind typeKind();

    /**
     * {@return a local variable load instruction}
     *
     * @param kind the type of the value to be loaded
     * @param slot the local variable slot to load from
     */
    static LoadInstruction of(TypeKind kind, int slot) {
        return of(BytecodeHelpers.loadOpcode(kind, slot), slot);
    }

    /**
     * {@return a local variable load instruction}
     *
     * @param op the opcode for the specific type of load instruction,
     *           which must be of kind {@link Opcode.Kind#LOAD}
     * @param slot the local variable slot to load from
     */
    static LoadInstruction of(Opcode op, int slot) {
        Util.checkKind(op, Opcode.Kind.LOAD);
        return new AbstractInstruction.UnboundLoadInstruction(op, slot);
    }
}
