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

package me.derklaro.codegen.spoon;

import me.derklaro.codegen.processor.TypeProcessor;
import me.derklaro.codegen.stack.generators.GeneratorStack;
import org.jetbrains.annotations.NotNull;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.util.Set;

public class GlobalProcessor extends RootProcessor<CtType<?>> {

  public GlobalProcessor(@NotNull Set<File> sources, @NotNull GeneratorStack stack, @NotNull Set<TypeProcessor> typeProcessors) {
    super(sources, stack, typeProcessors);
  }

  @Override
  public void process(CtType<?> element) {
    for (TypeProcessor handler : this.typeProcessors) {
      if (handler.shouldProcess(element) && !handler.process(element)) {
        throw new RuntimeException("Handler " + handler + " was unable to process " + element);
      }
    }
  }
}
