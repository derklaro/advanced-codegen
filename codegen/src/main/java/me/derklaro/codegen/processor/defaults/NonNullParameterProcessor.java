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

package me.derklaro.codegen.processor.defaults;

import javassist.ClassPool;
import javassist.CtClass;
import me.derklaro.codegen.annotations.NonNull;
import me.derklaro.codegen.generation.MethodNonNullParameterGenerator;
import me.derklaro.codegen.processor.AbstractTypeProcessor;
import me.derklaro.codegen.util.BytecodeUtility;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

public class NonNullParameterProcessor extends AbstractTypeProcessor {

  public NonNullParameterProcessor(ClassPool classPool) {
    super(classPool);
  }

  protected static @Nullable MethodNonNullParameterGenerator findConfigurations(@NotNull CtMethod<?> method) {
    Map<Integer, NonNull> configurations = new HashMap<>();
    for (int i = 0; i < method.getParameters().size(); i++) {
      NonNull nonNull = method.getParameters().get(i).getAnnotation(NonNull.class);
      if (nonNull != null) {
        // the parameter is the index + 1
        configurations.put(i + 1, nonNull);
      }
    }
    return configurations.isEmpty() ? null : new MethodNonNullParameterGenerator(method, configurations);
  }

  @Override
  public boolean shouldProcess(@NotNull CtType<?> type) {
    return type.getMethods().stream().anyMatch(method -> !method.isAbstract());
  }

  @Override
  public boolean process(@NotNull CtType<?> type) {
    for (CtMethod<?> method : type.getMethods()) {
      // we don't want to process abstract methods
      if (!method.isAbstract()) {
        // check if there are any parameters annotated as @NonNull and add the processor if needed
        @Nullable MethodNonNullParameterGenerator generator = findConfigurations(method);
        if (generator != null) {
          this.pendingGenerations.computeIfAbsent(type, $ -> new ArrayDeque<>()).push(generator);
        }
      }
    }
    // Success!
    return true;
  }

  @Override
  protected @Nullable CtClass provideCtClass(@NotNull CtType<?> type) {
    return BytecodeUtility.getCtClassOrNull(this.classPool, type.getQualifiedName());
  }
}
