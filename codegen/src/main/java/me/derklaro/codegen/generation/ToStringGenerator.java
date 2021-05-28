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
import me.derklaro.codegen.annotations.ToString;
import me.derklaro.codegen.generator.Generator;
import me.derklaro.codegen.stack.generators.GeneratorStack;
import me.derklaro.codegen.util.BytecodeUtility;
import me.derklaro.codegen.util.MethodFieldPair;
import org.jetbrains.annotations.NotNull;
import spoon.reflect.declaration.CtElement;

import java.util.Deque;

public class ToStringGenerator implements Generator {

  protected static final String TO_STRING_EXCLUDE_ANNOTATION = ToString.Exclude.class.getCanonicalName();

  protected static final String JAVA_STRING_BUILDER = "new java.lang.StringBuilder(\"%s{\")";
  protected static final String TO_STRING_HELPER = "com.google.common.base.MoreObjects.toStringHelper(%s)";

  protected final boolean callSuper;
  protected final boolean useToStringHelper;
  protected final Deque<MethodFieldPair> getterMethods;

  public ToStringGenerator(boolean callSuper, boolean useToStringHelper, @NotNull Deque<MethodFieldPair> getterMethods) {
    this.callSuper = callSuper;
    this.useToStringHelper = useToStringHelper;
    this.getterMethods = getterMethods;
  }

  @Override
  public void applyTo(@NotNull GeneratorStack stack) throws Exception {
    CtMethod method = CtMethod.make(String.format(
      "public String toString() { %s }",
      this.provideToStringInstructions(stack.getGeneratingClass(), this.getterMethods)
    ), stack.getGeneratingClass());
    stack.getGeneratingClass().addMethod(method);
  }

  protected @NotNull String provideToStringInstructions(@NotNull CtClass ctClass, @NotNull Deque<MethodFieldPair> getterMethods) {
    StringBuilder stringBuilder = new StringBuilder("return ");
    // add prefix if we use java StringBuilder or guava ToStringHelper
    if (this.useToStringHelper) {
      stringBuilder.append(String.format(TO_STRING_HELPER, "this"));
    } else {
      stringBuilder.append(String.format(JAVA_STRING_BUILDER, ctClass.getSimpleName()));
    }
    // append all non-excluded methods
    while (!getterMethods.isEmpty()) {
      MethodFieldPair pair = getterMethods.pop();
      // check if the method is excluded
      if (this.isExcluded(pair.getMethod())) {
        continue;
      }
      // append to the builder
      if (this.useToStringHelper) {
        stringBuilder.append(String.format(".add(\"%s\", this.%s())",
          pair.getAssociatedFieldName(), pair.getMethod().getSimpleName()));
      } else {
        stringBuilder.append(String.format(".append(\"%s=\").append(this.%s())",
          pair.getAssociatedFieldName(), pair.getMethod().getSimpleName()));
        // only append a comma if there is more than one element left
        if (getterMethods.size() > 1) {
          stringBuilder.append(".append(\", \")");
        }
      }
    }
    // close the defining brackets
    if (!this.useToStringHelper) {
      stringBuilder.append(".append(\"}\")");
      // include the super.toString result if enabled
      if (this.callSuper) {
        stringBuilder.append(".append(super.toString())");
      }
    }
    // call toString() for both types
    return stringBuilder.append(".toString();").toString();
  }

  protected boolean isExcluded(@NotNull CtElement element) {
    return BytecodeUtility.isAnnotationPresent(element, TO_STRING_EXCLUDE_ANNOTATION);
  }
}
