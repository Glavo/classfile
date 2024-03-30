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

import org.glavo.classfile.attribute.AnnotationDefaultAttribute;
import org.glavo.classfile.attribute.BootstrapMethodsAttribute;
import org.glavo.classfile.attribute.CharacterRangeTableAttribute;
import org.glavo.classfile.attribute.CodeAttribute;
import org.glavo.classfile.attribute.CompilationIDAttribute;
import org.glavo.classfile.attribute.ConstantValueAttribute;
import org.glavo.classfile.attribute.DeprecatedAttribute;
import org.glavo.classfile.attribute.EnclosingMethodAttribute;
import org.glavo.classfile.attribute.ExceptionsAttribute;
import org.glavo.classfile.attribute.InnerClassesAttribute;
import org.glavo.classfile.attribute.LineNumberTableAttribute;
import org.glavo.classfile.attribute.LocalVariableTableAttribute;
import org.glavo.classfile.attribute.LocalVariableTypeTableAttribute;
import org.glavo.classfile.attribute.MethodParametersAttribute;
import org.glavo.classfile.attribute.ModuleAttribute;
import org.glavo.classfile.attribute.ModuleHashesAttribute;
import org.glavo.classfile.attribute.ModuleMainClassAttribute;
import org.glavo.classfile.attribute.ModulePackagesAttribute;
import org.glavo.classfile.attribute.ModuleResolutionAttribute;
import org.glavo.classfile.attribute.ModuleTargetAttribute;
import org.glavo.classfile.attribute.NestHostAttribute;
import org.glavo.classfile.attribute.NestMembersAttribute;
import org.glavo.classfile.attribute.PermittedSubclassesAttribute;
import org.glavo.classfile.attribute.RecordAttribute;
import org.glavo.classfile.attribute.RuntimeInvisibleAnnotationsAttribute;
import org.glavo.classfile.attribute.RuntimeInvisibleParameterAnnotationsAttribute;
import org.glavo.classfile.attribute.RuntimeInvisibleTypeAnnotationsAttribute;
import org.glavo.classfile.attribute.RuntimeVisibleAnnotationsAttribute;
import org.glavo.classfile.attribute.RuntimeVisibleParameterAnnotationsAttribute;
import org.glavo.classfile.attribute.RuntimeVisibleTypeAnnotationsAttribute;
import org.glavo.classfile.attribute.SignatureAttribute;
import org.glavo.classfile.attribute.SourceDebugExtensionAttribute;
import org.glavo.classfile.attribute.SourceFileAttribute;
import org.glavo.classfile.attribute.SourceIDAttribute;
import org.glavo.classfile.attribute.StackMapTableAttribute;
import org.glavo.classfile.attribute.SyntheticAttribute;
import org.glavo.classfile.attribute.UnknownAttribute;
import org.glavo.classfile.impl.BoundAttribute;
import org.glavo.classfile.impl.UnboundAttribute;

/**
 * Models a classfile attribute {@jvms 4.7}.  Many, though not all, subtypes of
 * {@linkplain Attribute} will implement {@link ClassElement}, {@link
 * MethodElement}, {@link FieldElement}, or {@link CodeElement}; attributes that
 * are also elements will be delivered when traversing the elements of the
 * corresponding model type. Additionally, all attributes are accessible
 * directly from the corresponding model type through {@link
 * AttributedElement#findAttribute(AttributeMapper)}.
 * @param <A> the attribute type
 *
 * @sealedGraph
 * @since 22
 */
public sealed interface Attribute<A extends Attribute<A>>
        extends WritableElement<A>
        permits AnnotationDefaultAttribute, BootstrapMethodsAttribute,
        CharacterRangeTableAttribute, CodeAttribute, CompilationIDAttribute,
                ConstantValueAttribute, DeprecatedAttribute, EnclosingMethodAttribute,
                ExceptionsAttribute, InnerClassesAttribute, LineNumberTableAttribute,
                LocalVariableTableAttribute, LocalVariableTypeTableAttribute,
                MethodParametersAttribute, ModuleAttribute, ModuleHashesAttribute,
                ModuleMainClassAttribute, ModulePackagesAttribute, ModuleResolutionAttribute,
                ModuleTargetAttribute, NestHostAttribute, NestMembersAttribute,
                PermittedSubclassesAttribute,
                RecordAttribute, RuntimeInvisibleAnnotationsAttribute,
                RuntimeInvisibleParameterAnnotationsAttribute, RuntimeInvisibleTypeAnnotationsAttribute,
                RuntimeVisibleAnnotationsAttribute, RuntimeVisibleParameterAnnotationsAttribute,
                RuntimeVisibleTypeAnnotationsAttribute, SignatureAttribute,
                SourceDebugExtensionAttribute, SourceFileAttribute, SourceIDAttribute,
                StackMapTableAttribute, SyntheticAttribute,
                UnknownAttribute, BoundAttribute, UnboundAttribute, CustomAttribute {
    /**
     * {@return the name of the attribute}
     */
    String attributeName();

    /**
     * {@return the {@link AttributeMapper} associated with this attribute}
     */
    AttributeMapper<A> attributeMapper();
}
