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
package org.glavo.classfile.impl;

import org.glavo.classfile.Annotation;
import org.glavo.classfile.AnnotationElement;
import org.glavo.classfile.AnnotationValue;
import org.glavo.classfile.ClassBuilder;
import org.glavo.classfile.ClassElement;
import org.glavo.classfile.ClassSignature;
import org.glavo.classfile.CodeBuilder;
import org.glavo.classfile.CodeElement;
import org.glavo.classfile.CodeModel;
import org.glavo.classfile.CodeTransform;
import org.glavo.classfile.FieldBuilder;
import org.glavo.classfile.FieldElement;
import org.glavo.classfile.FieldModel;
import org.glavo.classfile.FieldTransform;
import org.glavo.classfile.Interfaces;
import org.glavo.classfile.MethodBuilder;
import org.glavo.classfile.MethodElement;
import org.glavo.classfile.MethodModel;
import org.glavo.classfile.MethodSignature;
import org.glavo.classfile.MethodTransform;
import org.glavo.classfile.Signature;
import org.glavo.classfile.Superclass;
import org.glavo.classfile.TypeAnnotation;
import org.glavo.classfile.attribute.AnnotationDefaultAttribute;
import org.glavo.classfile.attribute.EnclosingMethodAttribute;
import org.glavo.classfile.attribute.ExceptionsAttribute;
import org.glavo.classfile.attribute.InnerClassInfo;
import org.glavo.classfile.attribute.InnerClassesAttribute;
import org.glavo.classfile.attribute.ModuleAttribute;
import org.glavo.classfile.attribute.ModuleProvideInfo;
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
import org.glavo.classfile.components.ClassRemapper;
import org.glavo.classfile.constantpool.Utf8Entry;
import org.glavo.classfile.instruction.ConstantInstruction.LoadConstantInstruction;
import org.glavo.classfile.instruction.ExceptionCatch;
import org.glavo.classfile.instruction.FieldInstruction;
import org.glavo.classfile.instruction.InvokeDynamicInstruction;
import org.glavo.classfile.instruction.InvokeInstruction;
import org.glavo.classfile.instruction.LocalVariable;
import org.glavo.classfile.instruction.LocalVariableType;
import org.glavo.classfile.instruction.NewMultiArrayInstruction;
import org.glavo.classfile.instruction.NewObjectInstruction;
import org.glavo.classfile.instruction.NewReferenceArrayInstruction;
import org.glavo.classfile.instruction.TypeCheckInstruction;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.DynamicCallSiteDesc;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public record ClassRemapperImpl(Function<ClassDesc, ClassDesc> mapFunction) implements ClassRemapper {

    @Override
    public void accept(ClassBuilder clb, ClassElement cle) {
        Objects.requireNonNull(cle);
        if (cle instanceof FieldModel fm) {
            clb.withField(fm.fieldName().stringValue(), map(
                    fm.fieldTypeSymbol()), fb ->
                    fm.forEachElement(asFieldTransform().resolve(fb).consumer()));
        } else if (cle instanceof MethodModel mm) {
            clb.withMethod(mm.methodName().stringValue(), mapMethodDesc(
                    mm.methodTypeSymbol()), mm.flags().flagsMask(), mb ->
                    mm.forEachElement(asMethodTransform().resolve(mb).consumer()));
        } else if (cle instanceof Superclass sc) {
            clb.withSuperclass(map(sc.superclassEntry().asSymbol()));
        } else if (cle instanceof Interfaces ins) {
            clb.withInterfaceSymbols(Util.mappedList(ins.interfaces(), in ->
                    map(in.asSymbol())));
        } else if (cle instanceof SignatureAttribute sa) {
            clb.with(SignatureAttribute.of(mapClassSignature(sa.asClassSignature())));
        } else if (cle instanceof InnerClassesAttribute ica) {
            clb.with(InnerClassesAttribute.of(ica.classes().stream().map(ici ->
                    InnerClassInfo.of(map(ici.innerClass().asSymbol()),
                            ici.outerClass().map(oc -> map(oc.asSymbol())),
                            ici.innerName().map(Utf8Entry::stringValue),
                            ici.flagsMask())).toList()));
        } else if (cle instanceof EnclosingMethodAttribute ema) {
            clb.with(EnclosingMethodAttribute.of(map(ema.enclosingClass().asSymbol()),
                    ema.enclosingMethodName().map(Utf8Entry::stringValue),
                    ema.enclosingMethodTypeSymbol().map(this::mapMethodDesc)));
        } else if (cle instanceof RecordAttribute ra) {
            clb.with(RecordAttribute.of(ra.components().stream()
                    .map(this::mapRecordComponent).toList()));
        } else if (cle instanceof ModuleAttribute ma) {
            clb.with(ModuleAttribute.of(ma.moduleName(), ma.moduleFlagsMask(),
                    ma.moduleVersion().orElse(null),
                    ma.requires(), ma.exports(), ma.opens(),
                    ma.uses().stream().map(ce ->
                            clb.constantPool().classEntry(map(ce.asSymbol()))).toList(),
                    ma.provides().stream().map(mp ->
                            ModuleProvideInfo.of(map(mp.provides().asSymbol()),
                                    mp.providesWith().stream().map(pw ->
                                            map(pw.asSymbol())).toList())).toList()));
        } else if (cle instanceof NestHostAttribute nha) {
            clb.with(NestHostAttribute.of(map(nha.nestHost().asSymbol())));
        } else if (cle instanceof NestMembersAttribute nma) {
            clb.with(NestMembersAttribute.ofSymbols(nma.nestMembers().stream()
                    .map(nm -> map(nm.asSymbol())).toList()));
        } else if (cle instanceof PermittedSubclassesAttribute psa) {
            clb.with(PermittedSubclassesAttribute.ofSymbols(
                    psa.permittedSubclasses().stream().map(ps ->
                            map(ps.asSymbol())).toList()));
        } else if (cle instanceof RuntimeVisibleAnnotationsAttribute aa) {
            clb.with(RuntimeVisibleAnnotationsAttribute.of(
                    mapAnnotations(aa.annotations())));
        } else if (cle instanceof RuntimeInvisibleAnnotationsAttribute aa) {
            clb.with(RuntimeInvisibleAnnotationsAttribute.of(
                    mapAnnotations(aa.annotations())));
        } else if (cle instanceof RuntimeVisibleTypeAnnotationsAttribute aa) {
            clb.with(RuntimeVisibleTypeAnnotationsAttribute.of(
                    mapTypeAnnotations(aa.annotations())));
        } else if (cle instanceof RuntimeInvisibleTypeAnnotationsAttribute aa) {
            clb.with(RuntimeInvisibleTypeAnnotationsAttribute.of(
                    mapTypeAnnotations(aa.annotations())));
        } else {
            clb.with(cle);
        }
    }

    @Override
    public FieldTransform asFieldTransform() {
        return (FieldBuilder fb, FieldElement fe) -> {
            Objects.requireNonNull(fe);
            if (fe instanceof SignatureAttribute sa) {
                fb.with(SignatureAttribute.of(
                        mapSignature(sa.asTypeSignature())));
            } else if (fe instanceof RuntimeVisibleAnnotationsAttribute aa) {
                fb.with(RuntimeVisibleAnnotationsAttribute.of(
                        mapAnnotations(aa.annotations())));
            } else if (fe instanceof RuntimeInvisibleAnnotationsAttribute aa) {
                fb.with(RuntimeInvisibleAnnotationsAttribute.of(
                        mapAnnotations(aa.annotations())));
            } else if (fe instanceof RuntimeVisibleTypeAnnotationsAttribute aa) {
                fb.with(RuntimeVisibleTypeAnnotationsAttribute.of(
                        mapTypeAnnotations(aa.annotations())));
            } else if (fe instanceof RuntimeInvisibleTypeAnnotationsAttribute aa) {
                fb.with(RuntimeInvisibleTypeAnnotationsAttribute.of(
                        mapTypeAnnotations(aa.annotations())));
            } else {
                fb.with(fe);
            }
        };
    }

    @Override
    public MethodTransform asMethodTransform() {
        return (MethodBuilder mb, MethodElement me) -> {
            Objects.requireNonNull(me);
            if (me instanceof AnnotationDefaultAttribute ada) {
                mb.with(AnnotationDefaultAttribute.of(
                        mapAnnotationValue(ada.defaultValue())));
            } else if (me instanceof CodeModel com) {
                mb.transformCode(com, asCodeTransform());
            } else if (me instanceof ExceptionsAttribute ea) {
                mb.with(ExceptionsAttribute.ofSymbols(
                        ea.exceptions().stream().map(ce ->
                                map(ce.asSymbol())).toList()));
            } else if (me instanceof SignatureAttribute sa) {
                mb.with(SignatureAttribute.of(
                        mapMethodSignature(sa.asMethodSignature())));
            } else if (me instanceof RuntimeVisibleAnnotationsAttribute aa) {
                mb.with(RuntimeVisibleAnnotationsAttribute.of(
                        mapAnnotations(aa.annotations())));
            } else if (me instanceof RuntimeInvisibleAnnotationsAttribute aa) {
                mb.with(RuntimeInvisibleAnnotationsAttribute.of(
                        mapAnnotations(aa.annotations())));
            } else if (me instanceof RuntimeVisibleParameterAnnotationsAttribute paa) {
                mb.with(RuntimeVisibleParameterAnnotationsAttribute.of(
                        paa.parameterAnnotations().stream()
                                .map(this::mapAnnotations).toList()));
            } else if (me instanceof RuntimeInvisibleParameterAnnotationsAttribute paa) {
                mb.with(RuntimeInvisibleParameterAnnotationsAttribute.of(
                        paa.parameterAnnotations().stream()
                                .map(this::mapAnnotations).toList()));
            } else if (me instanceof RuntimeVisibleTypeAnnotationsAttribute aa) {
                mb.with(RuntimeVisibleTypeAnnotationsAttribute.of(
                        mapTypeAnnotations(aa.annotations())));
            } else if (me instanceof RuntimeInvisibleTypeAnnotationsAttribute aa) {
                mb.with(RuntimeInvisibleTypeAnnotationsAttribute.of(
                        mapTypeAnnotations(aa.annotations())));
            } else {
                mb.with(me);
            }
        };
    }

    @Override
    public CodeTransform asCodeTransform() {
        return (CodeBuilder cob, CodeElement coe) -> {
            Objects.requireNonNull(coe);
            if (coe instanceof FieldInstruction fai) {
                cob.fieldInstruction(fai.opcode(), map(fai.owner().asSymbol()),
                        fai.name().stringValue(), map(fai.typeSymbol()));
            } else if (coe instanceof InvokeInstruction ii) {
                cob.invokeInstruction(ii.opcode(), map(ii.owner().asSymbol()),
                        ii.name().stringValue(), mapMethodDesc(ii.typeSymbol()),
                        ii.isInterface());
            } else if (coe instanceof InvokeDynamicInstruction idi) {
                cob.invokeDynamicInstruction(DynamicCallSiteDesc.of(
                        idi.bootstrapMethod(), idi.name().stringValue(),
                        mapMethodDesc(idi.typeSymbol()),
                        idi.bootstrapArgs().stream().map(this::mapConstantValue).toArray(ConstantDesc[]::new)));
            } else if (coe instanceof NewObjectInstruction c) {
                cob.newObjectInstruction(map(c.className().asSymbol()));
            } else if (coe instanceof NewReferenceArrayInstruction c) {
                cob.anewarray(map(c.componentType().asSymbol()));
            } else if (coe instanceof NewMultiArrayInstruction c) {
                cob.multianewarray(map(c.arrayType().asSymbol()), c.dimensions());
            } else if (coe instanceof TypeCheckInstruction c) {
                cob.typeCheckInstruction(c.opcode(), map(c.type().asSymbol()));
            } else if (coe instanceof ExceptionCatch c) {
                cob.exceptionCatch(c.tryStart(), c.tryEnd(), c.handler(), c.catchType()
                        .map(d -> TemporaryConstantPool.INSTANCE.classEntry(map(d.asSymbol()))));
            } else if (coe instanceof LocalVariable c) {
                cob.localVariable(c.slot(), c.name().stringValue(), map(c.typeSymbol()),
                        c.startScope(), c.endScope());
            } else if (coe instanceof LocalVariableType c) {
                cob.localVariableType(c.slot(), c.name().stringValue(),
                        mapSignature(c.signatureSymbol()), c.startScope(), c.endScope());
            } else if (coe instanceof LoadConstantInstruction ldc) {
                cob.constantInstruction(ldc.opcode(),
                        mapConstantValue(ldc.constantValue()));
            } else if (coe instanceof RuntimeVisibleTypeAnnotationsAttribute aa) {
                cob.with(RuntimeVisibleTypeAnnotationsAttribute.of(
                        mapTypeAnnotations(aa.annotations())));
            } else if (coe instanceof RuntimeInvisibleTypeAnnotationsAttribute aa) {
                cob.with(RuntimeInvisibleTypeAnnotationsAttribute.of(
                        mapTypeAnnotations(aa.annotations())));
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
                component.attributes().stream().map(atr -> {
                    Objects.requireNonNull(atr);
                    if (atr instanceof SignatureAttribute sa) {
                        return SignatureAttribute.of(
                                mapSignature(sa.asTypeSignature()));
                    } else if (atr instanceof RuntimeVisibleAnnotationsAttribute aa) {
                        return RuntimeVisibleAnnotationsAttribute.of(
                                mapAnnotations(aa.annotations()));
                    } else if (atr instanceof RuntimeInvisibleAnnotationsAttribute aa) {
                        return RuntimeInvisibleAnnotationsAttribute.of(
                                mapAnnotations(aa.annotations()));
                    } else if (atr instanceof RuntimeVisibleTypeAnnotationsAttribute aa) {
                        return RuntimeVisibleTypeAnnotationsAttribute.of(
                                mapTypeAnnotations(aa.annotations()));
                    } else if (atr instanceof RuntimeInvisibleTypeAnnotationsAttribute aa) {
                        return RuntimeInvisibleTypeAnnotationsAttribute.of(
                                mapTypeAnnotations(aa.annotations()));
                    } else {
                        return atr;
                    }
                }).toList());
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
        if (value instanceof ClassDesc cd) {
            return map(cd);
        } else if (value instanceof DynamicConstantDesc<?> dcd) {
            return mapDynamicConstant(dcd);
        } else if (value instanceof DirectMethodHandleDesc dmhd) {
            return mapDirectMethodHandle(dmhd);
        } else if (value instanceof MethodTypeDesc mtd) {
            return mapMethodDesc(mtd);
        } else {
            return value;
        }
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
        if (signature instanceof Signature.ArrayTypeSig ats) {
            return (S) Signature.ArrayTypeSig.of(mapSignature(ats.componentSignature()));
        } else if (signature instanceof Signature.ClassTypeSig cts) {
            return (S) Signature.ClassTypeSig.of(
                    cts.outerType().map(this::mapSignature).orElse(null),
                    map(cts.classDesc()),
                    cts.typeArgs().stream()
                            .map(ta -> Signature.TypeArg.of(
                                    ta.wildcardIndicator(),
                                    ta.boundType().map(this::mapSignature)))
                            .toArray(Signature.TypeArg[]::new));
        } else {
            return signature;
        }
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
        if (val instanceof AnnotationValue.OfAnnotation oa) {
            return AnnotationValue.ofAnnotation(mapAnnotation(oa.annotation()));
        } else if (val instanceof AnnotationValue.OfArray oa) {
            return AnnotationValue.ofArray(oa.values().stream().map(this::mapAnnotationValue).toList());
        } else if (val instanceof AnnotationValue.OfConstant oc) {
            return oc;
        } else if (val instanceof AnnotationValue.OfClass oc) {
            return AnnotationValue.ofClass(map(oc.classSymbol()));
        } else if (val instanceof AnnotationValue.OfEnum oe) {
            return AnnotationValue.ofEnum(map(oe.classSymbol()), oe.constantName().stringValue());
        } else {
            throw new IllegalArgumentException();
        }
    }

    List<TypeAnnotation> mapTypeAnnotations(List<TypeAnnotation> typeAnnotations) {
        return typeAnnotations.stream().map(a -> TypeAnnotation.of(a.targetInfo(),
                a.targetPath(), map(a.classSymbol()),
                a.elements().stream().map(el -> AnnotationElement.of(el.name(),
                        mapAnnotationValue(el.value()))).toList())).toList();
    }

    List<Signature.TypeParam> mapTypeParams(List<Signature.TypeParam> typeParams) {
        return typeParams.stream().map(tp -> Signature.TypeParam.of(tp.identifier(),
                tp.classBound().map(this::mapSignature),
                tp.interfaceBounds().stream()
                        .map(this::mapSignature).toArray(Signature.RefTypeSig[]::new))).toList();
    }

}
