/*
 * Copyright (c) 2023, 2025, Oracle and/or its affiliates. All rights reserved.
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
package org.glavo.classfile.impl.verifier;

import org.glavo.classfile.*;
import org.glavo.classfile.attribute.*;
import org.glavo.classfile.constantpool.*;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import org.glavo.classfile.AccessFlag;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import org.glavo.classfile.impl.BoundAttribute;
import org.glavo.classfile.impl.Util;

import static java.lang.constant.ConstantDescs.*;
import static org.glavo.classfile.impl.StackMapGenerator.*;

/// ParserVerifier performs selected checks of the class file format according to
/// {@jvms 4.8 Format Checking}.
///
/// From `classFileParser.cpp`.
///
public record ParserVerifier(ClassModel classModel) {

    private static final String INIT_NAME = "<init>";
    private static final String CLASS_INIT_NAME = "<clinit>";

    List<VerifyError> verify() {
        var errors = new ArrayList<VerifyError>();
        verifyConstantPool(errors);
        verifyInterfaces(errors);
        verifyFields(errors);
        verifyMethods(errors);
        verifyAttributes(classModel, errors);
        return errors;
    }

    private void verifyConstantPool(List<VerifyError> errors) {
        for (var cpe : classModel.constantPool()) {
            try {
                if (cpe instanceof DoubleEntry de) {
                    de.doubleValue();
                } else if (cpe instanceof FloatEntry fe) {
                    fe.floatValue();
                } else if (cpe instanceof IntegerEntry ie) {
                    ie.intValue();
                } else if (cpe instanceof LongEntry le) {
                    le.longValue();
                } else if (cpe instanceof Utf8Entry ue) {
                    ue.stringValue();
                } else if (cpe instanceof ConstantDynamicEntry cde) {
                    cde.asSymbol();
                } else if (cpe instanceof InvokeDynamicEntry ide) {
                    ide.asSymbol();
                } else if (cpe instanceof ClassEntry ce) {
                    ce.asSymbol();
                } else if (cpe instanceof StringEntry se) {
                    se.stringValue();
                } else if (cpe instanceof MethodHandleEntry mhe) {
                    mhe.asSymbol();
                } else if (cpe instanceof MethodTypeEntry mte) {
                    mte.asSymbol();
                } else if (cpe instanceof FieldRefEntry fre) {
                        try {
                            fre.owner().asSymbol();
                        } catch (VerifyError|Exception e) {
                            errors.add(cpeVerifyError(cpe, e));
                        }
                        try {
                            fre.typeSymbol();
                        } catch (VerifyError|Exception e) {
                            errors.add(cpeVerifyError(cpe, e));
                        }
                        verifyFieldName(fre.name().stringValue());
                } else if (cpe instanceof InterfaceMethodRefEntry imre) {
                        try {
                            imre.owner().asSymbol();
                        } catch (VerifyError|Exception e) {
                            errors.add(cpeVerifyError(cpe, e));
                        }
                        try {
                            imre.typeSymbol();
                        } catch (VerifyError|Exception e) {
                            errors.add(cpeVerifyError(cpe, e));
                        }
                        verifyMethodName(imre.name().stringValue());
                } else if (cpe instanceof MethodRefEntry mre) {
                        try {
                            mre.owner().asSymbol();
                        } catch (VerifyError|Exception e) {
                            errors.add(cpeVerifyError(cpe, e));
                        }
                        try {
                            mre.typeSymbol();
                        } catch (VerifyError|Exception e) {
                            errors.add(cpeVerifyError(cpe, e));
                        }
                        verifyMethodName(mre.name().stringValue());
                } else if (cpe instanceof ModuleEntry me) {
                    me.asSymbol();
                } else if (cpe instanceof NameAndTypeEntry nate) {
                        try {
                            nate.name().stringValue();
                        } catch (VerifyError|Exception e) {
                            errors.add(cpeVerifyError(cpe, e));
                        }
                        nate.type().stringValue();
                } else if (cpe instanceof PackageEntry pe) {
                    pe.asSymbol();
                }
            } catch (VerifyError|Exception e) {
                errors.add(cpeVerifyError(cpe, e));
            }
        }
    }

    private VerifyError cpeVerifyError(final PoolEntry cpe, final Throwable e) {
        return new VerifyError("%s at constant pool index %d in %s".formatted(e.getMessage(), cpe.index(), toString(classModel)));
    }

    private void verifyFieldName(String name) {
        if (name.length() == 0 || name.chars().anyMatch(ch -> switch(ch) {
                    case '.', ';', '[', '/' -> true;
                    default -> false;
                })) {
              throw new VerifyError("Illegal field name %s in %s".formatted(name, toString(classModel)));
        }
    }

    private void verifyMethodName(String name) {
        if (!name.equals(INIT_NAME)
            && !name.equals(CLASS_INIT_NAME)
            && (name.length() == 0 || name.chars().anyMatch(ch -> switch(ch) {
                    case '.', ';', '[', '/', '<', '>' -> true;
                    default -> false;
                }))) {
              throw new VerifyError("Illegal method name %s in %s".formatted(name, toString(classModel)));
        }
    }

    private void verifyInterfaces(List<VerifyError> errors) {
        var intfs = new HashSet<ClassEntry>();
        for (var intf : classModel.interfaces()) {
            if (!intfs.add(intf)) {
                errors.add(new VerifyError("Duplicate interface %s in %s".formatted(intf.asSymbol().displayName(), toString(classModel))));
            }
        }
    }

    private void verifyFields(List<VerifyError> errors) {
        record F(Utf8Entry name, Utf8Entry type) {};
        var fields = new HashSet<F>();
        for (var f : classModel.fields()) try {
            if (!fields.add(new F(f.fieldName(), f.fieldType()))) {
                errors.add(new VerifyError("Duplicate field name %s with signature %s in %s".formatted(f.fieldName().stringValue(), f.fieldType().stringValue(), toString(classModel))));
            }
            verifyFieldName(f.fieldName().stringValue());
        } catch (VerifyError ve) {
            errors.add(ve);
        }
    }

    private void verifyMethods(List<VerifyError> errors) {
        record M(Utf8Entry name, Utf8Entry type) {};
        var methods = new HashSet<M>();
        for (var m : classModel.methods()) try {
            if (!methods.add(new M(m.methodName(), m.methodType()))) {
                errors.add(new VerifyError("Duplicate method name %s with signature %s in %s".formatted(m.methodName().stringValue(), m.methodType().stringValue(), toString(classModel))));
            }
            if (m.methodName().equalsString(CLASS_INIT_NAME)
                    && !m.flags().has(AccessFlag.STATIC)) {
                errors.add(new VerifyError("Method <clinit> is not static in %s".formatted(toString(classModel))));
            }
            if (classModel.flags().has(AccessFlag.INTERFACE)
                    && m.methodName().equalsString(INIT_NAME)) {
                errors.add(new VerifyError("Interface cannot have a method named <init> in %s".formatted(toString(classModel))));
            }
            verifyMethodName(m.methodName().stringValue());
        } catch (VerifyError ve) {
            errors.add(ve);
        }
    }

    private void verifyAttributes(ClassFileElement cfe, List<VerifyError> errors) {
        if (cfe instanceof AttributedElement ae) {
            var attrNames = new HashSet<String>();
            for (var a : ae.attributes()) {
                if (!a.attributeMapper().allowMultiple() && !attrNames.add(a.attributeName().stringValue())) {
                    errors.add(new VerifyError("Multiple %s attributes in %s".formatted(a.attributeName().stringValue(), toString(ae))));
                }
                verifyAttribute(ae, a, errors);
            }
        }
        if (cfe instanceof CompoundElement<?> comp) {
            for (var e : comp) verifyAttributes(e, errors);
        } else if (cfe instanceof RecordAttribute ra) {
            for(var rc : ra.components()) verifyAttributes(rc, errors);
        }
    }

    private void verifyAttribute(AttributedElement ae, Attribute<?> a, List<VerifyError> errors) {
        int size;
        if (a instanceof AnnotationDefaultAttribute aa) {
            size = valueSize(aa.defaultValue());
        } else if (a instanceof BootstrapMethodsAttribute bma) {
            size = 2 + bma.bootstrapMethods().stream().mapToInt(bm -> 4 + 2 * bm.arguments().size()).sum();
        } else if (a instanceof CharacterRangeTableAttribute cra) {
            size = 2 + 14 * cra.characterRangeTable().size();
        } else if (a instanceof CodeAttribute ca) {
            MethodModel mm = (MethodModel) ae;
            if (mm.flags().has(AccessFlag.NATIVE) || mm.flags().has(AccessFlag.ABSTRACT)) {
                errors.add(new VerifyError("Code attribute in native or abstract %s".formatted(toString(ae))));
            }
            if (ca.maxLocals() < Util.maxLocals(mm.flags().flagsMask(), mm.methodTypeSymbol())) {
                errors.add(new VerifyError("Arguments can't fit into locals in %s".formatted(toString(ae))));
            }
            size = 10 + ca.codeLength() + 8 * ca.exceptionHandlers().size() + attributesSize(ca.attributes());
        } else if (a instanceof CompilationIDAttribute cida) {
            cida.compilationId();
            size = 2;
        } else if (a instanceof ConstantValueAttribute cva) {
            ClassDesc type = ((FieldModel) ae).fieldTypeSymbol();
            ConstantValueEntry cve = cva.constant();
            if (!switch (TypeKind.from(type)) {
                case BOOLEAN, BYTE, CHAR, INT, SHORT -> cve instanceof IntegerEntry;
                case DOUBLE -> cve instanceof DoubleEntry;
                case FLOAT -> cve instanceof FloatEntry;
                case LONG -> cve instanceof LongEntry;
                case REFERENCE -> type.equals(ConstantDescs.CD_String) && cve instanceof StringEntry;
                case VOID -> false;
            }) {
                errors.add(new VerifyError("Bad constant value type in %s".formatted(toString(ae))));
            }
            size = 2;
        } else if (a instanceof DeprecatedAttribute) {
            size = 0;
        } else if (a instanceof EnclosingMethodAttribute ema) {
            ema.enclosingClass();
            ema.enclosingMethod();
            size = 4;
        } else if (a instanceof ExceptionsAttribute ea) {
            size = 2 + 2 * ea.exceptions().size();
        } else if (a instanceof InnerClassesAttribute ica) {
            for (var ici : ica.classes()) {
                if (ici.outerClass().isPresent() && ici.outerClass().get().equals(ici.innerClass())) {
                    errors.add(new VerifyError("Class is both outer and inner class in %s".formatted(toString(ae))));
                }
            }
            size = 2 + 8 * ica.classes().size();
        } else if (a instanceof LineNumberTableAttribute lta) {
            size = 2 + 4 * lta.lineNumbers().size();
        } else if (a instanceof LoadableDescriptorsAttribute lda) {
            for (var desc : lda.loadableDescriptorSymbols()) {
                if (desc.equals(CD_void)) {
                    errors.add(new VerifyError("illegal signature %s".formatted(desc)));
                }
            }
            size = 2 + 2 * lda.loadableDescriptors().size();
        } else if (a instanceof LocalVariableTableAttribute lvta) {
            size = 2 + 10 * lvta.localVariables().size();
        } else if (a instanceof LocalVariableTypeTableAttribute lvta) {
            size = 2 + 10 * lvta.localVariableTypes().size();
        } else if (a instanceof MethodParametersAttribute mpa) {
            size = 1 + 4 * mpa.parameters().size();
        } else if (a instanceof ModuleAttribute ma) {
            size = 16 + subSize(ma.exports(), ModuleExportInfo::exportsTo, 6, 2)
                    + subSize(ma.opens(), ModuleOpenInfo::opensTo, 6, 2)
                    + subSize(ma.provides(), ModuleProvideInfo::providesWith, 4, 2)
                    + 6 * ma.requires().size()
                    + 2 * ma.uses().size();
        } else if (a instanceof ModuleHashesAttribute mha) {
            size = 2 + moduleHashesSize(mha.hashes());
        } else if (a instanceof ModuleMainClassAttribute mmca) {
            mmca.mainClass();
            size = 2;
        } else if (a instanceof ModulePackagesAttribute mpa) {
            size = 2 + 2 * mpa.packages().size();
        } else if (a instanceof ModuleResolutionAttribute) {
            size = 2;
        } else if (a instanceof ModuleTargetAttribute mta) {
            mta.targetPlatform();
            size = 2;
        } else if (a instanceof NestHostAttribute nha) {
            nha.nestHost();
            size = 2;
        } else if (a instanceof NestMembersAttribute nma) {
            if (ae.findAttribute(Attributes.nestHost()).isPresent()) {
                errors.add(new VerifyError("Conflicting NestHost and NestMembers attributes in %s".formatted(toString(ae))));
            }
            size = 2 + 2 * nma.nestMembers().size();
        } else if (a instanceof PermittedSubclassesAttribute psa) {
            if (classModel.flags().has(AccessFlag.FINAL)) {
                errors.add(new VerifyError("PermittedSubclasses attribute in final %s".formatted(toString(ae))));
            }
            size = 2 + 2 * psa.permittedSubclasses().size();
        } else if (a instanceof RecordAttribute ra) {
            size = componentsSize(ra.components());
        } else if (a instanceof RuntimeVisibleAnnotationsAttribute aa) {
            size = annotationsSize(aa.annotations());
        } else if (a instanceof RuntimeInvisibleAnnotationsAttribute aa) {
            size = annotationsSize(aa.annotations());
        } else if (a instanceof RuntimeVisibleTypeAnnotationsAttribute aa) {
            size = typeAnnotationsSize(aa.annotations());
        } else if (a instanceof RuntimeInvisibleTypeAnnotationsAttribute aa) {
            size = typeAnnotationsSize(aa.annotations());
        } else if (a instanceof RuntimeVisibleParameterAnnotationsAttribute aa) {
            size = parameterAnnotationsSize(aa.parameterAnnotations());
        } else if (a instanceof RuntimeInvisibleParameterAnnotationsAttribute aa) {
            size = parameterAnnotationsSize(aa.parameterAnnotations());
        } else if (a instanceof SignatureAttribute sa) {
            sa.signature();
            size = 2;
        } else if (a instanceof SourceDebugExtensionAttribute sda) {
            size = sda.contents().length;
        } else if (a instanceof SourceFileAttribute sfa) {
            sfa.sourceFile();
            size = 2;
        } else if (a instanceof SourceIDAttribute sida) {
            sida.sourceId();
            size = 2;
        } else if (a instanceof StackMapTableAttribute) {
            size = -1; // Not sufficient info for assert unset size
        } else if (a instanceof SyntheticAttribute) {
            size = 0;
        } else if (a instanceof UnknownAttribute || a instanceof CustomAttribute<?>) {
            size = -1;
        } else {
            throw new AssertionError(a);
        }
        if (size >= 0 && size != ((BoundAttribute)a).payloadLen()) {
            errors.add(new VerifyError("Wrong %s attribute length in %s".formatted(a.attributeName().stringValue(), toString(ae))));
        }
    }

    private static <T, S extends Collection<?>> int subSize(Collection<T> entries, Function<T, S> subMH, int entrySize, int subSize) {
        return subSize(entries, (ToIntFunction<T>) t -> entrySize + subSize * subMH.apply(t).size());
    }

    private static <T> int subSize(Collection<T> entries, ToIntFunction<T> subMH) {
        int l = 0;
        for (T entry : entries) {
            l += subMH.applyAsInt(entry);
        }
        return l;
    }

    private static int componentsSize(List<RecordComponentInfo> comps) {
        int l = 2;
        for (var rc : comps) {
            l += 4 + attributesSize(rc.attributes());
        }
        return l;
    }

    private static int attributesSize(List<Attribute<?>> attrs) {
        int l = 2;
        for (var a : attrs) {
            l += 6 + ((BoundAttribute)a).payloadLen();
        }
        return l;
    }

    private static int parameterAnnotationsSize(List<List<Annotation>> pans) {
        int l = 1;
        for (var ans : pans) {
            l += annotationsSize(ans);
        }
        return l;
    }

    private static int annotationsSize(List<Annotation> ans) {
        int l = 2;
        for (var an : ans) {
            l += annotationSize(an);
        }
        return l;
    }

    private static int typeAnnotationsSize(List<TypeAnnotation> ans) {
        int l = 2;
        for (var an : ans) {
            l += 2 + an.targetInfo().size() + 2 * an.targetPath().size() + annotationSize(an.annotation());
        }
        return l;
    }

    private static int annotationSize(Annotation an) {
        int l = 4;
        for (var el : an.elements()) {
            l += 2 + valueSize(el.value());
        }
        return l;
    }

    private static int valueSize(AnnotationValue val) {
        int size;
        if (val instanceof AnnotationValue.OfAnnotation oan) {
            size = annotationSize(oan.annotation());
        } else if (val instanceof AnnotationValue.OfArray oar) {
            size = 2;
            for (var v : oar.values()) {
                size += valueSize(v);
            }
        } else if (val instanceof AnnotationValue.OfConstant || val instanceof AnnotationValue.OfClass) {
            size = 2;
        } else if (val instanceof AnnotationValue.OfEnum) {
            size = 4;
        } else {
            throw new AssertionError(val);
        }
        return 1 + size;
    }

    private static int moduleHashesSize(List<ModuleHashInfo> hashes) {
        int l = 2;
        for (var h : hashes) {
            h.moduleName();
            l += 4 + h.hash().length;
        }
        return l;
    }

    private int stackMapFrameSize(StackMapFrameInfo frame) {
        int ft = frame.frameType();
        if (ft <= SAME_FRAME_END) return 1;
        if (ft <= SAME_LOCALS_1_STACK_ITEM_FRAME_END) return 1 + verificationTypeSize(frame.stack().get(0));
        if (ft > RESERVED_END) {
            if (ft == SAME_LOCALS_1_STACK_ITEM_EXTENDED) return 3 + verificationTypeSize(frame.stack().get(0));
            if (ft <= SAME_FRAME_EXTENDED) return 3;
            if (ft <= APPEND_FRAME_END) {
                var loc = frame.locals();
                int l = 3;
                var k = ft - APPEND_FRAME_START + 1;
                for (int i = loc.size() - k; i < loc.size(); i++) {
                    l += verificationTypeSize(loc.get(i));
                }
                return l;
            }
            if (ft == FULL_FRAME) {
                int l = 7;
                for (var vt : frame.stack()) {
                    l += verificationTypeSize(vt);
                }
                for (var vt : frame.locals()) {
                    l += verificationTypeSize(vt);
                }
                return l;
            }
        }
        throw new IllegalArgumentException("Invalid stack map frame type " + ft);
    }

    private static int verificationTypeSize(StackMapFrameInfo.VerificationTypeInfo vti) {
        if (vti instanceof StackMapFrameInfo.SimpleVerificationTypeInfo) {
            return 1;
        } else if (vti instanceof StackMapFrameInfo.ObjectVerificationTypeInfo ovti) {
            ovti.classSymbol();
            return 3;
        } else if (vti instanceof StackMapFrameInfo.UninitializedVerificationTypeInfo) {
            return 3;
        }
        throw new AssertionError(vti);
    }

    private String className() {
        return classModel.thisClass().asSymbol().displayName();
    }

    private String toString(AttributedElement ae) {
        if (ae instanceof CodeModel m) {
            return "Code attribute for " + toString(m.parent().get());
        } else if (ae instanceof FieldModel m) {
            return "field %s.%s".formatted(
                    className(),
                    m.fieldName().stringValue());
        } else if (ae instanceof MethodModel m) {
            return "method %s::%s(%s)".formatted(
                    className(),
                    m.methodName().stringValue(),
                    m.methodTypeSymbol().parameterList().stream().map(ClassDesc::displayName).collect(Collectors.joining(",")));
        } else if (ae instanceof RecordComponentInfo i) {
            return "Record component %s of class %s".formatted(
                    i.name().stringValue(),
                    className());
        }
        return "class " + className();
    }
}
