/*
 * Copyright (c) 2022, 2025, Oracle and/or its affiliates. All rights reserved.
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

import org.glavo.classfile.Attribute;
import org.glavo.classfile.AttributeMapper;
import org.glavo.classfile.AttributeMapper.AttributeStability;
import org.glavo.classfile.Attributes;
import org.glavo.classfile.ClassFile;
import org.glavo.classfile.FieldElement;
import org.glavo.classfile.constantpool.ConstantValueEntry;
import java.lang.constant.ConstantDesc;

import org.glavo.classfile.impl.BoundAttribute;
import org.glavo.classfile.impl.TemporaryConstantPool;
import org.glavo.classfile.impl.UnboundAttribute;

/**
 * Models the {@link Attributes#constantValue() ConstantValue} attribute (JVMS
 * {@jvms 4.7.2}), which indicates this field's value is a constant and that
 * constant value.
 * <p>
 * This attribute only appears on fields, and does not permit {@linkplain
 * AttributeMapper#allowMultiple multiple instances} in a field.  It has a
 * data dependency on the {@linkplain AttributeStability#CP_REFS constant pool}.
 * <p>
 * This attribute was introduced in the Java Platform version 1.0.2, major
 * version {@value ClassFile#JAVA_1_VERSION}.
 *
 * @see Attributes#constantValue()
 * @jvms 4.7.2 The {@code ConstantValue} Attribute
 * @since 24
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
        final ConstantValueEntry entry;
        if (value instanceof Integer i) {
            entry = TemporaryConstantPool.INSTANCE.intEntry(i);
        } else if (value instanceof Float f) {
            entry = TemporaryConstantPool.INSTANCE.floatEntry(f);
        } else if (value instanceof Long l) {
            entry = TemporaryConstantPool.INSTANCE.longEntry(l);
        } else if (value instanceof Double d) {
            entry = TemporaryConstantPool.INSTANCE.doubleEntry(d);
        } else if (value instanceof String s) {
            entry = TemporaryConstantPool.INSTANCE.stringEntry(s);
        } else {
            throw new IllegalArgumentException("Invalid ConstantValueAttribute value: " + value);
        }
        return of(entry);
    }
}
