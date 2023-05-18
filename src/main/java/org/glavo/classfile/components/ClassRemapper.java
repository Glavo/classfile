/*
 * Copyright (c) 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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
 *
 */
package org.glavo.classfile.components;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.DynamicCallSiteDesc;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.glavo.classfile.Annotation;
import org.glavo.classfile.AnnotationElement;
import org.glavo.classfile.AnnotationValue;
import org.glavo.classfile.ClassBuilder;
import org.glavo.classfile.ClassElement;
import org.glavo.classfile.ClassModel;
import org.glavo.classfile.ClassSignature;
import org.glavo.classfile.ClassTransform;
import org.glavo.classfile.Classfile;
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
import org.glavo.classfile.impl.TemporaryConstantPool;
import org.glavo.classfile.instruction.FieldInstruction;
import org.glavo.classfile.instruction.InvokeDynamicInstruction;
import org.glavo.classfile.instruction.InvokeInstruction;
import org.glavo.classfile.instruction.NewMultiArrayInstruction;
import org.glavo.classfile.instruction.NewObjectInstruction;
import org.glavo.classfile.instruction.NewReferenceArrayInstruction;
import org.glavo.classfile.instruction.TypeCheckInstruction;
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
import org.glavo.classfile.constantpool.Utf8Entry;
import org.glavo.classfile.instruction.ExceptionCatch;
import org.glavo.classfile.instruction.LocalVariable;
import org.glavo.classfile.instruction.LocalVariableType;
import org.glavo.classfile.impl.Util;
import org.glavo.classfile.instruction.*;

/**
 * {@code ClassRemapper} is a {@link ClassTransform}, {@link FieldTransform},
 * {@link MethodTransform} and {@link CodeTransform}
 * deeply re-mapping all class references in any form, according to given map or
 * map function.
 * <p>
 * The re-mapping is applied to superclass, interfaces, all kinds of descriptors
 * and signatures, all attributes referencing classes in any form (including all
 * types of annotations), and to all instructions referencing to classes.
 * <p>
 * Primitive types and arrays are never subjects of mapping and are not allowed
 * targets of mapping.
 * <p>
 * Arrays of reference types are always decomposed, mapped as the base reference
 * types and composed back to arrays.
 */
public sealed interface ClassRemapper extends ClassTransform {

    /**
     * Creates new instance of {@code ClassRemapper} instructed with a class map.
     * Map may contain only re-mapping entries, identity mapping is applied by default.
     * @param classMap class map
     * @return new instance of {@code ClassRemapper}
     */
    static ClassRemapper of(Map<ClassDesc, ClassDesc> classMap) {
        return of(desc -> classMap.getOrDefault(desc, desc));
    }

    /**
     * Creates new instance of {@code ClassRemapper} instructed with a map function.
     * Map function must return valid {@link java.lang.constant.ClassDesc} of an interface
     * or a class, even for identity mappings.
     * @param mapFunction class map function
     * @return new instance of {@code ClassRemapper}
     */
    static ClassRemapper of(Function<ClassDesc, ClassDesc> mapFunction) {
        return new ClassRemapperImpl(mapFunction);
    }

    /**
     * Access method to internal class mapping function.
     * @param desc source class
     * @return class target class
     */
    ClassDesc map(ClassDesc desc);

    /**
     * Returns this {@code ClassRemapper} as {@link FieldTransform} instance
     * @return this {@code ClassRemapper} as {@link FieldTransform} instance
     */
    FieldTransform asFieldTransform();

    /**
     * Returns this {@code ClassRemapper} as {@link MethodTransform} instance
     * @return this {@code ClassRemapper} as {@link MethodTransform} instance
     */
    MethodTransform asMethodTransform();

    /**
     * Returns this {@code ClassRemapper} as {@link CodeTransform} instance
     * @return this {@code ClassRemapper} as {@link CodeTransform} instance
     */
    CodeTransform asCodeTransform();

    /**
     * Remaps the whole ClassModel into a new class file, including the class name.
     * @param clm class model to re-map
     * @return re-mapped class file bytes
     */
    default byte[] remapClass(ClassModel clm) {
        return Classfile.build(map(clm.thisClass().asSymbol()),
                clb -> clm.forEachElement(resolve(clb).consumer()));
    }

    record ClassRemapperImpl(Function<ClassDesc, ClassDesc> mapFunction) implements ClassRemapper {

        @Override
        public void accept(ClassBuilder clb, ClassElement cle) {
            switch (cle) {
                case FieldModel fm ->
                    clb.withField(fm.fieldName().stringValue(), map(
                            fm.fieldTypeSymbol()), fb ->
                                    fm.forEachElement(asFieldTransform().resolve(fb).consumer()));
                case MethodModel mm ->
                    clb.withMethod(mm.methodName().stringValue(), mapMethodDesc(
                            mm.methodTypeSymbol()), mm.flags().flagsMask(), mb ->
                                    mm.forEachElement(asMethodTransform().resolve(mb).consumer()));
                case Superclass sc ->
                    clb.withSuperclass(map(sc.superclassEntry().asSymbol()));
                case Interfaces ins ->
                    clb.withInterfaceSymbols(Util.mappedList(ins.interfaces(), in ->
                            map(in.asSymbol())));
                case SignatureAttribute sa ->
                    clb.with(SignatureAttribute.of(mapClassSignature(sa.asClassSignature())));
                case InnerClassesAttribute ica ->
                    clb.with(InnerClassesAttribute.of(ica.classes().stream().map(ici ->
                            InnerClassInfo.of(map(ici.innerClass().asSymbol()),
                                    ici.outerClass().map(oc -> map(oc.asSymbol())),
                                    ici.innerName().map(Utf8Entry::stringValue),
                                    ici.flagsMask())).toList()));
                case EnclosingMethodAttribute ema ->
                    clb.with(EnclosingMethodAttribute.of(map(ema.enclosingClass().asSymbol()),
                            ema.enclosingMethodName().map(Utf8Entry::stringValue),
                            ema.enclosingMethodTypeSymbol().map(this::mapMethodDesc)));
                case RecordAttribute ra ->
                    clb.with(RecordAttribute.of(ra.components().stream()
                            .map(this::mapRecordComponent).toList()));
                case ModuleAttribute ma ->
                    clb.with(ModuleAttribute.of(ma.moduleName(), ma.moduleFlagsMask(),
                            ma.moduleVersion().orElse(null),
                            ma.requires(), ma.exports(), ma.opens(),
                            ma.uses().stream().map(ce ->
                                    clb.constantPool().classEntry(map(ce.asSymbol()))).toList(),
                            ma.provides().stream().map(mp ->
                                    ModuleProvideInfo.of(map(mp.provides().asSymbol()),
                                            mp.providesWith().stream().map(pw ->
                                                    map(pw.asSymbol())).toList())).toList()));
                case NestHostAttribute nha ->
                    clb.with(NestHostAttribute.of(map(nha.nestHost().asSymbol())));
                case NestMembersAttribute nma ->
                    clb.with(NestMembersAttribute.ofSymbols(nma.nestMembers().stream()
                            .map(nm -> map(nm.asSymbol())).toList()));
                case PermittedSubclassesAttribute psa ->
                    clb.with(PermittedSubclassesAttribute.ofSymbols(
                            psa.permittedSubclasses().stream().map(ps ->
                                    map(ps.asSymbol())).toList()));
                case RuntimeVisibleAnnotationsAttribute aa ->
                    clb.with(RuntimeVisibleAnnotationsAttribute.of(
                            mapAnnotations(aa.annotations())));
                case RuntimeInvisibleAnnotationsAttribute aa ->
                    clb.with(RuntimeInvisibleAnnotationsAttribute.of(
                            mapAnnotations(aa.annotations())));
                case RuntimeVisibleTypeAnnotationsAttribute aa ->
                    clb.with(RuntimeVisibleTypeAnnotationsAttribute.of(
                            mapTypeAnnotations(aa.annotations())));
                case RuntimeInvisibleTypeAnnotationsAttribute aa ->
                    clb.with(RuntimeInvisibleTypeAnnotationsAttribute.of(
                            mapTypeAnnotations(aa.annotations())));
                default ->
                    clb.with(cle);
            }
        }

        @Override
        public FieldTransform asFieldTransform() {
            return (FieldBuilder fb, FieldElement fe) -> {
                switch (fe) {
                    case SignatureAttribute sa ->
                        fb.with(SignatureAttribute.of(
                                mapSignature(sa.asTypeSignature())));
                    case RuntimeVisibleAnnotationsAttribute aa ->
                        fb.with(RuntimeVisibleAnnotationsAttribute.of(
                                mapAnnotations(aa.annotations())));
                    case RuntimeInvisibleAnnotationsAttribute aa ->
                        fb.with(RuntimeInvisibleAnnotationsAttribute.of(
                                mapAnnotations(aa.annotations())));
                    case RuntimeVisibleTypeAnnotationsAttribute aa ->
                        fb.with(RuntimeVisibleTypeAnnotationsAttribute.of(
                                mapTypeAnnotations(aa.annotations())));
                    case RuntimeInvisibleTypeAnnotationsAttribute aa ->
                        fb.with(RuntimeInvisibleTypeAnnotationsAttribute.of(
                                mapTypeAnnotations(aa.annotations())));
                    default ->
                        fb.with(fe);
                }
            };
        }

        @Override
        public MethodTransform asMethodTransform() {
            return (MethodBuilder mb, MethodElement me) -> {
                switch (me) {
                    case AnnotationDefaultAttribute ada ->
                        mb.with(AnnotationDefaultAttribute.of(
                                mapAnnotationValue(ada.defaultValue())));
                    case CodeModel com ->
                        mb.transformCode(com, asCodeTransform());
                    case ExceptionsAttribute ea ->
                        mb.with(ExceptionsAttribute.ofSymbols(
                                ea.exceptions().stream().map(ce ->
                                        map(ce.asSymbol())).toList()));
                    case SignatureAttribute sa ->
                        mb.with(SignatureAttribute.of(
                                mapMethodSignature(sa.asMethodSignature())));
                    case RuntimeVisibleAnnotationsAttribute aa ->
                        mb.with(RuntimeVisibleAnnotationsAttribute.of(
                                mapAnnotations(aa.annotations())));
                    case RuntimeInvisibleAnnotationsAttribute aa ->
                        mb.with(RuntimeInvisibleAnnotationsAttribute.of(
                                mapAnnotations(aa.annotations())));
                    case RuntimeVisibleParameterAnnotationsAttribute paa ->
                        mb.with(RuntimeVisibleParameterAnnotationsAttribute.of(
                                paa.parameterAnnotations().stream()
                                        .map(this::mapAnnotations).toList()));
                    case RuntimeInvisibleParameterAnnotationsAttribute paa ->
                        mb.with(RuntimeInvisibleParameterAnnotationsAttribute.of(
                                paa.parameterAnnotations().stream()
                                        .map(this::mapAnnotations).toList()));
                    case RuntimeVisibleTypeAnnotationsAttribute aa ->
                        mb.with(RuntimeVisibleTypeAnnotationsAttribute.of(
                                mapTypeAnnotations(aa.annotations())));
                    case RuntimeInvisibleTypeAnnotationsAttribute aa ->
                        mb.with(RuntimeInvisibleTypeAnnotationsAttribute.of(
                                mapTypeAnnotations(aa.annotations())));
                    default ->
                        mb.with(me);
                }
            };
        }

        @Override
        public CodeTransform asCodeTransform() {
            return (CodeBuilder cob, CodeElement coe) -> {
                switch (coe) {
                    case FieldInstruction fai ->
                        cob.fieldInstruction(fai.opcode(), map(fai.owner().asSymbol()),
                                fai.name().stringValue(), map(fai.typeSymbol()));
                    case InvokeInstruction ii ->
                        cob.invokeInstruction(ii.opcode(), map(ii.owner().asSymbol()),
                                ii.name().stringValue(), mapMethodDesc(ii.typeSymbol()),
                                ii.isInterface());
                    case InvokeDynamicInstruction idi ->
                        cob.invokeDynamicInstruction(DynamicCallSiteDesc.of(
                                idi.bootstrapMethod(), idi.name().stringValue(),
                                mapMethodDesc(idi.typeSymbol()),
                                idi.bootstrapArgs().stream().map(this::mapConstantValue).toArray(ConstantDesc[]::new)));
                    case NewObjectInstruction c ->
                        cob.newObjectInstruction(map(c.className().asSymbol()));
                    case NewReferenceArrayInstruction c ->
                        cob.anewarray(map(c.componentType().asSymbol()));
                    case NewMultiArrayInstruction c ->
                        cob.multianewarray(map(c.arrayType().asSymbol()), c.dimensions());
                    case TypeCheckInstruction c ->
                        cob.typeCheckInstruction(c.opcode(), map(c.type().asSymbol()));
                    case ExceptionCatch c ->
                        cob.exceptionCatch(c.tryStart(), c.tryEnd(), c.handler(),c.catchType()
                                .map(d -> TemporaryConstantPool.INSTANCE.classEntry(map(d.asSymbol()))));
                    case LocalVariable c ->
                        cob.localVariable(c.slot(), c.name().stringValue(), map(c.typeSymbol()),
                                c.startScope(), c.endScope());
                    case LocalVariableType c ->
                        cob.localVariableType(c.slot(), c.name().stringValue(),
                                mapSignature(c.signatureSymbol()), c.startScope(), c.endScope());
                    case ConstantInstruction.LoadConstantInstruction ldc ->
                        cob.constantInstruction(ldc.opcode(),
                                mapConstantValue(ldc.constantValue()));
                    case RuntimeVisibleTypeAnnotationsAttribute aa ->
                        cob.with(RuntimeVisibleTypeAnnotationsAttribute.of(
                                mapTypeAnnotations(aa.annotations())));
                    case RuntimeInvisibleTypeAnnotationsAttribute aa ->
                        cob.with(RuntimeInvisibleTypeAnnotationsAttribute.of(
                                mapTypeAnnotations(aa.annotations())));
                    default ->
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
                            .map(this::mapSignature).toArray(Signature.RefTypeSig[]::new));
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
                    component.attributes().stream().map(atr ->
                        switch (atr) {
                            case SignatureAttribute sa ->
                                SignatureAttribute.of(
                                        mapSignature(sa.asTypeSignature()));
                            case RuntimeVisibleAnnotationsAttribute aa ->
                                RuntimeVisibleAnnotationsAttribute.of(
                                        mapAnnotations(aa.annotations()));
                            case RuntimeInvisibleAnnotationsAttribute aa ->
                                RuntimeInvisibleAnnotationsAttribute.of(
                                        mapAnnotations(aa.annotations()));
                            case RuntimeVisibleTypeAnnotationsAttribute aa ->
                                RuntimeVisibleTypeAnnotationsAttribute.of(
                                        mapTypeAnnotations(aa.annotations()));
                            case RuntimeInvisibleTypeAnnotationsAttribute aa ->
                                RuntimeInvisibleTypeAnnotationsAttribute.of(
                                        mapTypeAnnotations(aa.annotations()));
                            default -> atr;
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
            return switch (value) {
                case ClassDesc cd ->
                    map(cd);
                case DynamicConstantDesc<?> dcd ->
                    mapDynamicConstant(dcd);
                case DirectMethodHandleDesc dmhd ->
                    mapDirectMethodHandle(dmhd);
                case MethodTypeDesc mtd ->
                    mapMethodDesc(mtd);
                default -> value;
            };
        }

        DynamicConstantDesc<?> mapDynamicConstant(DynamicConstantDesc<?> dcd) {
            return DynamicConstantDesc.ofNamed(mapDirectMethodHandle(dcd.bootstrapMethod()),
                    dcd.constantName(),
                    map(dcd.constantType()),
                    dcd.bootstrapArgsList().stream().map(this::mapConstantValue).toArray(ConstantDesc[]::new));
        }

        @SuppressWarnings("unchecked")
        <S extends Signature> S mapSignature(S signature) {
            return (S) switch (signature) {
                case Signature.ArrayTypeSig ats ->
                    Signature.ArrayTypeSig.of(mapSignature(ats.componentSignature()));
                case Signature.ClassTypeSig cts ->
                    Signature.ClassTypeSig.of(
                            cts.outerType().map(this::mapSignature).orElse(null),
                            map(cts.classDesc()),
                            cts.typeArgs().stream()
                                    .map(ta -> Signature.TypeArg.of(
                                            ta.wildcardIndicator(),
                                            ta.boundType().map(this::mapSignature)))
                                    .toArray(Signature.TypeArg[]::new));
                default -> signature;
            };
        }

        List<Annotation> mapAnnotations(List<Annotation> annotations) {
            return annotations.stream().map(this::mapAnnotation).toList();
        }

        Annotation mapAnnotation(Annotation a) {
            return Annotation.of(map(a.classSymbol()), a.elements().stream().map(el ->
                    AnnotationElement.of(el.name(), mapAnnotationValue(el.value()))).toList());
        }

        AnnotationValue mapAnnotationValue(AnnotationValue val) {
            return switch (val) {
                case AnnotationValue.OfAnnotation oa ->
                    AnnotationValue.ofAnnotation(mapAnnotation(oa.annotation()));
                case AnnotationValue.OfArray oa ->
                    AnnotationValue.ofArray(oa.values().stream().map(this::mapAnnotationValue).toList());
                case AnnotationValue.OfConstant oc -> oc;
                case AnnotationValue.OfClass oc ->
                    AnnotationValue.ofClass(map(oc.classSymbol()));
                case AnnotationValue.OfEnum oe ->
                    AnnotationValue.ofEnum(map(oe.classSymbol()), oe.constantName().stringValue());
            };
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
}
