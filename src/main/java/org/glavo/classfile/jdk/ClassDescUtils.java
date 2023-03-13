package org.glavo.classfile.jdk;

import java.lang.constant.ClassDesc;
import java.util.Objects;

public final class ClassDescUtils {
    private static void validateInternalClassName(String name) {
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (ch == ';' || ch == '[' || ch == '.')
                throw new IllegalArgumentException("Invalid class name: " + name);
        }
    }

    public static ClassDesc ofInternalName(String name) {
        validateInternalClassName(Objects.requireNonNull(name));
        return ClassDesc.ofDescriptor("L" + name + ";");
    }
}
