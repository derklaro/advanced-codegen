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

import javassist.NotFoundException;
import me.derklaro.codegen.annotations.NonNull;
import me.derklaro.codegen.generator.Generator;
import me.derklaro.codegen.stack.generators.GeneratorStack;
import me.derklaro.codegen.util.BytecodeUtility;
import org.jetbrains.annotations.NotNull;
import spoon.reflect.declaration.CtMethod;

import java.util.Map;

public class MethodNonNullParameterGenerator implements Generator {

  protected final CtMethod<?> method;
  protected final Map<Integer, NonNull> configurations;

  public MethodNonNullParameterGenerator(CtMethod<?> method, Map<Integer, NonNull> configurations) {
    this.method = method;
    this.configurations = configurations;
  }

  @Override
  public void applyTo(@NotNull GeneratorStack stack) throws Exception {
    try {
      // get the method from the current stack class
      javassist.CtMethod method = stack.getGeneratingClass().getMethod(
        this.method.getSimpleName(), BytecodeUtility.provideMethodSignature(this.method));
      for (Map.Entry<Integer, NonNull> entry : this.configurations.entrySet()) {
        method.insertBefore(String.format("{ %s }", this.provideMethodBody(entry.getKey(), entry.getValue())));
      }
    } catch (NotFoundException exception) {
      // unknown method (should not happen so notify)
      throw new IllegalStateException("Method which has parameter annotated with @NonNull is not present "
        + this.method.getSimpleName());
    }
  }

  protected @NotNull String provideMethodBody(@NotNull Integer location, @NotNull NonNull configuration) {
    switch (configuration.handler()) {
      case CUSTOM:
        return this.provideCustomMethodBody(location, configuration);
      case GUAVA_PRECONDITIONS:
        return this.provideGuavaMethodBody(location, configuration);
      case APACHE_VALIDATE:
        return this.provideApacheMethodBody(location, configuration);
      default:
        throw new UnsupportedOperationException("Unsupported non-null handler " + configuration.handler());
    }
  }

  protected @NotNull String provideCustomMethodBody(@NotNull Integer location, @NotNull NonNull configuration) {
    return String.format("if ($%d == null) { throw new %s(java.text.MessageFormat.format(\"%s\", new String[]{ \"%s\" })); }",
      location, configuration.exception().getCanonicalName(), configuration.message(), location);
  }

  protected @NotNull String provideGuavaMethodBody(@NotNull Integer location, @NotNull NonNull configuration) {
    return String.format("com.google.common.base.Preconditions.checkNotNull($%d, \"%s\", new String[]{ \"%d\" });",
      location, configuration.message(), location);
  }

  protected @NotNull String provideApacheMethodBody(@NotNull Integer location, @NotNull NonNull configuration) {
    return String.format("org.apache.commons.lang3.Validate.notNull($%d, \"%s\", new String[]{ \"%d\" });",
      location, configuration.message(), location);
  }
}
