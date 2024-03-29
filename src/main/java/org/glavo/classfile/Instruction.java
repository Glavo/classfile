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

package org.glavo.classfile;

import org.glavo.classfile.impl.AbstractInstruction;
import org.glavo.classfile.instruction.ArrayLoadInstruction;
import org.glavo.classfile.instruction.ArrayStoreInstruction;
import org.glavo.classfile.instruction.BranchInstruction;
import org.glavo.classfile.instruction.ConstantInstruction;
import org.glavo.classfile.instruction.ConvertInstruction;
import org.glavo.classfile.instruction.DiscontinuedInstruction;
import org.glavo.classfile.instruction.FieldInstruction;
import org.glavo.classfile.instruction.IncrementInstruction;
import org.glavo.classfile.instruction.InvokeDynamicInstruction;
import org.glavo.classfile.instruction.InvokeInstruction;
import org.glavo.classfile.instruction.LoadInstruction;
import org.glavo.classfile.instruction.LookupSwitchInstruction;
import org.glavo.classfile.instruction.MonitorInstruction;
import org.glavo.classfile.instruction.NewMultiArrayInstruction;
import org.glavo.classfile.instruction.NewObjectInstruction;
import org.glavo.classfile.instruction.NewPrimitiveArrayInstruction;
import org.glavo.classfile.instruction.NewReferenceArrayInstruction;
import org.glavo.classfile.instruction.NopInstruction;
import org.glavo.classfile.instruction.OperatorInstruction;
import org.glavo.classfile.instruction.ReturnInstruction;
import org.glavo.classfile.instruction.StackInstruction;
import org.glavo.classfile.instruction.StoreInstruction;
import org.glavo.classfile.instruction.TableSwitchInstruction;
import org.glavo.classfile.instruction.ThrowInstruction;
import org.glavo.classfile.instruction.TypeCheckInstruction;

/**
 * Models an executable instruction in a method body.
 *
 * @since 22
 */
public sealed interface Instruction extends CodeElement
        permits ArrayLoadInstruction, ArrayStoreInstruction, BranchInstruction,
        ConstantInstruction, ConvertInstruction, DiscontinuedInstruction,
                FieldInstruction, InvokeDynamicInstruction, InvokeInstruction,
                LoadInstruction, StoreInstruction, IncrementInstruction,
                LookupSwitchInstruction, MonitorInstruction, NewMultiArrayInstruction,
                NewObjectInstruction, NewPrimitiveArrayInstruction, NewReferenceArrayInstruction,
                NopInstruction, OperatorInstruction, ReturnInstruction,
                StackInstruction, TableSwitchInstruction,
                ThrowInstruction, TypeCheckInstruction, AbstractInstruction {

    /**
     * {@return the opcode of this instruction}
     */
    Opcode opcode();

    /**
     * {@return the size in bytes of this instruction}
     */
    int sizeInBytes();
}
