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
import me.derklaro.codegen.annotations.Equals;
import me.derklaro.codegen.generator.Generator;
import me.derklaro.codegen.stack.generators.GeneratorStack;
import me.derklaro.codegen.util.BytecodeUtility;
import me.derklaro.codegen.util.MethodFieldPair;
import org.jetbrains.annotations.NotNull;
import spoon.reflect.declaration.CtElement;

import java.util.Deque;

public class EqualsGenerator implements Generator {

  protected static final String EQUALS_EXCLUDE_ANNOTATION = Equals.Exclude.class.getCanonicalName();

  protected final boolean callSuper;
  protected final boolean preventNullabilityIssues;
  protected final Deque<MethodFieldPair> getterMethods;

  public EqualsGenerator(boolean callSuper, boolean preventNullabilityIssues, @NotNull Deque<MethodFieldPair> getterMethods) {
    this.callSuper = callSuper;
    this.preventNullabilityIssues = preventNullabilityIssues;
    this.getterMethods = getterMethods;
  }

  @Override
  public void applyTo(@NotNull GeneratorStack stack) throws Exception {
    CtMethod method = CtMethod.make(String.format(
      "public boolean equals(Object o) { %s }",
      this.provideEqualsMethodBody(stack.getGeneratingClass(), this.getterMethods)
    ), stack.getGeneratingClass());
    stack.getGeneratingClass().addMethod(method);
  }

  protected @NotNull String provideEqualsMethodBody(@NotNull CtClass ctClass, @NotNull Deque<MethodFieldPair> getterMethods) {
    StringBuilder builder = new StringBuilder();
    // check if the class is actually the current class
    builder.append("if ($1 == $0) return true;");
    // check if the other object is null
    builder.append("if ($1 == null) return false;");
    // check if both classes match
    builder.append("if ($1.getClass() != $0.getClass()) return false;");
    // cast the object
    builder.append(ctClass.getName()).append(" that = (").append(ctClass.getName()).append(") $1;");
    // next step: return
    builder.append("return ");
    // we need to remember if we processed at least one field
    boolean processedOne = false;
    // append all fields to the method
    while (!getterMethods.isEmpty()) {
      MethodFieldPair pair = getterMethods.pop();
      // check if the method is excluded
      if (this.isExcluded(pair.getMethod())) {
        continue;
      }
      // processed
      processedOne = true;
      // format the actual equals check using Objects.equals
      // when nullability issues should be prevented
      if (this.preventNullabilityIssues) {
        builder.append(String.format("java.util.Objects.equals(this.%s(), that.%s())",
          pair.getMethod().getSimpleName(), pair.getMethod().getSimpleName()));
      } else {
        builder.append(String.format("this.%s().equals(that.%s())",
          pair.getMethod().getSimpleName(), pair.getMethod().getSimpleName()));
      }
      // chain the calls
      builder.append("&&");
    }
    // remove the last && if needed
    if (processedOne) {
      if (this.callSuper) {
        builder.append("super.equals($1)");
      } else {
        builder.delete(builder.length() - 2, builder.length());
      }
    } else {
      // no fields to process, always equals
      builder.append("true");
    }
    return builder.append(";").toString();
  }

  protected boolean isExcluded(@NotNull CtElement element) {
    return BytecodeUtility.isAnnotationPresent(element, EQUALS_EXCLUDE_ANNOTATION);
  }
}
