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

/*
 * @test
 * @summary Testing Classfile streaming versus model.
 * @run junit StreamedVsListTest
 */
import org.glavo.classfile.ClassModel;
import org.glavo.classfile.Classfile;
import org.glavo.classfile.CodeElement;
import org.glavo.classfile.Instruction;
import org.glavo.classfile.MethodModel;
import org.glavo.classfile.impl.DirectCodeBuilder;
import org.glavo.classfile.instruction.BranchInstruction;
import org.glavo.classfile.instruction.ConstantInstruction;
import org.glavo.classfile.instruction.FieldInstruction;
import org.glavo.classfile.instruction.IncrementInstruction;
import org.glavo.classfile.instruction.InvokeDynamicInstruction;
import org.glavo.classfile.instruction.InvokeInstruction;
import org.glavo.classfile.instruction.LoadInstruction;
import org.glavo.classfile.instruction.LookupSwitchInstruction;
import org.glavo.classfile.instruction.NewMultiArrayInstruction;
import org.glavo.classfile.instruction.NewObjectInstruction;
import org.glavo.classfile.instruction.NewPrimitiveArrayInstruction;
import org.glavo.classfile.instruction.NewReferenceArrayInstruction;
import org.glavo.classfile.instruction.StoreInstruction;
import org.glavo.classfile.instruction.TableSwitchInstruction;
import org.glavo.classfile.instruction.TypeCheckInstruction;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

class StreamedVsListTest {
    static byte[] fileBytes;

    static {
        try {
            fileBytes = DirectCodeBuilder.class.getResourceAsStream("DirectCodeBuilder.class").readAllBytes();
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }


    @Test
    void testStreamed() throws Exception {
        Vs vs = new Vs();
        vs.test();
        if (vs.failed) {
            throw new AssertionError("assertions failed");
        }
    }

    private class Vs {
        boolean failed;
        ClassModel cm = Classfile.parse(fileBytes);
        String meth;
        CodeElement iim;
        CodeElement mim;
        int n;

        void test() {
            for (MethodModel mm : cm.methods()) {
                try {
                    mm.code().ifPresent(code -> {
                        meth = mm.methodName().stringValue();
                        List<CodeElement> insts = code.elementList();
                        n = 0;
                        for (CodeElement element : code) {
                            iim = element;
                            mim = insts.get(n++);
                            if (iim instanceof Instruction)
                                testInstruction();
                        }
                    });
                } catch (Throwable ex) {
                    failed = true;
                    System.err.printf("%s.%s #%d[%s]: ", cm.thisClass().asInternalName(), meth, n - 1, iim instanceof Instruction i ? i.opcode() : "<->");
                    System.err.printf("Threw: %s" + "%n", ex);
                    throw ex;
                }
            }
        }

        void testInstruction() {
            assertEquals(((Instruction)iim).opcode(), ((Instruction)mim).opcode(), "Opcodes don't match");
            switch (((Instruction)iim).opcode().kind()) {
                case LOAD: {
                    LoadInstruction i = (LoadInstruction) iim;
                    LoadInstruction x = (LoadInstruction) mim;
                    assertEquals(i.slot(), x.slot(), "variable");
                    break;
                }
                case STORE: {
                    StoreInstruction i = (StoreInstruction) iim;
                    StoreInstruction x = (StoreInstruction) mim;
                    assertEquals(i.slot(), x.slot(), "variable");
                    break;
                }
                case INCREMENT: {
                    IncrementInstruction i = (IncrementInstruction) iim;
                    IncrementInstruction x = (IncrementInstruction) mim;
                    assertEquals(i.slot(), x.slot(), "variable");
                    assertEquals(i.constant(), x.constant(), "constant");
                    break;
                }
                case BRANCH: {
                    BranchInstruction i = (BranchInstruction) iim;
                    BranchInstruction x = (BranchInstruction) mim;
                    //TODO: test labels
                    break;
                }
                case TABLE_SWITCH: {
                    TableSwitchInstruction i = (TableSwitchInstruction) iim;
                    TableSwitchInstruction x = (TableSwitchInstruction) mim;
                    assertEquals(i.lowValue(), x.lowValue(), "lowValue");
                    assertEquals(i.highValue(), x.highValue(), "highValue");
                    assertEquals(i.cases().size(), x.cases().size(), "cases().size");
                    //TODO: test labels
                    break;
                }
                case LOOKUP_SWITCH: {
                    LookupSwitchInstruction i = (LookupSwitchInstruction) iim;
                    LookupSwitchInstruction x = (LookupSwitchInstruction) mim;
                    assertEquals(i.cases(), (Object) x.cases(), "matches: ");
                    /**
                    var ipairs = i.pairs();
                    var xpairs = x.pairs();
                    assertEquals("pairs().size", ipairs.size(), xpairs.size());
                    for (int k = 0; k < xpairs.size(); ++k) {
                        assertEquals("pair #" + k, ipairs.get(k).caseMatch(), xpairs.get(k).caseMatch());
                    }
                    **/
                    //TODO: test labels
                    break;
                }
                case RETURN:
                case THROW_EXCEPTION:
                    break;
                case FIELD_ACCESS: {
                    FieldInstruction i = (FieldInstruction) iim;
                    FieldInstruction x = (FieldInstruction) mim;
                    assertEquals(i.owner().asInternalName(), (Object) x.owner().asInternalName(), "owner");
                    assertEquals(i.name().stringValue(), (Object) x.name().stringValue(), "name");
                    assertEquals(i.type().stringValue(), (Object) x.type().stringValue(), "type");
                    break;
                }
                case INVOKE: {
                    InvokeInstruction i = (InvokeInstruction) iim;
                    InvokeInstruction x = (InvokeInstruction) mim;
                    assertEquals(i.owner().asInternalName(), (Object) x.owner().asInternalName(), "owner");
                    assertEquals(i.name().stringValue(), (Object) x.name().stringValue(), "name");
                    assertEquals(i.type().stringValue(), (Object) x.type().stringValue(), "type");
                    assertEquals(i.isInterface(), (Object) x.isInterface(), "isInterface");
                    assertEquals(i.count(), x.count(), "count");
                    break;
                }
                case INVOKE_DYNAMIC: {
                    InvokeDynamicInstruction i = (InvokeDynamicInstruction) iim;
                    InvokeDynamicInstruction x = (InvokeDynamicInstruction) mim;
                    assertEquals(i.bootstrapMethod(), x.bootstrapMethod(), "bootstrapMethod");
                    assertEquals(i.bootstrapArgs(), (Object) x.bootstrapArgs(), "bootstrapArgs");
                    assertEquals(i.name().stringValue(), (Object) x.name().stringValue(), "name");
                    assertEquals(i.type().stringValue(), (Object) x.type().stringValue(), "type");
                    break;
                }
                case NEW_OBJECT: {
                    NewObjectInstruction i = (NewObjectInstruction) iim;
                    NewObjectInstruction x = (NewObjectInstruction) mim;
                    assertEquals(i.className().asInternalName(), (Object) x.className().asInternalName(), "type");
                    break;
                }
                case NEW_PRIMITIVE_ARRAY:
                {
                    NewPrimitiveArrayInstruction i = (NewPrimitiveArrayInstruction) iim;
                    NewPrimitiveArrayInstruction x = (NewPrimitiveArrayInstruction) mim;
                    assertEquals(i.typeKind(), x.typeKind(), "type");
                    break;
                }

                case NEW_REF_ARRAY:{
                    NewReferenceArrayInstruction i = (NewReferenceArrayInstruction) iim;
                    NewReferenceArrayInstruction x = (NewReferenceArrayInstruction) mim;
                    assertEquals(i.componentType().asInternalName(), (Object) x.componentType().asInternalName(), "type");
                    break;
                }

                case NEW_MULTI_ARRAY:{
                    NewMultiArrayInstruction i = (NewMultiArrayInstruction) iim;
                    NewMultiArrayInstruction x = (NewMultiArrayInstruction) mim;
                    assertEquals(i.arrayType().asInternalName(), (Object) x.arrayType().asInternalName(), "type");
                    assertEquals(i.dimensions(), x.dimensions(), "dimensions");
                    break;
                }

                case TYPE_CHECK: {
                    TypeCheckInstruction i = (TypeCheckInstruction) iim;
                    TypeCheckInstruction x = (TypeCheckInstruction) mim;
                    assertEquals(i.type().asInternalName(), (Object) x.type().asInternalName(), "type");
                    break;
                }
                case ARRAY_LOAD:
                case ARRAY_STORE:
                case STACK:
                case CONVERT:
                case OPERATOR:
                    break;
                case CONSTANT: {
                    ConstantInstruction i = (ConstantInstruction) iim;
                    ConstantInstruction x = (ConstantInstruction) mim;
                    assertEquals(i.constantValue(), x.constantValue(), "constantValue");
                }
                break;
                case MONITOR:
                case NOP:
                    break;

            }
        }
    }
}
