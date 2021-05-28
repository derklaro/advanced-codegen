/*
 * This file is part of codegen, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2021 Pasqual Koschmieder and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package me.derklaro.codegen.util;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@ApiStatus.Internal
public final class BytecodeUtility {

  protected static final Map<String, Character> PRIMITIVE_FIELD_TYPES = new ConcurrentHashMap<String, Character>() {{
    this.put("byte", 'B');
    this.put("char", 'C');
    this.put("double", 'D');
    this.put("float", 'F');
    this.put("int", 'I');
    this.put("long", 'J');
    this.put("short", 'S');
    this.put("boolean", 'Z');
    this.put("void", 'V');
  }};

  private BytecodeUtility() {
    throw new UnsupportedOperationException();
  }

  public static @NotNull CtClass provideCtClass(@NotNull ClassPool classPool, @NotNull String className) {
    try {
      CtClass ctClass = classPool.get(className);
      // defrost to allow modification
      ctClass.defrost();
      return ctClass;
    } catch (NotFoundException exception) {
      return classPool.makeClass(className);
    }
  }

  public static @Nullable CtClass getCtClassOrNull(@NotNull ClassPool classPool, @NotNull String className) {
    CtClass ctClass = classPool.getOrNull(className);
    if (ctClass != null) {
      // defrost to allow modification
      ctClass.defrost();
    }
    return ctClass;
  }

  public static <E extends CtElement> boolean isAnnotationPresent(@NotNull E element, @NotNull String annotation) {
    for (CtAnnotation<? extends Annotation> ctAnnotation : element.getAnnotations()) {
      if (ctAnnotation.getAnnotationType().getQualifiedName().equals(annotation)) {
        return true;
      }
    }
    return false;
  }

  public static @NotNull String provideMethodSignature(@NotNull CtMethod<?> method) {
    return provideSignature(method.getType().getQualifiedName(),
      method.getParameters().stream().map(ctParameter -> ctParameter.getType().getQualifiedName()).collect(Collectors.toList()));
  }

  public static @NotNull String provideVagueConstructorSignature(@NotNull Collection<MethodFieldPair> pairs) {
    return provideSignature("void",
      pairs.stream().map(pair -> pair.getReturnType()).collect(Collectors.toList()));
  }

  public static @NotNull String provideVagueMethodSignature(@NotNull String methodReturnType,
                                                            @NotNull Collection<MethodFieldPair> pairs) {
    return provideSignature(methodReturnType,
      pairs.stream().map(pair -> pair.getReturnType()).collect(Collectors.toList()));
  }

  public static @NotNull String provideSignature(@NotNull String methodReturnType, @NotNull Collection<String> parameterTypes) {
    StringBuilder builder = new StringBuilder().append('(');
    for (String parameterType : parameterTypes) {
      // append the return type of the field
      appendReturnType(builder, parameterType);
    }
    // close parameters
    builder.append(')');
    // append the return type
    appendReturnType(builder, methodReturnType);
    // done processing, should look like for example: '(Lcom/mojang/authlib/GameProfile;)V'
    return builder.toString();
  }

  private static void appendReturnType(@NotNull StringBuilder builder, @NotNull String returnType) {
    int braceClosings = (int) returnType.codePoints().filter(ch -> ch == '[').count();
    if (braceClosings > 0) {
      // determine the type of the array
      String arrayType = returnType.substring(0, returnType.indexOf('['));
      // append the size of the array
      builder.append(String.join("", Collections.nCopies(braceClosings, "[")));
      // append the return type
      appendReturnType(builder, arrayType);
    } else {
      // check for primitive type
      Character fieldType = PRIMITIVE_FIELD_TYPES.get(returnType);
      if (fieldType != null) {
        builder.append(fieldType);
      } else {
        // another object
        builder.append('L').append(returnType.replace('.', '/')).append(';');
      }
    }
  }
}
