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

import java.lang.constant.ConstantDesc;

import org.glavo.classfile.CodeElement;
import org.glavo.classfile.CodeModel;
import org.glavo.classfile.Instruction;
import org.glavo.classfile.Opcode;
import org.glavo.classfile.TypeKind;
import org.glavo.classfile.constantpool.LoadableConstantEntry;
import org.glavo.classfile.impl.AbstractInstruction;
import org.glavo.classfile.impl.Util;

/**
 * Models a constant-load instruction in the {@code code} array of a {@code
 * Code} attribute, including "intrinsic constant" instructions (e.g., {@code
 * iconst_0}), "argument constant" instructions (e.g., {@code bipush}), and "load
 * constant" instructions (e.g., {@code LDC}).  Corresponding opcodes will have
 * a {@code kind} of {@link Opcode.Kind#CONSTANT}.  Delivered as a {@link
 * CodeElement} when traversing the elements of a {@link CodeModel}.
 *
 * @since 22
 */
public sealed interface ConstantInstruction extends Instruction {

    /**
     * {@return the constant value}
     */
    ConstantDesc constantValue();

    /**
     * {@return the type of the constant}
     */
    TypeKind typeKind();

    /**
     * Models an "intrinsic constant" instruction (e.g., {@code
     * iconst_0}).
     *
     * @since 22
     */
        sealed interface IntrinsicConstantInstruction extends ConstantInstruction
            permits AbstractInstruction.UnboundIntrinsicConstantInstruction {

        /**
         * {@return the type of the constant}
         */
        @Override
        default TypeKind typeKind() {
            return opcode().primaryTypeKind();
        }
    }

    /**
     * Models an "argument constant" instruction (e.g., {@code
     * bipush}).
     *
     * @since 22
     */
        sealed interface ArgumentConstantInstruction extends ConstantInstruction
            permits AbstractInstruction.BoundArgumentConstantInstruction,
                    AbstractInstruction.UnboundArgumentConstantInstruction {

        @Override
        Integer constantValue();

        /**
         * {@return the type of the constant}
         */
        @Override
        default TypeKind typeKind() {
            return opcode().primaryTypeKind();
        }
    }

    /**
     * Models a "load constant" instruction (e.g., {@code
     * ldc}).
     *
     * @since 22
     */
        sealed interface LoadConstantInstruction extends ConstantInstruction
            permits AbstractInstruction.BoundLoadConstantInstruction,
                    AbstractInstruction.UnboundLoadConstantInstruction {

        /**
         * {@return the constant value}
         */
        LoadableConstantEntry constantEntry();

        /**
         * {@return the type of the constant}
         */
        @Override
        default TypeKind typeKind() {
            return constantEntry().typeKind();
        }
    }

    /**
     * {@return an intrinsic constant instruction}
     *
     * @param op the opcode for the specific type of intrinsic constant instruction,
     *           which must be of kind {@link Opcode.Kind#CONSTANT}
     */
    static IntrinsicConstantInstruction ofIntrinsic(Opcode op) {
        Util.checkKind(op, Opcode.Kind.CONSTANT);
        if (op.constantValue() == null)
            throw new IllegalArgumentException(String.format("Wrong opcode specified; found %s, expected xCONST_val", op));
        return new AbstractInstruction.UnboundIntrinsicConstantInstruction(op);
    }

    /**
     * {@return an argument constant instruction}
     *
     * @param op the opcode for the specific type of intrinsic constant instruction,
     *           which must be of kind {@link Opcode.Kind#CONSTANT}
     * @param value the constant value
     */
    static ArgumentConstantInstruction ofArgument(Opcode op, int value) {
        Util.checkKind(op, Opcode.Kind.CONSTANT);
        if (op != Opcode.BIPUSH && op != Opcode.SIPUSH)
            throw new IllegalArgumentException(String.format("Wrong opcode specified; found %s, expected BIPUSH or SIPUSH", op));
        return new AbstractInstruction.UnboundArgumentConstantInstruction(op, value);
    }

    /**
     * {@return a load constant instruction}
     *
     * @param op the opcode for the specific type of load constant instruction,
     *           which must be of kind {@link Opcode.Kind#CONSTANT}
     * @param constant the constant value
     */
    static LoadConstantInstruction ofLoad(Opcode op, LoadableConstantEntry constant) {
        Util.checkKind(op, Opcode.Kind.CONSTANT);
        if (op != Opcode.LDC && op != Opcode.LDC_W && op != Opcode.LDC2_W)
            throw new IllegalArgumentException(String.format("Wrong opcode specified; found %s, expected LDC, LDC_W or LDC2_W", op));
        return new AbstractInstruction.UnboundLoadConstantInstruction(op, constant);
    }
}
