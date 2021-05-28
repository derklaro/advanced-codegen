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

import me.derklaro.codegen.annotations.Invoke;
import me.derklaro.codegen.generator.Generator;
import me.derklaro.codegen.stack.generators.GeneratorStack;
import org.jetbrains.annotations.NotNull;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.reference.CtTypeReference;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

public class MethodGenerator implements Generator {

  private final CtMethod<?> root;

  private final String name;
  private final String methodBody;
  private final List<CtTypeReference<?>> parameters;

  public MethodGenerator(@NotNull CtMethod<?> method) {
    this.root = method;
    this.name = method.getSimpleName();
    this.parameters = method.getParameters().stream().map(CtParameter::getType).collect(Collectors.toList());

    Invoke invoke = method.getAnnotation(Invoke.class);
    if (invoke != null) {
      this.methodBody = this.formatBody(invoke, new ArrayDeque<>(Arrays.asList(invoke.invocations())));
    } else {
      // empty method body
      this.methodBody = "";
    }
  }

  public MethodGenerator(@NotNull CtMethod<?> method, @NotNull String methodBody) {
    this.root = method;
    this.name = method.getSimpleName();
    this.methodBody = methodBody;
    this.parameters = method.getParameters().stream().map(CtParameter::getType).collect(Collectors.toList());
  }

  @Override
  public void applyTo(@NotNull GeneratorStack stack) throws Exception {
    javassist.CtMethod method = javassist.CtMethod.make(String.format(
      "public %s %s(%s) { %s }",
      this.root.getType().getQualifiedName(),
      this.name,
      this.formatParameters(this.parameters),
      this.methodBody
    ), stack.getGeneratingClass());
    stack.getGeneratingClass().addMethod(method);
  }

  protected @NotNull String formatParameters(@NotNull List<CtTypeReference<?>> parameters) {
    StringBuilder stringBuilder = new StringBuilder();
    for (int i = 0; i < parameters.size(); i++) {
      stringBuilder.append(parameters.get(i).getQualifiedName()).append(" param").append(i).append(",");
    }
    return stringBuilder.length() == 0 ? "" : stringBuilder.substring(0, stringBuilder.length() - 1);
  }

  protected @NotNull String formatBody(@NotNull Invoke root, @NotNull Deque<Invoke.SingleInvoke> invocations) {
    StringBuilder stringBuilder = new StringBuilder();
    while (!invocations.isEmpty()) {
      // check if we reached the last method call.
      // We need to handle the invoke return value in the
      // last method call.
      if (invocations.size() == 1) {
        Invoke.SingleInvoke invoke = invocations.pop();
        switch (root.returns()) {
          case METHOD_CALL:
            // We need to return the last method call of the body.
            // So we just write 'return ' before the downstream
            // call to the write method.
            this.appendBodyInvocation(invoke, stringBuilder.append("return "));
            break;
          case SELF:
            // The method should return the same instance of the class
            // e.g. for chaining calls.
            this.appendBodyInvocation(invoke, stringBuilder);
            stringBuilder.append("return this;");
            break;
          default:
            // Just write the entry to the body as we need no specific
            // handling of the statement.
            this.appendBodyInvocation(invoke, stringBuilder);
            break;
        }
      } else {
        // dump the body entry into the builder
        this.appendBodyInvocation(invocations.pop(), stringBuilder);
      }
    }
    return stringBuilder.toString();
  }

  protected void appendBodyInvocation(@NotNull Invoke.SingleInvoke invoke, @NotNull StringBuilder builder) {
    builder.append(invoke.method()).append(";");
  }
}
