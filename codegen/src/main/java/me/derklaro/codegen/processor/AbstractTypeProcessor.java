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

package me.derklaro.codegen.processor;

import javassist.ClassPool;
import javassist.CtClass;
import me.derklaro.codegen.generator.Generator;
import me.derklaro.codegen.generator.result.ClassResult;
import me.derklaro.codegen.stack.generators.GeneratorStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import spoon.reflect.declaration.CtType;

import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractTypeProcessor implements TypeProcessor {

  protected final ClassPool classPool;
  protected final Map<CtType<?>, Deque<Generator>> pendingGenerations;

  /**
   * Creates a new type processor instance.
   *
   * @param classPool the class pool for post generation.
   */
  protected AbstractTypeProcessor(ClassPool classPool) {
    this(classPool, new HashMap<>());
  }

  /**
   * Creates a new type processor instance.
   *
   * @param classPool          the class pool for post generation.
   * @param pendingGenerations the initial pending generations.
   */
  public AbstractTypeProcessor(ClassPool classPool, Map<CtType<?>, Deque<Generator>> pendingGenerations) {
    this.classPool = classPool;
    this.pendingGenerations = pendingGenerations;
  }

  @Override
  public void postProcess(@NotNull GeneratorStack stack) throws Exception {
    // check if we have generations pending
    for (Map.Entry<CtType<?>, Deque<Generator>> entry : this.pendingGenerations.entrySet()) {
      // provide the current generation information to the stack
      stack.setCurrentType(entry.getKey());
      // try to provide a class from the type information
      CtClass ctClass = this.provideCtClass(entry.getKey());
      if (ctClass != null) {
        // provide the class to the stack
        stack.setGeneratingClass(ctClass);
        // post the generators to the class
        for (Generator generator : entry.getValue()) {
          // post the stack to the generator
          stack.pushLast(generator);
          stack.setCurrentGenerator(generator);
          generator.applyTo(stack);
        }
        // push the class result to the class stack
        stack.classStack().pushLast(ClassResult.builder(ctClass).build());
      }
    }
  }

  /**
   * Provides a {@link CtClass} for the given spoon input type.
   *
   * @param type the type to get the ct-class for.
   * @return the ct-class or null if the handler don't need the processing.
   * @throws Exception if any exception occurs during the class providing.
   */
  protected abstract @Nullable CtClass provideCtClass(@NotNull CtType<?> type) throws Exception;
}
