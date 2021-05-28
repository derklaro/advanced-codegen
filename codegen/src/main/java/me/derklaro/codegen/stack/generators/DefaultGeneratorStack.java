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

package me.derklaro.codegen.stack.generators;

import javassist.CtClass;
import me.derklaro.codegen.generator.Generator;
import me.derklaro.codegen.stack.classes.ClassStack;
import me.derklaro.codegen.stack.defaults.AbstractMutableStack;
import org.jetbrains.annotations.NotNull;
import spoon.reflect.declaration.CtType;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class DefaultGeneratorStack extends AbstractMutableStack<Generator> implements GeneratorStack {

  protected final ClassStack classStack;
  protected final AtomicReference<Generator> currentGenerator;
  protected final AtomicReference<CtType<?>> currentDownstreamType;
  protected final AtomicReference<CtClass> currentDownstreamCtClass;

  public DefaultGeneratorStack(@NotNull ClassStack classStack) {
    super(() -> new Generator[0]);

    this.classStack = classStack;
    this.currentGenerator = new AtomicReference<>();
    this.currentDownstreamType = new AtomicReference<>();
    this.currentDownstreamCtClass = new AtomicReference<>();
  }

  @Override
  public @NotNull ClassStack classStack() {
    return this.classStack;
  }

  @Override
  public @NotNull Generator current() {
    return Objects.requireNonNull(this.currentGenerator.get(), "Stack not initialized yet.");
  }

  @Override
  public void setCurrentGenerator(@NotNull Generator generator) {
    this.currentGenerator.set(generator);
  }

  @Override
  public @NotNull CtType<?> getType() {
    return Objects.requireNonNull(this.currentDownstreamType.get(), "Stack not initialized yet.");
  }

  @Override
  public void setCurrentType(@NotNull CtType<?> type) {
    this.currentDownstreamType.set(type);
  }

  @Override
  public @NotNull CtClass getGeneratingClass() {
    return Objects.requireNonNull(this.currentDownstreamCtClass.get(), "Stack not initialized yet.");
  }

  @Override
  public void setGeneratingClass(@NotNull CtClass ctClass) {
    this.currentDownstreamCtClass.set(ctClass);
  }
}
