/*
 * Copyright (c) 2023, Oracle and/or its affiliates. All rights reserved.
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

/*
 * @test
 * @bug 8304937
 * @compile -parameters ClassBuildingTest.java
 * @summary Ensure that class transform chaining works.
 * @run junit ClassBuildingTest
 */

import helpers.ByteArrayClassLoader;
import org.glavo.classfile.ClassModel;
import org.glavo.classfile.ClassTransform;
import org.glavo.classfile.Classfile;
import org.glavo.classfile.MethodTransform;
import org.glavo.classfile.attribute.MethodParametersAttribute;
import org.glavo.classfile.attribute.SignatureAttribute;
import org.glavo.classfile.components.ClassRemapper;
import org.junit.jupiter.api.Test;

import java.lang.constant.ClassDesc;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;

public class ClassBuildingTest {
    @Test
    public void test() throws Throwable {
        ClassModel cm;
        try (var in = ClassBuildingTest.class.getResourceAsStream("/Outer$1Local.class")) {
            cm = Classfile.parse(Objects.requireNonNull(in).readAllBytes());
        }

        ClassTransform transform = ClassRemapper.of(Map.of(ClassDesc.of("Outer"), ClassDesc.of("Router")));
        transform = transform.andThen(ClassTransform.transformingMethods(MethodTransform.dropping(me
                -> me instanceof MethodParametersAttribute)));
        transform = transform.andThen(ClassTransform.transformingMethods(MethodTransform.dropping(me
                -> me instanceof SignatureAttribute)));

        // java.lang.LinkageError: loader 'app' attempted duplicate class definition for Outer$1Local. (Outer$1Local is in unnamed module of loader 'app')
        // MethodHandles.lookup().defineClass(cm.transform(transform));

        var loader = new ByteArrayClassLoader(ClassBuildingTest.class.getClassLoader(), "Outer$1Local", cm.transform(transform));
        loader.findClass("Outer$1Local");
    }
}

class Outer {
    void method(int p) {
        class Local<V> {
            Local(V value, int q, Comparator<Integer> p2) {
                System.out.println(p + q);
            }
        }
    }
}
