/*
 * Copyright (c) 2022, 2026, Oracle and/or its affiliates. All rights reserved.
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
import org.glavo.classfile.ClassModel;
import org.glavo.classfile.constantpool.PackageEntry;
import org.glavo.classfile.constant.PackageDesc;
import java.lang.module.ModuleDescriptor;
import java.util.Arrays;
import java.util.List;

import org.glavo.classfile.impl.BoundAttribute;
import org.glavo.classfile.impl.TemporaryConstantPool;
import org.glavo.classfile.impl.UnboundAttribute;
import org.glavo.classfile.impl.Util;

/**
 * Models the {@link Attributes#modulePackages() ModulePackages} attribute (JVMS
 * {@jvms 4.7.26}), which can appear on classes that {@linkplain
 * ClassModel#isModuleInfo() represent} module descriptors to indicate packages
 * in the module used by the module descriptor.
 * <p>
 * This attribute only appears on classes, and does not permit {@linkplain
 * AttributeMapper#allowMultiple multiple instances} in a class.  It has a
 * data dependency on the {@linkplain AttributeStability#CP_REFS constant pool}.
 * <p>
 * The attribute was introduced in the Java SE Platform version 9, major version
 * {@value ClassFile#JAVA_9_VERSION}.
 *
 * @see Attributes#modulePackages()
 * @see ModuleDescriptor#packages()
 * @jvms 4.7.26 The {@code ModulePackages} Attribute
 * @since 24
 */
public sealed interface ModulePackagesAttribute
        extends Attribute<ModulePackagesAttribute>, ClassElement
        permits BoundAttribute.BoundModulePackagesAttribute,
                UnboundAttribute.UnboundModulePackagesAttribute {

    /**
     * {@return the packages used by the module descriptor}  This must include
     * all packages opened or exported by the module, as well as the packages of
     * any service providers, and the package for the main class.
     */
    List<PackageEntry> packages();

    /**
     * {@return a {@code ModulePackages} attribute}
     * @param packages the packages
     * @throws IllegalArgumentException if the number of packages exceeds the
     *         limit of {@link org.glavo.classfile##u2 u2}
     */
    static ModulePackagesAttribute of(List<PackageEntry> packages) {
        return new UnboundAttribute.UnboundModulePackagesAttribute(packages);
    }

    /**
     * {@return a {@code ModulePackages} attribute}
     * @param packages the packages
     * @throws IllegalArgumentException if the number of packages exceeds the
     *         limit of {@link org.glavo.classfile##u2 u2}
     */
    static ModulePackagesAttribute of(PackageEntry... packages) {
        return of(List.of(packages));
    }

    /**
     * {@return a {@code ModulePackages} attribute}
     * @param packages the packages
     * @throws IllegalArgumentException if any of {@code packages} represents an
     *         unnamed package; or if the number of packages exceeds the
     *         limit of {@link org.glavo.classfile##u2 u2}
     */
    static ModulePackagesAttribute ofNames(List<PackageDesc> packages) {
        return of(Util.packageEntryList(packages));
    }

    /**
     * {@return a {@code ModulePackages} attribute}
     * @param packages the packages
     * @throws IllegalArgumentException if any of {@code packages} represents an
     *         unnamed package; or if the number of packages exceeds the
     *         limit of {@link org.glavo.classfile##u2 u2}
     */
    static ModulePackagesAttribute ofNames(PackageDesc... packages) {
        // List view, since ref to packages is temporary
        return ofNames(Arrays.asList(packages));
    }
}
