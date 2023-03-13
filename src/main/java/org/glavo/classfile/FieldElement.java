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

import org.glavo.classfile.attribute.ConstantValueAttribute;
import org.glavo.classfile.attribute.DeprecatedAttribute;
import org.glavo.classfile.attribute.RuntimeInvisibleAnnotationsAttribute;
import org.glavo.classfile.attribute.RuntimeInvisibleTypeAnnotationsAttribute;
import org.glavo.classfile.attribute.RuntimeVisibleAnnotationsAttribute;
import org.glavo.classfile.attribute.RuntimeVisibleTypeAnnotationsAttribute;
import org.glavo.classfile.attribute.SignatureAttribute;
import org.glavo.classfile.attribute.SyntheticAttribute;
import org.glavo.classfile.attribute.UnknownAttribute;

/**
 * A {@link ClassfileElement} that can appear when traversing the elements
 * of a {@link FieldModel} or be presented to a {@link FieldBuilder}.
 */
public sealed interface FieldElement extends ClassfileElement
        permits AccessFlags,
                CustomAttribute, ConstantValueAttribute, DeprecatedAttribute,
        RuntimeInvisibleAnnotationsAttribute, RuntimeInvisibleTypeAnnotationsAttribute,
        RuntimeVisibleAnnotationsAttribute, RuntimeVisibleTypeAnnotationsAttribute,
                SignatureAttribute, SyntheticAttribute, UnknownAttribute {

}
