package org.glavo.classfile.jdk;

import java.lang.constant.ClassDesc;
import java.util.Objects;

/// Provides nominal-descriptor operations introduced after Java 17.
public final class ClassDescUtils {
    /// Validates a JVM internal class or interface name.
    ///
    /// @param name the internal name
    /// @throws IllegalArgumentException if `name` contains `.`, `;`, or `[`
    private static void validateInternalClassName(String name) {
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (ch == ';' || ch == '[' || ch == '.')
                throw new IllegalArgumentException("Invalid class name: " + name);
        }
    }

    /// Creates a nominal descriptor from a JVM internal class or interface name.
    ///
    /// @param name the internal name
    /// @return the nominal descriptor
    /// @throws NullPointerException if `name` is `null`
    /// @throws IllegalArgumentException if `name` is not a valid internal name
    public static ClassDesc ofInternalName(String name) {
        validateInternalClassName(Objects.requireNonNull(name));
        return ClassDesc.ofDescriptor("L" + name + ";");
    }

    /// Returns the JVM internal name represented by a class or interface descriptor.
    ///
    /// @param descriptor the class or interface descriptor
    /// @return the internal name
    /// @throws NullPointerException if `descriptor` is `null`
    /// @throws IllegalArgumentException if `descriptor` is not a class or interface descriptor
    public static String internalName(ClassDesc descriptor) {
        Objects.requireNonNull(descriptor);
        if (!descriptor.isClassOrInterface()) {
            throw new IllegalArgumentException(descriptor.descriptorString());
        }
        String value = descriptor.descriptorString();
        return value.substring(1, value.length() - 1);
    }

    /// Returns the nominal descriptor of a loaded type.
    ///
    /// @param type the loaded type
    /// @return its nominal descriptor
    /// @throws NullPointerException if `type` is `null`
    public static ClassDesc referenceClassDesc(Class<?> type) {
        Objects.requireNonNull(type);
        return ClassDesc.ofDescriptor(type.descriptorString());
    }

    /// Prevents instantiation.
    private ClassDescUtils() {
    }
}
