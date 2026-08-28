/*
 * Copyright (c) 2023, 2026, Oracle and/or its affiliates. All rights reserved.
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

import org.glavo.classfile.*;
import org.glavo.classfile.attribute.*;
import org.glavo.classfile.components.ClassRemapper;
import org.glavo.classfile.constantpool.Utf8Entry;
import org.glavo.classfile.instruction.ConstantInstruction.LoadConstantInstruction;
import org.glavo.classfile.instruction.*;
import java.lang.constant.*;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public record ClassRemapperImpl(Function<ClassDesc, ClassDesc> mapFunction) implements ClassRemapper {

    @Override
    public void accept(ClassBuilder clb, ClassElement cle) {
        Objects.requireNonNull(cle);
        if (cle instanceof FieldModel fm) {
            clb.withField(fm.fieldName().stringValue(), map(fm.fieldTypeSymbol()),
                    fb -> fb.transform(fm, asFieldTransform()));
        } else if (cle instanceof MethodModel mm) {
            clb.withMethod(mm.methodName().stringValue(), mapMethodDesc(mm.methodTypeSymbol()),
                    mm.flags().flagsMask(), mb -> mb.transform(mm, asMethodTransform()));
        } else if (cle instanceof Superclass sc) {
            clb.withSuperclass(map(sc.superclassEntry().asSymbol()));
        } else if (cle instanceof Interfaces interfaces) {
            clb.withInterfaceSymbols(Util.mappedList(interfaces.interfaces(), in -> map(in.asSymbol())));
        } else if (cle instanceof SignatureAttribute signature) {
            clb.with(SignatureAttribute.of(mapClassSignature(signature.asClassSignature())));
        } else if (cle instanceof InnerClassesAttribute innerClasses) {
            clb.with(InnerClassesAttribute.of(innerClasses.classes().stream().map(ici ->
                    InnerClassInfo.of(map(ici.innerClass().asSymbol()),
                            ici.outerClass().map(oc -> map(oc.asSymbol())),
                            ici.innerName().map(Utf8Entry::stringValue),
                            ici.flagsMask())).toList()));
        } else if (cle instanceof EnclosingMethodAttribute enclosingMethod) {
            clb.with(EnclosingMethodAttribute.of(map(enclosingMethod.enclosingClass().asSymbol()),
                    enclosingMethod.enclosingMethodName().map(Utf8Entry::stringValue),
                    enclosingMethod.enclosingMethodTypeSymbol().map(this::mapMethodDesc)));
        } else if (cle instanceof RecordAttribute record) {
            clb.with(RecordAttribute.of(record.components().stream().map(this::mapRecordComponent).toList()));
        } else if (cle instanceof ModuleAttribute module) {
            clb.with(ModuleAttribute.of(module.moduleName(), module.moduleFlagsMask(),
                    module.moduleVersion().orElse(null),
                    module.requires(), module.exports(), module.opens(),
                    module.uses().stream().map(ce ->
                            clb.constantPool().classEntry(map(ce.asSymbol()))).toList(),
                    module.provides().stream().map(mp ->
                            ModuleProvideInfo.of(map(mp.provides().asSymbol()),
                                    mp.providesWith().stream().map(pw -> map(pw.asSymbol())).toList())).toList()));
        } else if (cle instanceof NestHostAttribute nestHost) {
            clb.with(NestHostAttribute.of(map(nestHost.nestHost().asSymbol())));
        } else if (cle instanceof NestMembersAttribute nestMembers) {
            clb.with(NestMembersAttribute.ofSymbols(nestMembers.nestMembers().stream()
                    .map(nm -> map(nm.asSymbol())).toList()));
        } else if (cle instanceof PermittedSubclassesAttribute permittedSubclasses) {
            clb.with(PermittedSubclassesAttribute.ofSymbols(permittedSubclasses.permittedSubclasses().stream()
                    .map(ps -> map(ps.asSymbol())).toList()));
        } else if (cle instanceof LoadableDescriptorsAttribute loadableDescriptors) {
            clb.with(LoadableDescriptorsAttribute.ofSymbols(loadableDescriptors.loadableDescriptorSymbols().stream()
                    .map(this::map).toList()));
        } else if (cle instanceof RuntimeVisibleAnnotationsAttribute annotations) {
            clb.with(RuntimeVisibleAnnotationsAttribute.of(mapAnnotations(annotations.annotations())));
        } else if (cle instanceof RuntimeInvisibleAnnotationsAttribute annotations) {
            clb.with(RuntimeInvisibleAnnotationsAttribute.of(mapAnnotations(annotations.annotations())));
        } else if (cle instanceof RuntimeVisibleTypeAnnotationsAttribute annotations) {
            clb.with(RuntimeVisibleTypeAnnotationsAttribute.of(mapTypeAnnotations(annotations.annotations())));
        } else if (cle instanceof RuntimeInvisibleTypeAnnotationsAttribute annotations) {
            clb.with(RuntimeInvisibleTypeAnnotationsAttribute.of(mapTypeAnnotations(annotations.annotations())));
        } else {
            clb.with(cle);
        }
    }

    @Override
    public FieldTransform asFieldTransform() {
        return (FieldBuilder fb, FieldElement fe) -> {
            Objects.requireNonNull(fe);
            if (fe instanceof SignatureAttribute signature) {
                fb.with(SignatureAttribute.of(mapSignature(signature.asTypeSignature())));
            } else if (fe instanceof RuntimeVisibleAnnotationsAttribute annotations) {
                fb.with(RuntimeVisibleAnnotationsAttribute.of(mapAnnotations(annotations.annotations())));
            } else if (fe instanceof RuntimeInvisibleAnnotationsAttribute annotations) {
                fb.with(RuntimeInvisibleAnnotationsAttribute.of(mapAnnotations(annotations.annotations())));
            } else if (fe instanceof RuntimeVisibleTypeAnnotationsAttribute annotations) {
                fb.with(RuntimeVisibleTypeAnnotationsAttribute.of(mapTypeAnnotations(annotations.annotations())));
            } else if (fe instanceof RuntimeInvisibleTypeAnnotationsAttribute annotations) {
                fb.with(RuntimeInvisibleTypeAnnotationsAttribute.of(mapTypeAnnotations(annotations.annotations())));
            } else {
                fb.with(fe);
            }
        };
    }

    @Override
    public MethodTransform asMethodTransform() {
        return (MethodBuilder mb, MethodElement me) -> {
            Objects.requireNonNull(me);
            if (me instanceof AnnotationDefaultAttribute annotationDefault) {
                mb.with(AnnotationDefaultAttribute.of(mapAnnotationValue(annotationDefault.defaultValue())));
            } else if (me instanceof CodeModel code) {
                mb.transformCode(code, asCodeTransform());
            } else if (me instanceof ExceptionsAttribute exceptions) {
                mb.with(ExceptionsAttribute.ofSymbols(exceptions.exceptions().stream()
                        .map(ce -> map(ce.asSymbol())).toList()));
            } else if (me instanceof SignatureAttribute signature) {
                mb.with(SignatureAttribute.of(mapMethodSignature(signature.asMethodSignature())));
            } else if (me instanceof RuntimeVisibleAnnotationsAttribute annotations) {
                mb.with(RuntimeVisibleAnnotationsAttribute.of(mapAnnotations(annotations.annotations())));
            } else if (me instanceof RuntimeInvisibleAnnotationsAttribute annotations) {
                mb.with(RuntimeInvisibleAnnotationsAttribute.of(mapAnnotations(annotations.annotations())));
            } else if (me instanceof RuntimeVisibleParameterAnnotationsAttribute annotations) {
                mb.with(RuntimeVisibleParameterAnnotationsAttribute.of(annotations.parameterAnnotations().stream()
                        .map(this::mapAnnotations).toList()));
            } else if (me instanceof RuntimeInvisibleParameterAnnotationsAttribute annotations) {
                mb.with(RuntimeInvisibleParameterAnnotationsAttribute.of(annotations.parameterAnnotations().stream()
                        .map(this::mapAnnotations).toList()));
            } else if (me instanceof RuntimeVisibleTypeAnnotationsAttribute annotations) {
                mb.with(RuntimeVisibleTypeAnnotationsAttribute.of(mapTypeAnnotations(annotations.annotations())));
            } else if (me instanceof RuntimeInvisibleTypeAnnotationsAttribute annotations) {
                mb.with(RuntimeInvisibleTypeAnnotationsAttribute.of(mapTypeAnnotations(annotations.annotations())));
            } else {
                mb.with(me);
            }
        };
    }

    @Override
    public CodeTransform asCodeTransform() {
        return (CodeBuilder cob, CodeElement coe) -> {
            Objects.requireNonNull(coe);
            if (coe instanceof FieldInstruction field) {
                cob.fieldAccess(field.opcode(), map(field.owner().asSymbol()),
                        field.name().stringValue(), map(field.typeSymbol()));
            } else if (coe instanceof InvokeInstruction invoke) {
                cob.invoke(invoke.opcode(), map(invoke.owner().asSymbol()),
                        invoke.name().stringValue(), mapMethodDesc(invoke.typeSymbol()), invoke.isInterface());
            } else if (coe instanceof InvokeDynamicInstruction invokeDynamic) {
                cob.invokedynamic(DynamicCallSiteDesc.of(
                        mapDirectMethodHandle(invokeDynamic.bootstrapMethod()),
                        invokeDynamic.name().stringValue(),
                        mapMethodDesc(invokeDynamic.typeSymbol()),
                        invokeDynamic.bootstrapArgs().stream().map(this::mapConstantValue)
                                .toArray(ConstantDesc[]::new)));
            } else if (coe instanceof NewObjectInstruction newObject) {
                cob.new_(map(newObject.className().asSymbol()));
            } else if (coe instanceof NewReferenceArrayInstruction newArray) {
                cob.anewarray(map(newArray.componentType().asSymbol()));
            } else if (coe instanceof NewMultiArrayInstruction newMultiArray) {
                cob.multianewarray(map(newMultiArray.arrayType().asSymbol()), newMultiArray.dimensions());
            } else if (coe instanceof TypeCheckInstruction typeCheck) {
                cob.with(TypeCheckInstruction.of(typeCheck.opcode(), map(typeCheck.type().asSymbol())));
            } else if (coe instanceof ExceptionCatch exceptionCatch) {
                cob.exceptionCatch(exceptionCatch.tryStart(), exceptionCatch.tryEnd(), exceptionCatch.handler(),
                        exceptionCatch.catchType().map(d ->
                                TemporaryConstantPool.INSTANCE.classEntry(map(d.asSymbol()))));
            } else if (coe instanceof LocalVariable local) {
                cob.localVariable(local.slot(), local.name().stringValue(), map(local.typeSymbol()),
                        local.startScope(), local.endScope());
            } else if (coe instanceof LocalVariableType localType) {
                cob.localVariableType(localType.slot(), localType.name().stringValue(),
                        mapSignature(localType.signatureSymbol()), localType.startScope(), localType.endScope());
            } else if (coe instanceof LoadConstantInstruction loadConstant) {
                cob.ldc(mapConstantValue(loadConstant.constantValue()));
            } else if (coe instanceof RuntimeVisibleTypeAnnotationsAttribute annotations) {
                cob.with(RuntimeVisibleTypeAnnotationsAttribute.of(mapTypeAnnotations(annotations.annotations())));
            } else if (coe instanceof RuntimeInvisibleTypeAnnotationsAttribute annotations) {
                cob.with(RuntimeInvisibleTypeAnnotationsAttribute.of(mapTypeAnnotations(annotations.annotations())));
            } else {
                cob.with(coe);
            }
        };
    }

    @Override
    public ClassDesc map(ClassDesc desc) {
        if (desc == null) return null;
        if (desc.isArray()) return map(desc.componentType()).arrayType();
        if (desc.isPrimitive()) return desc;
        return mapFunction.apply(desc);
    }

    MethodTypeDesc mapMethodDesc(MethodTypeDesc desc) {
        return MethodTypeDesc.of(map(desc.returnType()),
                desc.parameterList().stream().map(this::map).toArray(ClassDesc[]::new));
    }

    ClassSignature mapClassSignature(ClassSignature signature) {
        return ClassSignature.of(mapTypeParams(signature.typeParameters()),
                mapSignature(signature.superclassSignature()),
                signature.superinterfaceSignatures().stream()
                        .map(this::mapSignature).toArray(Signature.ClassTypeSig[]::new));
    }

    MethodSignature mapMethodSignature(MethodSignature signature) {
        return MethodSignature.of(mapTypeParams(signature.typeParameters()),
                signature.throwableSignatures().stream().map(this::mapSignature).toList(),
                mapSignature(signature.result()),
                signature.arguments().stream()
                        .map(this::mapSignature).toArray(Signature[]::new));
    }

    RecordComponentInfo mapRecordComponent(RecordComponentInfo component) {
        return RecordComponentInfo.of(component.name().stringValue(),
                map(component.descriptorSymbol()),
                component.attributes().stream().map(this::mapRecordComponentAttribute).toList());
    }

    private Attribute<?> mapRecordComponentAttribute(Attribute<?> attribute) {
        if (attribute instanceof SignatureAttribute signature) {
            return SignatureAttribute.of(mapSignature(signature.asTypeSignature()));
        } else if (attribute instanceof RuntimeVisibleAnnotationsAttribute annotations) {
            return RuntimeVisibleAnnotationsAttribute.of(mapAnnotations(annotations.annotations()));
        } else if (attribute instanceof RuntimeInvisibleAnnotationsAttribute annotations) {
            return RuntimeInvisibleAnnotationsAttribute.of(mapAnnotations(annotations.annotations()));
        } else if (attribute instanceof RuntimeVisibleTypeAnnotationsAttribute annotations) {
            return RuntimeVisibleTypeAnnotationsAttribute.of(mapTypeAnnotations(annotations.annotations()));
        } else if (attribute instanceof RuntimeInvisibleTypeAnnotationsAttribute annotations) {
            return RuntimeInvisibleTypeAnnotationsAttribute.of(mapTypeAnnotations(annotations.annotations()));
        }
        return attribute;
    }

    DirectMethodHandleDesc mapDirectMethodHandle(DirectMethodHandleDesc dmhd) {
        return switch (dmhd.kind()) {
            case GETTER, SETTER, STATIC_GETTER, STATIC_SETTER ->
                MethodHandleDesc.ofField(dmhd.kind(), map(dmhd.owner()),
                        dmhd.methodName(),
                        map(ClassDesc.ofDescriptor(dmhd.lookupDescriptor())));
            default ->
                MethodHandleDesc.ofMethod(dmhd.kind(), map(dmhd.owner()),
                        dmhd.methodName(),
                        mapMethodDesc(MethodTypeDesc.ofDescriptor(dmhd.lookupDescriptor())));
        };
    }

    ConstantDesc mapConstantValue(ConstantDesc value) {
        Objects.requireNonNull(value);
        if (value instanceof ClassDesc classDesc) {
            return map(classDesc);
        } else if (value instanceof DynamicConstantDesc<?> dynamicConstant) {
            return mapDynamicConstant(dynamicConstant);
        } else if (value instanceof DirectMethodHandleDesc methodHandle) {
            return mapDirectMethodHandle(methodHandle);
        } else if (value instanceof MethodTypeDesc methodType) {
            return mapMethodDesc(methodType);
        }
        return value;
    }

    DynamicConstantDesc<?> mapDynamicConstant(DynamicConstantDesc<?> dcd) {
        return DynamicConstantDesc.ofNamed(mapDirectMethodHandle(dcd.bootstrapMethod()),
                dcd.constantName(),
                map(dcd.constantType()),
                dcd.bootstrapArgsList().stream().map(this::mapConstantValue).toArray(ConstantDesc[]::new));
    }

    @SuppressWarnings("unchecked")
    <S extends Signature> S mapSignature(S signature) {
        Objects.requireNonNull(signature);
        if (signature instanceof Signature.ArrayTypeSig array) {
            return (S) Signature.ArrayTypeSig.of(mapSignature(array.componentSignature()));
        } else if (signature instanceof Signature.ClassTypeSig classType) {
            return (S) Signature.ClassTypeSig.of(
                    classType.outerType().map(this::mapSignature).orElse(null),
                    Util.toInternalName(map(classType.classDesc())),
                    classType.typeArgs().stream().map(this::mapTypeArgument)
                            .toArray(Signature.TypeArg[]::new));
        }
        return signature;
    }

    private Signature.TypeArg mapTypeArgument(Signature.TypeArg argument) {
        if (argument instanceof Signature.TypeArg.Unbounded) {
            return argument;
        }
        var bounded = (Signature.TypeArg.Bounded) argument;
        return Signature.TypeArg.bounded(bounded.wildcardIndicator(), mapSignature(bounded.boundType()));
    }

    List<Annotation> mapAnnotations(List<Annotation> annotations) {
        return annotations.stream().map(this::mapAnnotation).toList();
    }

    Annotation mapAnnotation(Annotation a) {
        return Annotation.of(map(a.classSymbol()), a.elements().stream().map(el ->
                AnnotationElement.of(el.name(), mapAnnotationValue(el.value()))).toList());
    }

    AnnotationValue mapAnnotationValue(AnnotationValue val) {
        Objects.requireNonNull(val);
        if (val instanceof AnnotationValue.OfAnnotation annotation) {
            return AnnotationValue.ofAnnotation(mapAnnotation(annotation.annotation()));
        } else if (val instanceof AnnotationValue.OfArray array) {
            return AnnotationValue.ofArray(array.values().stream().map(this::mapAnnotationValue).toList());
        } else if (val instanceof AnnotationValue.OfConstant) {
            return val;
        } else if (val instanceof AnnotationValue.OfClass classValue) {
            return AnnotationValue.ofClass(map(classValue.classSymbol()));
        } else if (val instanceof AnnotationValue.OfEnum enumValue) {
            return AnnotationValue.ofEnum(map(enumValue.classSymbol()), enumValue.constantName().stringValue());
        }
        throw new AssertionError("Unexpected annotation value: " + val);
    }

    List<TypeAnnotation> mapTypeAnnotations(List<TypeAnnotation> typeAnnotations) {
        return typeAnnotations.stream().map(a -> TypeAnnotation.of(a.targetInfo(),
                a.targetPath(), mapAnnotation(a.annotation()))).toList();
    }

    List<Signature.TypeParam> mapTypeParams(List<Signature.TypeParam> typeParams) {
        return typeParams.stream().map(tp -> Signature.TypeParam.of(tp.identifier(),
                tp.classBound().map(this::mapSignature),
                tp.interfaceBounds().stream()
                        .map(this::mapSignature).toArray(Signature.RefTypeSig[]::new))).toList();
    }

}
