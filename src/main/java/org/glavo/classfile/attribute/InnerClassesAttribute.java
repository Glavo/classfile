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
import java.util.List;

import org.glavo.classfile.impl.BoundAttribute;
import org.glavo.classfile.impl.UnboundAttribute;

/**
 * Models the {@link Attributes#innerClasses() InnerClasses} attribute (JVMS
 * {@jvms 4.7.6}), which records which classes referenced by this {@code class}
 * file are nested classes.
 * <p>
 * This attribute only appears on classes, and does not permit {@linkplain
 * AttributeMapper#allowMultiple multiple instances} in a class.  It has a
 * data dependency on the {@linkplain AttributeStability#CP_REFS constant pool}.
 * <p>
 * The attribute was introduced in the Java SE Platform version 1.1, major
 * version {@value ClassFile#JAVA_1_VERSION}.
 *
 * @see Attributes#innerClasses()
 * @jvms 4.7.6 The {@code InnerClasses} Attribute
 * @since 24
 */
public sealed interface InnerClassesAttribute
        extends Attribute<InnerClassesAttribute>, ClassElement
        permits BoundAttribute.BoundInnerClassesAttribute,
                UnboundAttribute.UnboundInnerClassesAttribute {

    /**
     * {@return the nested classes used by this {@code class} file}
     */
    List<InnerClassInfo> classes();

    /**
     * {@return an {@code InnerClasses} attribute}
     * @param innerClasses descriptions of the nested classes
     * @throws IllegalArgumentException if the number of descriptions exceeds
     *         the limit of {@link org.glavo.classfile##u2 u2}
     */
    static InnerClassesAttribute of(List<InnerClassInfo> innerClasses) {
        return new UnboundAttribute.UnboundInnerClassesAttribute(innerClasses);
    }

    /**
     * {@return an {@code InnerClasses} attribute}
     * @param innerClasses descriptions of the nested classes
     * @throws IllegalArgumentException if the number of descriptions exceeds
     *         the limit of {@link org.glavo.classfile##u2 u2}
     */
    static InnerClassesAttribute of(InnerClassInfo... innerClasses) {
        return new UnboundAttribute.UnboundInnerClassesAttribute(List.of(innerClasses));
    }
}
