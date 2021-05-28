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

import javassist.CtMethod;
import me.derklaro.codegen.annotations.HashCode;
import me.derklaro.codegen.generator.Generator;
import me.derklaro.codegen.stack.generators.GeneratorStack;
import me.derklaro.codegen.util.BytecodeUtility;
import me.derklaro.codegen.util.MethodFieldPair;
import org.jetbrains.annotations.NotNull;
import spoon.reflect.declaration.CtElement;

import java.util.Deque;

public class HashCodeGenerator implements Generator {

  protected static final String HASH_CODE_EXCLUDE_ANNOTATION = HashCode.Exclude.class.getCanonicalName();

  protected final boolean callSuper;
  protected final Deque<MethodFieldPair> getterMethods;

  public HashCodeGenerator(boolean callSuper, @NotNull Deque<MethodFieldPair> getterMethods) {
    this.callSuper = callSuper;
    this.getterMethods = getterMethods;
  }

  @Override
  public void applyTo(@NotNull GeneratorStack stack) throws Exception {
    CtMethod method = CtMethod.make(String.format(
      "public int hashCode() { %s }",
      this.provideMethodBody(this.getterMethods)
    ), stack.getGeneratingClass());
    stack.getGeneratingClass().addMethod(method);
  }

  protected @NotNull String provideMethodBody(@NotNull Deque<MethodFieldPair> getterMethods) {
    StringBuilder builder = new StringBuilder();
    // we need some more things if we want to call super
    builder.append("int result = 1;");
    if (this.callSuper) {
      builder.append("result = (result * 59) + super.hashCode();");
    }
    // remove all fields which are excluded
    getterMethods.removeIf(pair -> this.isExcluded(pair.getMethod()));
    // append the hashCode of the fields which are not excluded (if there are any)
    if (!getterMethods.isEmpty()) {
      builder.append("result = (result * 59) + java.util.Objects.hashCode(");
      for (MethodFieldPair pair : getterMethods) {
        builder.append("this.").append(pair.getMethod().getSimpleName()).append("(),");
      }
      // remove the last comma and close the statement
      builder.delete(builder.length() - 1, builder.length()).append(");");
    }
    // return the result
    return builder.append("return result;").toString();
  }

  protected boolean isExcluded(@NotNull CtElement element) {
    return BytecodeUtility.isAnnotationPresent(element, HASH_CODE_EXCLUDE_ANNOTATION);
  }
}
