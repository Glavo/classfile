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

import java.lang.constant.ClassDesc;
import java.util.Arrays;
import java.util.List;

import org.glavo.classfile.Attribute;
import org.glavo.classfile.ClassElement;
import org.glavo.classfile.ClassModel;
import org.glavo.classfile.constantpool.ClassEntry;
import org.glavo.classfile.impl.BoundAttribute;
import org.glavo.classfile.impl.UnboundAttribute;
import org.glavo.classfile.impl.Util;

/**
 * Models the {@code PermittedSubclasses} attribute {@jvms 4.7.31}, which can
 * appear on classes to indicate which classes may extend this class.
 * Delivered as a {@link ClassElement} when
 * traversing the elements of a {@link ClassModel}.
 */
public sealed interface PermittedSubclassesAttribute
        extends Attribute<PermittedSubclassesAttribute>, ClassElement
        permits BoundAttribute.BoundPermittedSubclassesAttribute, UnboundAttribute.UnboundPermittedSubclassesAttribute {

    /**
     * {@return the list of permitted subclasses}
     */
    List<ClassEntry> permittedSubclasses();

    /**
     * {@return a {@code PermittedSubclasses} attribute}
     * @param permittedSubclasses the permitted subclasses
     */
    static PermittedSubclassesAttribute of(List<ClassEntry> permittedSubclasses) {
        return new UnboundAttribute.UnboundPermittedSubclassesAttribute(permittedSubclasses);
    }

    /**
     * {@return a {@code PermittedSubclasses} attribute}
     * @param permittedSubclasses the permitted subclasses
     */
    static PermittedSubclassesAttribute of(ClassEntry... permittedSubclasses) {
        return of(List.of(permittedSubclasses));
    }

    /**
     * {@return a {@code PermittedSubclasses} attribute}
     * @param permittedSubclasses the permitted subclasses
     */
    static PermittedSubclassesAttribute ofSymbols(List<ClassDesc> permittedSubclasses) {
        return of(Util.entryList(permittedSubclasses));
    }

    /**
     * {@return a {@code PermittedSubclasses} attribute}
     * @param permittedSubclasses the permitted subclasses
     */
    static PermittedSubclassesAttribute ofSymbols(ClassDesc... permittedSubclasses) {
        // List view, since ref to nestMembers is temporary
        return ofSymbols(Arrays.asList(permittedSubclasses));
    }
}
