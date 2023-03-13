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
package org.glavo.classfile.attribute;

import java.lang.constant.ConstantDesc;
import org.glavo.classfile.Attribute;
import org.glavo.classfile.FieldElement;
import org.glavo.classfile.FieldModel;
import org.glavo.classfile.constantpool.ConstantValueEntry;
import org.glavo.classfile.impl.BoundAttribute;
import org.glavo.classfile.impl.TemporaryConstantPool;
import org.glavo.classfile.impl.UnboundAttribute;

/**
 * Models the {@code ConstantValue} attribute {@jvms 4.7.2}, which can appear on
 * fields and indicates that the field's value is a constant.  Delivered as a
 * {@link FieldElement} when traversing the elements of a
 * {@link FieldModel}.
 */
public sealed interface ConstantValueAttribute
        extends Attribute<ConstantValueAttribute>, FieldElement
        permits BoundAttribute.BoundConstantValueAttribute,
        UnboundAttribute.UnboundConstantValueAttribute {

    /**
     * {@return the constant value of the field}
     */
    ConstantValueEntry constant();

    /**
     * {@return a {@code ConstantValue} attribute}
     * @param value the constant value
     */
    static ConstantValueAttribute of(ConstantValueEntry value) {
        return new UnboundAttribute.UnboundConstantValueAttribute(value);
    }

    /**
     * {@return a {@code ConstantValue} attribute}
     * @param value the constant value
     */
    static ConstantValueAttribute of(ConstantDesc value) {
        return of(switch(value) {
            case Integer i -> TemporaryConstantPool.INSTANCE.intEntry(i);
            case Float f -> TemporaryConstantPool.INSTANCE.floatEntry(f);
            case Long l -> TemporaryConstantPool.INSTANCE.longEntry(l);
            case Double d -> TemporaryConstantPool.INSTANCE.doubleEntry(d);
            case String s -> TemporaryConstantPool.INSTANCE.stringEntry(s);
            default -> throw new IllegalArgumentException("Invalid ConstantValueAtrtibute value: " + value);
        });
    }
}
