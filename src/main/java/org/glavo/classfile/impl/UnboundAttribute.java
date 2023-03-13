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

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.glavo.classfile.*;
import org.glavo.classfile.constantpool.ClassEntry;
import org.glavo.classfile.Label;
import org.glavo.classfile.TypeAnnotation;
import org.glavo.classfile.attribute.AnnotationDefaultAttribute;
import org.glavo.classfile.attribute.BootstrapMethodsAttribute;
import org.glavo.classfile.attribute.CharacterRangeInfo;
import org.glavo.classfile.attribute.CharacterRangeTableAttribute;
import org.glavo.classfile.attribute.CompilationIDAttribute;
import org.glavo.classfile.attribute.ConstantValueAttribute;
import org.glavo.classfile.attribute.DeprecatedAttribute;
import org.glavo.classfile.attribute.EnclosingMethodAttribute;
import org.glavo.classfile.attribute.ExceptionsAttribute;
import org.glavo.classfile.attribute.InnerClassInfo;
import org.glavo.classfile.attribute.InnerClassesAttribute;
import org.glavo.classfile.attribute.LineNumberInfo;
import org.glavo.classfile.attribute.LineNumberTableAttribute;
import org.glavo.classfile.attribute.LocalVariableInfo;
import org.glavo.classfile.attribute.LocalVariableTableAttribute;
import org.glavo.classfile.attribute.LocalVariableTypeInfo;
import org.glavo.classfile.attribute.LocalVariableTypeTableAttribute;
import org.glavo.classfile.attribute.MethodParameterInfo;
import org.glavo.classfile.attribute.MethodParametersAttribute;
import org.glavo.classfile.attribute.ModuleAttribute;
import org.glavo.classfile.attribute.ModuleExportInfo;
import org.glavo.classfile.attribute.ModuleHashInfo;
import org.glavo.classfile.attribute.ModuleHashesAttribute;
import org.glavo.classfile.attribute.ModuleMainClassAttribute;
import org.glavo.classfile.attribute.ModuleOpenInfo;
import org.glavo.classfile.attribute.ModulePackagesAttribute;
import org.glavo.classfile.attribute.ModuleProvideInfo;
import org.glavo.classfile.attribute.ModuleRequireInfo;
import org.glavo.classfile.attribute.ModuleResolutionAttribute;
import org.glavo.classfile.attribute.ModuleTargetAttribute;
import org.glavo.classfile.attribute.NestHostAttribute;
import org.glavo.classfile.attribute.NestMembersAttribute;
import org.glavo.classfile.attribute.PermittedSubclassesAttribute;
import org.glavo.classfile.attribute.RecordAttribute;
import org.glavo.classfile.attribute.RecordComponentInfo;
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
import org.glavo.classfile.attribute.StackMapFrameInfo;
import org.glavo.classfile.attribute.SyntheticAttribute;
import org.glavo.classfile.constantpool.ConstantValueEntry;
import org.glavo.classfile.constantpool.ModuleEntry;
import org.glavo.classfile.constantpool.NameAndTypeEntry;
import org.glavo.classfile.constantpool.PackageEntry;
import org.glavo.classfile.constantpool.Utf8Entry;

public abstract sealed class UnboundAttribute<T extends Attribute<T>>
        extends AbstractElement
        implements Attribute<T> {
    protected final AttributeMapper<T> mapper;

    public UnboundAttribute(AttributeMapper<T> mapper) {
        this.mapper = mapper;
    }

    @Override
    public AttributeMapper<T> attributeMapper() {
        return mapper;
    }

    @Override
    public String attributeName() {
        return mapper.name();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void writeTo(BufWriter buf) {
        mapper.writeAttribute(buf, (T) this);
    }

    @Override
    public void writeTo(DirectClassBuilder builder) {
        builder.writeAttribute(this);
    }

    @Override
    public void writeTo(DirectCodeBuilder builder) {
        builder.writeAttribute(this);
    }

    @Override
    public void writeTo(DirectMethodBuilder builder) {
        builder.writeAttribute(this);
    }

    @Override
    public void writeTo(DirectFieldBuilder builder) {
        builder.writeAttribute(this);
    }

    @Override
    public String toString() {
        return String.format("Attribute[name=%s]", mapper.name());
    }
    public static final class UnboundConstantValueAttribute
            extends UnboundAttribute<ConstantValueAttribute>
            implements ConstantValueAttribute {

        private final ConstantValueEntry entry;

        public UnboundConstantValueAttribute(ConstantValueEntry entry) {
            super(Attributes.CONSTANT_VALUE);
            this.entry = entry;
        }

        @Override
        public ConstantValueEntry constant() {
            return entry;
        }

    }

    public static final class UnboundDeprecatedAttribute
            extends UnboundAttribute<DeprecatedAttribute>
            implements DeprecatedAttribute {
        public UnboundDeprecatedAttribute() {
            super(Attributes.DEPRECATED);
        }
    }

    public static final class UnboundSyntheticAttribute
            extends UnboundAttribute<SyntheticAttribute>
            implements SyntheticAttribute {
        public UnboundSyntheticAttribute() {
            super(Attributes.SYNTHETIC);
        }
    }

    public static final class UnboundSignatureAttribute
            extends UnboundAttribute<SignatureAttribute>
            implements SignatureAttribute {
        private final Utf8Entry signature;

        public UnboundSignatureAttribute(Utf8Entry signature) {
            super(Attributes.SIGNATURE);
            this.signature = signature;
        }

        @Override
        public Utf8Entry signature() {
            return signature;
        }
    }

    public static final class UnboundExceptionsAttribute
            extends UnboundAttribute<ExceptionsAttribute>
            implements ExceptionsAttribute {
        private final List<ClassEntry> exceptions;

        public UnboundExceptionsAttribute(List<ClassEntry> exceptions) {
            super(Attributes.EXCEPTIONS);
            this.exceptions = List.copyOf(exceptions);
        }

        @Override
        public List<ClassEntry> exceptions() {
            return exceptions;
        }
    }

    public static final class UnboundAnnotationDefaultAttribute
            extends UnboundAttribute<AnnotationDefaultAttribute>
            implements AnnotationDefaultAttribute {
        private final AnnotationValue annotationDefault;

        public UnboundAnnotationDefaultAttribute(AnnotationValue annotationDefault) {
            super(Attributes.ANNOTATION_DEFAULT);
            this.annotationDefault = annotationDefault;
        }

        @Override
        public AnnotationValue defaultValue() {
            return annotationDefault;
        }
    }

    public static final class UnboundSourceFileAttribute extends UnboundAttribute<SourceFileAttribute>
            implements SourceFileAttribute {
        private final Utf8Entry sourceFile;

        public UnboundSourceFileAttribute(Utf8Entry sourceFile) {
            super(Attributes.SOURCE_FILE);
            this.sourceFile = sourceFile;
        }

        @Override
        public Utf8Entry sourceFile() {
            return sourceFile;
        }

    }

    public static final class UnboundStackMapTableAttribute extends UnboundAttribute<StackMapTableAttribute>
            implements StackMapTableAttribute {
        private final List<StackMapFrameInfo> entries;

        public UnboundStackMapTableAttribute(List<StackMapFrameInfo> entries) {
            super(Attributes.STACK_MAP_TABLE);
            this.entries = List.copyOf(entries);
        }

        @Override
        public List<StackMapFrameInfo> entries() {
            return entries;
        }
    }

    public static final class UnboundInnerClassesAttribute
            extends UnboundAttribute<InnerClassesAttribute>
            implements InnerClassesAttribute {
        private final List<InnerClassInfo> innerClasses;

        public UnboundInnerClassesAttribute(List<InnerClassInfo> innerClasses) {
            super(Attributes.INNER_CLASSES);
            this.innerClasses = List.copyOf(innerClasses);
        }

        @Override
        public List<InnerClassInfo> classes() {
            return innerClasses;
        }
    }

    public static final class UnboundRecordAttribute
            extends UnboundAttribute<RecordAttribute>
            implements RecordAttribute {
        private final List<RecordComponentInfo> components;

        public UnboundRecordAttribute(List<RecordComponentInfo> components) {
            super(Attributes.RECORD);
            this.components = List.copyOf(components);
        }

        @Override
        public List<RecordComponentInfo> components() {
            return components;
        }
    }

    public static final class UnboundEnclosingMethodAttribute
            extends UnboundAttribute<EnclosingMethodAttribute>
            implements EnclosingMethodAttribute {
        private final ClassEntry classEntry;
        private final NameAndTypeEntry method;

        public UnboundEnclosingMethodAttribute(ClassEntry classEntry, NameAndTypeEntry method) {
            super(Attributes.ENCLOSING_METHOD);
            this.classEntry = classEntry;
            this.method = method;
        }

        @Override
        public ClassEntry enclosingClass() {
            return classEntry;
        }

        @Override
        public Optional<NameAndTypeEntry> enclosingMethod() {
            return Optional.ofNullable(method);
        }
    }

    public static final class UnboundMethodParametersAttribute
            extends UnboundAttribute<MethodParametersAttribute>
            implements MethodParametersAttribute {
        private final List<MethodParameterInfo> parameters;

        public UnboundMethodParametersAttribute(List<MethodParameterInfo> parameters) {
            super(Attributes.METHOD_PARAMETERS);
            this.parameters = List.copyOf(parameters);
        }

        @Override
        public List<MethodParameterInfo> parameters() {
            return parameters;
        }
    }

    public static final class UnboundModuleTargetAttribute
            extends UnboundAttribute<ModuleTargetAttribute>
            implements ModuleTargetAttribute {
        final Utf8Entry moduleTarget;

        public UnboundModuleTargetAttribute(Utf8Entry moduleTarget) {
            super(Attributes.MODULE_TARGET);
            this.moduleTarget = moduleTarget;
        }

        @Override
        public Utf8Entry targetPlatform() {
            return moduleTarget;
        }
    }

    public static final class UnboundModuleMainClassAttribute
            extends UnboundAttribute<ModuleMainClassAttribute>
            implements ModuleMainClassAttribute {
        final ClassEntry mainClass;

        public UnboundModuleMainClassAttribute(ClassEntry mainClass) {
            super(Attributes.MODULE_MAIN_CLASS);
            this.mainClass = mainClass;
        }

        @Override
        public ClassEntry mainClass() {
            return mainClass;
        }
    }

    public static final class UnboundModuleHashesAttribute
            extends UnboundAttribute<ModuleHashesAttribute>
            implements ModuleHashesAttribute {
        private final Utf8Entry algorithm;
        private final List<ModuleHashInfo> hashes;

        public UnboundModuleHashesAttribute(Utf8Entry algorithm, List<ModuleHashInfo> hashes) {
            super(Attributes.MODULE_HASHES);
            this.algorithm = algorithm;
            this.hashes = List.copyOf(hashes);
        }

        @Override
        public Utf8Entry algorithm() {
            return algorithm;
        }

        @Override
        public List<ModuleHashInfo> hashes() {
            return hashes;
        }
    }

    public static final class UnboundModulePackagesAttribute
            extends UnboundAttribute<ModulePackagesAttribute>
            implements ModulePackagesAttribute {
        private final Collection<PackageEntry> packages;

        public UnboundModulePackagesAttribute(Collection<PackageEntry> packages) {
            super(Attributes.MODULE_PACKAGES);
            this.packages = List.copyOf(packages);
        }

        @Override
        public List<PackageEntry> packages() {
            return List.copyOf(packages);
        }
    }

    public static final class UnboundModuleResolutionAttribute
            extends UnboundAttribute<ModuleResolutionAttribute>
            implements ModuleResolutionAttribute {
        private final int resolutionFlags;

        public UnboundModuleResolutionAttribute(int flags) {
            super(Attributes.MODULE_RESOLUTION);
            resolutionFlags = flags;
        }

        @Override
        public int resolutionFlags() {
            return resolutionFlags;
        }
    }

    public static final class UnboundPermittedSubclassesAttribute
            extends UnboundAttribute<PermittedSubclassesAttribute>
            implements PermittedSubclassesAttribute {
        private final List<ClassEntry> permittedSubclasses;

        public UnboundPermittedSubclassesAttribute(List<ClassEntry> permittedSubclasses) {
            super(Attributes.PERMITTED_SUBCLASSES);
            this.permittedSubclasses = List.copyOf(permittedSubclasses);
        }

        @Override
        public List<ClassEntry> permittedSubclasses() {
            return permittedSubclasses;
        }
    }

    public static final class UnboundNestMembersAttribute
            extends UnboundAttribute<NestMembersAttribute>
            implements NestMembersAttribute {
        private final List<ClassEntry> memberEntries;

        public UnboundNestMembersAttribute(List<ClassEntry> memberEntries) {
            super(Attributes.NEST_MEMBERS);
            this.memberEntries = List.copyOf(memberEntries);
        }

        @Override
        public List<ClassEntry> nestMembers() {
            return memberEntries;
        }
    }

    public static final class UnboundNestHostAttribute
            extends UnboundAttribute<NestHostAttribute>
            implements NestHostAttribute {
        private final ClassEntry hostEntry;

        public UnboundNestHostAttribute(ClassEntry hostEntry) {
            super(Attributes.NEST_HOST);
            this.hostEntry = hostEntry;
        }

        @Override
        public ClassEntry nestHost() {
            return hostEntry;
        }
    }

    public static final class UnboundCompilationIDAttribute
            extends UnboundAttribute<CompilationIDAttribute>
            implements CompilationIDAttribute {
        private final Utf8Entry idEntry;

        public UnboundCompilationIDAttribute(Utf8Entry idEntry) {
            super(Attributes.COMPILATION_ID);
            this.idEntry = idEntry;
        }

        @Override
        public Utf8Entry compilationId() {
            return idEntry;
        }
    }

    public static final class UnboundSourceIDAttribute
            extends UnboundAttribute<SourceIDAttribute>
            implements SourceIDAttribute {
        private final Utf8Entry idEntry;

        public UnboundSourceIDAttribute(Utf8Entry idEntry) {
            super(Attributes.SOURCE_ID);
            this.idEntry = idEntry;
        }

        @Override
        public Utf8Entry sourceId() {
            return idEntry;
        }
    }

    public static final class UnboundSourceDebugExtensionAttribute
        extends UnboundAttribute<SourceDebugExtensionAttribute>
            implements SourceDebugExtensionAttribute {
        private final byte[] contents;

        public UnboundSourceDebugExtensionAttribute(byte[] contents) {
            super(Attributes.SOURCE_DEBUG_EXTENSION);
            this.contents = contents;
        }

        @Override
        public byte[] contents() {
            return contents;
        }
    }

    public static final class UnboundCharacterRangeTableAttribute
        extends UnboundAttribute<CharacterRangeTableAttribute>
            implements CharacterRangeTableAttribute {
        private final List<CharacterRangeInfo> ranges;

        public UnboundCharacterRangeTableAttribute(List<CharacterRangeInfo> ranges) {
            super(Attributes.CHARACTER_RANGE_TABLE);
            this.ranges = List.copyOf(ranges);
        }

        @Override
        public List<CharacterRangeInfo> characterRangeTable() {
            return ranges;
        }
    }

    public static final class UnboundLineNumberTableAttribute
        extends UnboundAttribute<LineNumberTableAttribute>
            implements LineNumberTableAttribute {
        private final List<LineNumberInfo> lines;

        public UnboundLineNumberTableAttribute(List<LineNumberInfo> lines) {
            super(Attributes.LINE_NUMBER_TABLE);
            this.lines = List.copyOf(lines);
        }

        @Override
        public List<LineNumberInfo> lineNumbers() {
            return lines;
        }
    }

    public static final class UnboundLocalVariableTableAttribute
        extends UnboundAttribute<LocalVariableTableAttribute>
            implements LocalVariableTableAttribute {
        private final List<LocalVariableInfo> locals;

        public UnboundLocalVariableTableAttribute(List<LocalVariableInfo> locals) {
            super(Attributes.LOCAL_VARIABLE_TABLE);
            this.locals = List.copyOf(locals);
        }

        @Override
        public List<LocalVariableInfo> localVariables() {
            return locals;
        }
    }

    public static final class UnboundLocalVariableTypeTableAttribute
        extends UnboundAttribute<LocalVariableTypeTableAttribute>
            implements LocalVariableTypeTableAttribute {
        private final List<LocalVariableTypeInfo> locals;

        public UnboundLocalVariableTypeTableAttribute(List<LocalVariableTypeInfo> locals) {
            super(Attributes.LOCAL_VARIABLE_TYPE_TABLE);
            this.locals = List.copyOf(locals);
        }

        @Override
        public List<LocalVariableTypeInfo> localVariableTypes() {
            return locals;
        }
    }

    public static final class UnboundRuntimeVisibleAnnotationsAttribute
            extends UnboundAttribute<RuntimeVisibleAnnotationsAttribute>
            implements RuntimeVisibleAnnotationsAttribute {
        private final List<Annotation> elements;

        public UnboundRuntimeVisibleAnnotationsAttribute(List<Annotation> elements) {
            super(Attributes.RUNTIME_VISIBLE_ANNOTATIONS);
            this.elements = List.copyOf(elements);
        }

        @Override
        public List<Annotation> annotations() {
            return elements;
        }
    }

    public static final class UnboundRuntimeInvisibleAnnotationsAttribute
            extends UnboundAttribute<RuntimeInvisibleAnnotationsAttribute>
            implements RuntimeInvisibleAnnotationsAttribute {
        private final List<Annotation> elements;

        public UnboundRuntimeInvisibleAnnotationsAttribute(List<Annotation> elements) {
            super(Attributes.RUNTIME_INVISIBLE_ANNOTATIONS);
            this.elements = List.copyOf(elements);
        }

        @Override
        public List<Annotation> annotations() {
            return elements;
        }
    }

    public static final class UnboundRuntimeVisibleParameterAnnotationsAttribute
            extends UnboundAttribute<RuntimeVisibleParameterAnnotationsAttribute>
            implements RuntimeVisibleParameterAnnotationsAttribute {
        private final List<List<Annotation>> elements;

        public UnboundRuntimeVisibleParameterAnnotationsAttribute(List<List<Annotation>> elements) {
            super(Attributes.RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS);
            this.elements = List.copyOf(elements);
        }

        @Override
        public List<List<Annotation>> parameterAnnotations() {
            return elements;
        }
    }

    public static final class UnboundRuntimeInvisibleParameterAnnotationsAttribute
            extends UnboundAttribute<RuntimeInvisibleParameterAnnotationsAttribute>
            implements RuntimeInvisibleParameterAnnotationsAttribute {
        private final List<List<Annotation>> elements;

        public UnboundRuntimeInvisibleParameterAnnotationsAttribute(List<List<Annotation>> elements) {
            super(Attributes.RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS);
            this.elements = List.copyOf(elements);
        }

        @Override
        public List<List<Annotation>> parameterAnnotations() {
            return elements;
        }
    }

    public static final class UnboundRuntimeVisibleTypeAnnotationsAttribute
            extends UnboundAttribute<RuntimeVisibleTypeAnnotationsAttribute>
            implements RuntimeVisibleTypeAnnotationsAttribute {
        private final List<TypeAnnotation> elements;

        public UnboundRuntimeVisibleTypeAnnotationsAttribute(List<TypeAnnotation> elements) {
            super(Attributes.RUNTIME_VISIBLE_TYPE_ANNOTATIONS);
            this.elements = List.copyOf(elements);
        }

        @Override
        public List<TypeAnnotation> annotations() {
            return elements;
        }
    }

    public static final class UnboundRuntimeInvisibleTypeAnnotationsAttribute
            extends UnboundAttribute<RuntimeInvisibleTypeAnnotationsAttribute>
            implements RuntimeInvisibleTypeAnnotationsAttribute {
        private final List<TypeAnnotation> elements;

        public UnboundRuntimeInvisibleTypeAnnotationsAttribute(List<TypeAnnotation> elements) {
            super(Attributes.RUNTIME_INVISIBLE_TYPE_ANNOTATIONS);
            this.elements = List.copyOf(elements);
        }

        @Override
        public List<TypeAnnotation> annotations() {
            return elements;
        }
    }

    public record UnboundCharacterRangeInfo(int startPc, int endPc,
                                            int characterRangeStart,
                                            int characterRangeEnd,
                                            int flags)
            implements CharacterRangeInfo { }

    public record UnboundInnerClassInfo(ClassEntry innerClass,
                                        Optional<ClassEntry> outerClass,
                                        Optional<Utf8Entry> innerName,
                                        int flagsMask)
            implements InnerClassInfo {}

    public record UnboundLineNumberInfo(int startPc, int lineNumber)
            implements LineNumberInfo { }

    public record UnboundLocalVariableInfo(int startPc, int length,
                                           Utf8Entry name,
                                           Utf8Entry type,
                                           int slot)
            implements LocalVariableInfo { }

    public record UnboundLocalVariableTypeInfo(int startPc, int length,
                                               Utf8Entry name,
                                               Utf8Entry signature,
                                               int slot)
            implements LocalVariableTypeInfo { }

    public record UnboundMethodParameterInfo(Optional<Utf8Entry> name, int flagsMask)
            implements MethodParameterInfo {}

    public record UnboundModuleExportInfo(PackageEntry exportedPackage,
                                          int exportsFlagsMask,
                                          List<ModuleEntry> exportsTo)
            implements ModuleExportInfo {
        public UnboundModuleExportInfo(PackageEntry exportedPackage, int exportsFlagsMask,
                                       List<ModuleEntry> exportsTo) {
            this.exportedPackage = exportedPackage;
            this.exportsFlagsMask = exportsFlagsMask;
            this.exportsTo = List.copyOf(exportsTo);
        }
    }

    public record UnboundModuleHashInfo(ModuleEntry moduleName,
                                        byte[] hash) implements ModuleHashInfo { }

    public record UnboundModuleOpenInfo(PackageEntry openedPackage, int opensFlagsMask,
                                        List<ModuleEntry> opensTo)
            implements ModuleOpenInfo {
        public UnboundModuleOpenInfo(PackageEntry openedPackage, int opensFlagsMask,
                                     List<ModuleEntry> opensTo) {
            this.openedPackage = openedPackage;
            this.opensFlagsMask = opensFlagsMask;
            this.opensTo = List.copyOf(opensTo);
        }
    }

    public record UnboundModuleProvideInfo(ClassEntry provides,
                                           List<ClassEntry> providesWith)
            implements ModuleProvideInfo {
        public UnboundModuleProvideInfo(ClassEntry provides, List<ClassEntry> providesWith) {
            this.provides = provides;
            this.providesWith = List.copyOf(providesWith);
        }
    }

    public record UnboundModuleRequiresInfo(ModuleEntry requires, int requiresFlagsMask,
                                            Optional<Utf8Entry> requiresVersion)
            implements ModuleRequireInfo {}

    public record UnboundRecordComponentInfo(Utf8Entry name,
                                             Utf8Entry descriptor,
                                             List<Attribute<?>> attributes)
            implements RecordComponentInfo {
        public UnboundRecordComponentInfo(Utf8Entry name, Utf8Entry descriptor, List<Attribute<?>> attributes) {
            this.name = name;
            this.descriptor = descriptor;
            this.attributes = List.copyOf(attributes);
        }
    }

    public record UnboundTypeAnnotation(TargetInfo targetInfo,
                                        List<TypePathComponent> targetPath,
                                        Utf8Entry className,
                                        List<AnnotationElement> elements) implements TypeAnnotation {

        public UnboundTypeAnnotation(TargetInfo targetInfo, List<TypePathComponent> targetPath,
                                     Utf8Entry className, List<AnnotationElement> elements) {
            this.targetInfo = targetInfo;
            this.targetPath = List.copyOf(targetPath);
            this.className = className;
            this.elements = List.copyOf(elements);
        }

        private int labelToBci(LabelContext lr, Label label) {
            //helper method to avoid NPE
            if (lr == null) throw new IllegalArgumentException("Illegal targetType '%s' in TypeAnnotation outside of Code attribute".formatted(targetInfo.targetType()));
            return lr.labelToBci(label);
        }

        @Override
        public void writeTo(BufWriter buf) {
            LabelContext lr = ((BufWriterImpl) buf).labelContext();
            // target_type
            buf.writeU1(targetInfo.targetType().targetTypeValue());

            // target_info
            switch (targetInfo) {
                case TypeParameterTarget tpt -> buf.writeU1(tpt.typeParameterIndex());
                case SupertypeTarget st -> buf.writeU2(st.supertypeIndex());
                case TypeParameterBoundTarget tpbt -> {
                    buf.writeU1(tpbt.typeParameterIndex());
                    buf.writeU1(tpbt.boundIndex());
                }
                case EmptyTarget et -> {
                    // nothing to write
                }
                case FormalParameterTarget fpt -> buf.writeU1(fpt.formalParameterIndex());
                case ThrowsTarget tt -> buf.writeU2(tt.throwsTargetIndex());
                case LocalVarTarget lvt -> {
                    buf.writeU2(lvt.table().size());
                    for (var e : lvt.table()) {
                        int startPc = labelToBci(lr, e.startLabel());
                        buf.writeU2(startPc);
                        buf.writeU2(labelToBci(lr, e.endLabel()) - startPc);
                        buf.writeU2(e.index());
                    }
                }
                case CatchTarget ct -> buf.writeU2(ct.exceptionTableIndex());
                case OffsetTarget ot -> buf.writeU2(labelToBci(lr, ot.target()));
                case TypeArgumentTarget tat -> {
                    buf.writeU2(labelToBci(lr, tat.target()));
                    buf.writeU1(tat.typeArgumentIndex());
                }
            }

            // target_path
            buf.writeU1(targetPath().size());
            for (TypePathComponent component : targetPath()) {
                buf.writeU1(component.typePathKind().tag());
                buf.writeU1(component.typeArgumentIndex());
            }

            // type_index
            buf.writeIndex(className);

            // element_value_pairs
            buf.writeU2(elements.size());
            for (AnnotationElement pair : elements()) {
                buf.writeIndex(pair.name());
                pair.value().writeTo(buf);
            }
        }
    }

    public record TypePathComponentImpl(TypeAnnotation.TypePathComponent.Kind typePathKind, int typeArgumentIndex)
            implements TypeAnnotation.TypePathComponent {}

    public static final class UnboundModuleAttribute extends UnboundAttribute<ModuleAttribute> implements ModuleAttribute {
        private final ModuleEntry moduleName;
        private final int moduleFlags;
        private final Utf8Entry moduleVersion;
        private final List<ModuleRequireInfo> requires;
        private final List<ModuleExportInfo> exports;
        private final List<ModuleOpenInfo> opens;
        private final List<ClassEntry> uses;
        private final List<ModuleProvideInfo> provides;

        public UnboundModuleAttribute(ModuleEntry moduleName,
                                      int moduleFlags,
                                      Utf8Entry moduleVersion,
                                      Collection<ModuleRequireInfo> requires,
                                      Collection<ModuleExportInfo> exports,
                                      Collection<ModuleOpenInfo> opens,
                                      Collection<ClassEntry> uses,
                                      Collection<ModuleProvideInfo> provides)
        {
            super(Attributes.MODULE);
            this.moduleName = moduleName;
            this.moduleFlags = moduleFlags;
            this.moduleVersion = moduleVersion;
            this.requires = List.copyOf(requires);
            this.exports = List.copyOf(exports);
            this.opens = List.copyOf(opens);
            this.uses = List.copyOf(uses);
            this.provides = List.copyOf(provides);
        }

        @Override
        public ModuleEntry moduleName() {
            return moduleName;
        }

        @Override
        public int moduleFlagsMask() {
            return moduleFlags;
        }

        @Override
        public Optional<Utf8Entry> moduleVersion() {
            return Optional.ofNullable(moduleVersion);
        }

        @Override
        public List<ModuleRequireInfo> requires() {
            return requires;
        }

        @Override
        public List<ModuleExportInfo> exports() {
            return exports;
        }

        @Override
        public List<ModuleOpenInfo> opens() {
            return opens;
        }

        @Override
        public List<ClassEntry> uses() {
            return uses;
        }

        @Override
        public List<ModuleProvideInfo> provides() {
            return provides;
        }
    }

    public static abstract non-sealed class AdHocAttribute<T extends Attribute<T>>
            extends UnboundAttribute<T> {

        public AdHocAttribute(AttributeMapper<T> mapper) {
            super(mapper);
        }

        public abstract void writeBody(BufWriter b);

        @Override
        public void writeTo(BufWriter b) {
            b.writeIndex(b.constantPool().utf8Entry(mapper.name()));
            b.writeInt(0);
            int start = b.size();
            writeBody(b);
            int written = b.size() - start;
            b.patchInt(start - 4, 4, written);
        }
    }

    public static final class EmptyBootstrapAttribute
            extends UnboundAttribute<BootstrapMethodsAttribute>
            implements BootstrapMethodsAttribute {
        public EmptyBootstrapAttribute() {
            super(Attributes.BOOTSTRAP_METHODS);
        }

        @Override
        public int bootstrapMethodsSize() {
            return 0;
        }

        @Override
        public List<BootstrapMethodEntry> bootstrapMethods() {
            return List.of();
        }
    }

    public static abstract sealed class CustomAttribute<T extends CustomAttribute<T>>
            extends UnboundAttribute<T>
            permits org.glavo.classfile.CustomAttribute {

        public CustomAttribute(AttributeMapper<T> mapper) {
            super(mapper);
        }
    }
}
