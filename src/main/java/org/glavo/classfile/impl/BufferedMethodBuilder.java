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
package org.glavo.classfile.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.glavo.classfile.AccessFlags;

import org.glavo.classfile.BufWriter;
import org.glavo.classfile.ClassModel;
import org.glavo.classfile.CodeBuilder;
import org.glavo.classfile.CodeModel;
import org.glavo.classfile.CodeTransform;
import org.glavo.classfile.constantpool.ConstantPoolBuilder;
import org.glavo.classfile.MethodBuilder;
import org.glavo.classfile.MethodElement;
import org.glavo.classfile.MethodModel;
import org.glavo.classfile.constantpool.Utf8Entry;

public final class BufferedMethodBuilder
        implements TerminalMethodBuilder, MethodInfo {
    private final List<MethodElement> elements;
    private final SplitConstantPool constantPool;
    private final Utf8Entry name;
    private final Utf8Entry desc;
    private AccessFlags flags;
    private final MethodModel original;
    private int[] parameterSlots;

    public BufferedMethodBuilder(SplitConstantPool constantPool,
                                 Utf8Entry nameInfo,
                                 Utf8Entry typeInfo,
                                 MethodModel original) {
        this.elements = new ArrayList<>();
        this.constantPool = constantPool;
        this.name = nameInfo;
        this.desc = typeInfo;
        this.flags = AccessFlags.ofMethod();
        this.original = original;
    }

    @Override
    public MethodBuilder with(MethodElement element) {
        elements.add(element);
        if (element instanceof AccessFlags f) this.flags = f;
        return this;
    }

    @Override
    public ConstantPoolBuilder constantPool() {
        return constantPool;
    }

    @Override
    public Optional<MethodModel> original() {
        return Optional.ofNullable(original);
    }

    @Override
    public Utf8Entry methodName() {
        return name;
    }

    @Override
    public Utf8Entry methodType() {
        return desc;
    }

    @Override
    public int methodFlags() {
        return flags.flagsMask();
    }

    @Override
    public int parameterSlot(int paramNo) {
        if (parameterSlots == null)
            parameterSlots = Util.parseParameterSlots(methodFlags(), methodType().stringValue());
        return parameterSlots[paramNo];
    }

    @Override
    public MethodBuilder withCode(Consumer<? super CodeBuilder> handler) {
        return with(new BufferedCodeBuilder(this, constantPool, null)
                            .run(handler)
                            .toModel());
    }

    @Override
    public MethodBuilder transformCode(CodeModel code, CodeTransform transform) {
        BufferedCodeBuilder builder = new BufferedCodeBuilder(this, constantPool, code);
        builder.transform(code, transform);
        return with(builder.toModel());
    }

    @Override
    public BufferedCodeBuilder bufferedCodeBuilder(CodeModel original) {
        return new BufferedCodeBuilder(this, constantPool, original);
    }

    public BufferedMethodBuilder run(Consumer<? super MethodBuilder> handler) {
        handler.accept(this);
        return this;
    }

    public MethodModel toModel() {
        return new Model();
    }

    public final class Model
            extends AbstractUnboundModel<MethodElement>
            implements MethodModel, MethodInfo {
        public Model() {
            super(elements);
        }

        @Override
        public AccessFlags flags() {
            return flags;
        }

        @Override
        public Optional<ClassModel> parent() {
            return original().flatMap(MethodModel::parent);
        }

        @Override
        public Utf8Entry methodName() {
            return name;
        }

        @Override
        public Utf8Entry methodType() {
            return desc;
        }

        @Override
        public int methodFlags() {
            return flags.flagsMask();
        }

        @Override
        public int parameterSlot(int paramNo) {
            return BufferedMethodBuilder.this.parameterSlot(paramNo);
        }

        @Override
        public Optional<CodeModel> code() {
            throw new UnsupportedOperationException("nyi");
        }

        @Override
        public void writeTo(DirectClassBuilder builder) {
            builder.withMethod(methodName(), methodType(), methodFlags(), new Consumer<>() {
                @Override
                public void accept(MethodBuilder mb) {
                    forEachElement(mb);
                }
            });
        }

        @Override
        public void writeTo(BufWriter buf) {
            DirectMethodBuilder mb = new DirectMethodBuilder(constantPool, name, desc, methodFlags(), null);
            elements.forEach(mb);
            mb.writeTo(buf);
        }

        @Override
        public String toString() {
            return String.format("MethodModel[methodName=%s, methodType=%s, flags=%d]",
                    name.stringValue(), desc.stringValue(), flags.flagsMask());
        }
    }
}
