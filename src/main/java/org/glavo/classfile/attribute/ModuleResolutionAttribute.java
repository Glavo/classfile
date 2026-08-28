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
import org.glavo.classfile.ClassModel;

import org.glavo.classfile.impl.BoundAttribute;
import org.glavo.classfile.impl.UnboundAttribute;

/**
 * Models the {@link Attributes#moduleResolution() ModuleResolution} attribute,
 * which can appear on classes that {@linkplain ClassModel#isModuleInfo()
 * represent} module descriptors, to capture resolution metadata for modules.
 * <p>
 * The specification of the {@code ModuleResolution} attribute is:
 * <pre> {@code
 *  ModuleResolution_attribute {
 *    u2 attribute_name_index;    // "ModuleResolution"
 *    u4 attribute_length;        // 2
 *    u2 resolution_flags;
 *
 *  The value of the resolution_flags item is a mask of flags used to denote
 *  properties of module resolution. The flags are as follows:
 *
 *   // Optional
 *   0x0001 (DO_NOT_RESOLVE_BY_DEFAULT)
 *
 *   // At most one of:
 *   0x0002 (WARN_DEPRECATED)
 *   0x0004 (WARN_DEPRECATED_FOR_REMOVAL)
 *   0x0008 (WARN_INCUBATING)
 *  }
 * } </pre>
 * <p>
 * This attribute only appears on classes, and does not permit {@linkplain
 * AttributeMapper#allowMultiple multiple instances} in a class.  It has
 * {@linkplain AttributeStability#STATELESS no data dependency}.
 * <p>
 * This attribute is not predefined in the Java SE Platform.  This is a
 * JDK-specific nonstandard attribute produced by the {@code jdk.jlink} module,
 * which defines the {@code jlink} and {@code jmod} tools.
 *
 * @see Attributes#moduleResolution()
 * @see ModuleHashesAttribute
 * @see ModuleTargetAttribute
 * @since 24
 */
public sealed interface ModuleResolutionAttribute
        extends Attribute<ModuleResolutionAttribute>, ClassElement
        permits BoundAttribute.BoundModuleResolutionAttribute, UnboundAttribute.UnboundModuleResolutionAttribute {

    /**
     * {@return the module resolution flags}  It is a {@link
     * org.glavo.classfile##u2 u2} value.
     * <p>
     * The value of the resolution_flags item is a mask of flags used to denote
     * properties of module resolution. The flags are as follows:
     * <pre> {@code
     *   // Optional
     *   0x0001 (DO_NOT_RESOLVE_BY_DEFAULT)
     *
     *   // At most one of:
     *   0x0002 (WARN_DEPRECATED)
     *   0x0004 (WARN_DEPRECATED_FOR_REMOVAL)
     *   0x0008 (WARN_INCUBATING)
     *  } </pre>
     */
    int resolutionFlags();

    /**
     * {@return a {@code ModuleResolution} attribute}
     *
     * @param resolutionFlags the resolution flags
     * @throws IllegalArgumentException if {@code resolutionFlags} is not {@link
     *         org.glavo.classfile##u2 u2}
     */
    static ModuleResolutionAttribute of(int resolutionFlags) {
        return new UnboundAttribute.UnboundModuleResolutionAttribute(resolutionFlags);
    }
}
