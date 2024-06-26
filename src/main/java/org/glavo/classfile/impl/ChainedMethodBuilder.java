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

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import org.glavo.classfile.CodeBuilder;
import org.glavo.classfile.CodeModel;
import org.glavo.classfile.CodeTransform;
import org.glavo.classfile.MethodBuilder;
import org.glavo.classfile.MethodElement;
import org.glavo.classfile.MethodModel;
import org.glavo.classfile.constantpool.ConstantPoolBuilder;

public final class ChainedMethodBuilder implements MethodBuilder {
    final MethodBuilder downstream;
    final TerminalMethodBuilder terminal;
    final Consumer<MethodElement> consumer;

    public ChainedMethodBuilder(MethodBuilder downstream,
                                Consumer<MethodElement> consumer) {
        Objects.requireNonNull(downstream);
        this.downstream = downstream;
        this.consumer = consumer;
        if (downstream instanceof ChainedMethodBuilder cb) {
            this.terminal = cb.terminal;
        } else if (downstream instanceof TerminalMethodBuilder tb) {
            this.terminal = tb;
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public MethodBuilder with(MethodElement element) {
        consumer.accept(element);
        return this;
    }

    @Override
    public MethodBuilder withCode(Consumer<? super CodeBuilder> handler) {
        return downstream.with(terminal.bufferedCodeBuilder(null)
                                       .run(handler)
                                       .toModel());
    }

    @Override
    public MethodBuilder transformCode(CodeModel code, CodeTransform transform) {
        BufferedCodeBuilder builder = terminal.bufferedCodeBuilder(code);
        builder.transform(code, transform);
        return downstream.with(builder.toModel());
    }

    @Override
    public ConstantPoolBuilder constantPool() {
        return terminal.constantPool();
    }

    @Override
    public Optional<MethodModel> original() {
        return terminal.original();
    }

}
