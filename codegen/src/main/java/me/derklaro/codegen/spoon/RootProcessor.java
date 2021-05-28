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
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.util.Set;

/**
 * The parent processor for the generator.
 *
 * @param <E> the type of CtType processed and handled by this processor.
 */
public abstract class RootProcessor<E extends CtType<?>> extends AbstractProcessor<E> {

  protected final Set<File> sources;
  protected final GeneratorStack generatorStack;
  protected final Set<TypeProcessor> typeProcessors;

  /**
   * Constructs a root processor object.
   *
   * @param sources        the source files used for generation.
   * @param stack          the generator stack this processor should use to call the registered processors.
   * @param typeProcessors The handlers for all processing targets of this processor.
   */
  protected RootProcessor(@NotNull Set<File> sources, @NotNull GeneratorStack stack, @NotNull Set<TypeProcessor> typeProcessors) {
    this.sources = sources;
    this.generatorStack = stack;
    this.typeProcessors = typeProcessors;
  }

  /**
   * Provides an unmodifiable view of the current processing result.
   *
   * @return an unmodifiable view of the current processing result.
   */
  public @NotNull GeneratorStack getProcessingResult() {
    return this.generatorStack;
  }

  @Override
  public boolean isToBeProcessed(E candidate) {
    File candidateLocation = candidate.getPosition().getCompilationUnit().getFile();
    for (File source : this.sources) {
      if (candidateLocation.equals(source)) {
        // check if the candidate is the specific file
        return true;
      } else if (source.isDirectory() && candidateLocation.toString().startsWith(source.toString())) {
        // check if the source is a directory and if the candidate is in the directory
        return true;
      }
    }
    // neither a specific source nor a directory source for the candidate
    return false;
  }

  @Override
  public void processingDone() {
    for (TypeProcessor handler : this.typeProcessors) {
      try {
        handler.postProcess(this.generatorStack);
      } catch (Exception exception) {
        throw new RuntimeException("Unable to finish generation in handler " + handler, exception);
      }
    }
  }
}
