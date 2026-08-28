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
import org.glavo.classfile.ClassElement;
import org.glavo.classfile.ClassFile;
import org.glavo.classfile.constantpool.ClassEntry;
import java.lang.constant.ClassDesc;

import org.glavo.classfile.impl.BoundAttribute;
import org.glavo.classfile.impl.TemporaryConstantPool;
import org.glavo.classfile.impl.UnboundAttribute;

/**
 * Models the {@link Attributes#nestHost() NestHost} attribute (JVMS {@jvms
 * 4.7.28}), which indicates this class is a member of a nest and the host
 * class of the nest.
 * <p>
 * This attribute only appears on classes, and does not permit {@linkplain
 * AttributeMapper#allowMultiple multiple instances} in a class.  It has a
 * data dependency on the {@linkplain AttributeStability#CP_REFS constant pool}.
 * <p>
 * The attribute was introduced in the Java SE Platform version 11, major
 * version {@value ClassFile#JAVA_11_VERSION}.
 *
 * @see Attributes#nestHost()
 * @see NestMembersAttribute
 * @see Class#getNestHost()
 * @see Class#isNestmateOf(Class)
 * @jvms 4.7.28 The {@code NestHost} Attribute
 * @since 24
 */
public sealed interface NestHostAttribute extends Attribute<NestHostAttribute>, ClassElement
        permits BoundAttribute.BoundNestHostAttribute,
                UnboundAttribute.UnboundNestHostAttribute {

    /**
     * {@return the host class of the nest to which this class belongs}
     *
     * @see Class#getNestHost()
     */
    ClassEntry nestHost();

    /**
     * {@return a {@code NestHost} attribute}
     *
     * @param nestHost the host class of the nest
     */
    static NestHostAttribute of(ClassEntry nestHost) {
        return new UnboundAttribute.UnboundNestHostAttribute(nestHost);
    }

    /**
     * {@return a {@code NestHost} attribute}
     *
     * @param nestHost the host class of the nest
     * @throws IllegalArgumentException if {@code nestHost} represents a primitive type
     */
    static NestHostAttribute of(ClassDesc nestHost) {
        return of(TemporaryConstantPool.INSTANCE.classEntry(nestHost));
    }
}
