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

package me.derklaro.codegen.generation;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;
import me.derklaro.codegen.generator.Generator;
import me.derklaro.codegen.generator.result.ClassResult;
import me.derklaro.codegen.stack.generators.GeneratorStack;
import me.derklaro.codegen.util.BytecodeUtility;
import me.derklaro.codegen.util.MethodFieldPair;
import org.jetbrains.annotations.NotNull;

import java.util.Deque;

public class FactoryMethodGenerator implements Generator {

  protected final String classLocation;
  protected final String classMethod;
  protected final boolean overrideReturn;
  protected final Deque<MethodFieldPair> pairs;

  public FactoryMethodGenerator(String classLocation, String classMethod, boolean overrideReturn, Deque<MethodFieldPair> pairs) {
    this.classLocation = classLocation;
    this.classMethod = classMethod;
    this.overrideReturn = overrideReturn;
    this.pairs = pairs;
  }

  @Override
  public void applyTo(@NotNull GeneratorStack stack) throws Exception {
    // get or generate the target class
    CtClass factoryClass = BytecodeUtility.provideCtClass(stack.getGeneratingClass().getClassPool(), this.classLocation);
    // get or create the method
    CtMethod method;
    try {
      String desc = BytecodeUtility.provideVagueMethodSignature(stack.getType().getQualifiedName(), this.pairs);
      method = factoryClass.getMethod(this.classMethod, desc);
      // check if the existing method is static
      if ((method.getMethodInfo().getAccessFlags() & AccessFlag.STATIC) == 0) {
        throw new IllegalStateException(String.format("Existing factory method %s (desc: %s) in class %s is-non static",
          this.classMethod, desc, this.classLocation));
      }
    } catch (NotFoundException exception) {
      method = CtMethod.make(String.format(
        "public static %s %s(%s) { return new %s($$); }",
        stack.getType().getQualifiedName(),
        this.classMethod,
        this.provideParameters(this.pairs),
        stack.getGeneratingClass().getName()
      ), factoryClass);
      factoryClass.addMethod(method);
      stack.classStack().pushLast(ClassResult.builder()
        .packageClassName(this.classLocation)
        .bytecode(factoryClass.toBytecode())
        .build());
      return;
    }
    // modify the method if we are allowed to
    if (this.overrideReturn) {
      method.insertAfter(String.format("return new %s($$);", stack.getGeneratingClass().getName()));
      // only put in the map if we actually modified the class
      stack.classStack().replace(ClassResult.builder()
        .packageClassName(this.classLocation)
        .bytecode(factoryClass.toBytecode())
        .build());
    }
  }

  protected @NotNull String provideParameters(@NotNull Deque<MethodFieldPair> pairs) {
    StringBuilder builder = new StringBuilder();
    // process the getter methods
    for (MethodFieldPair pair : pairs) {
      // build the parameter
      builder
        .append(pair.getReturnType())
        .append(" ")
        .append(pair.getAssociatedFieldName())
        .append(",");
    }
    return builder.substring(0, builder.length() - 1);
  }
}
