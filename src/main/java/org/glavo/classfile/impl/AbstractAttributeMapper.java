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

import org.glavo.classfile.Attribute;
import org.glavo.classfile.AttributeMapper;
import org.glavo.classfile.BufWriter;

public abstract class AbstractAttributeMapper<T extends Attribute<T>>
        implements AttributeMapper<T> {

    private final String name;
    private final boolean allowMultiple;

    protected abstract void writeBody(BufWriter buf, T attr);

    public AbstractAttributeMapper(String name) {
        this(name, false);
    }

    public AbstractAttributeMapper(String name,
                                   boolean allowMultiple) {
        this.name = name;
        this.allowMultiple = allowMultiple;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void writeAttribute(BufWriter buf, T attr) {
        buf.writeIndex(buf.constantPool().utf8Entry(name));
        buf.writeInt(0);
        int start = buf.size();
        writeBody(buf, attr);
        int written = buf.size() - start;
        buf.patchInt(start - 4, 4, written);
    }

    @Override
    public boolean allowMultiple() {
        return allowMultiple;
    }

    @Override
    public String toString() {
        return String.format("AttributeMapper[name=%s, allowMultiple=%b, stability=%s]",
                name, allowMultiple, stability());
    }
}
