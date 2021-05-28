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

import me.derklaro.codegen.stack.generators.GeneratorStack;
import org.jetbrains.annotations.NotNull;
import spoon.reflect.declaration.CtType;

/**
 * Represents a processor of a specific type reference.
 */
public interface TypeProcessor {
  /**
   * Checks weather or not the given {@code type} should be processed by this handler.
   *
   * @param type the type to check.
   * @return {@code true} if the type should be processed by this handler, {@code false} otherwise.
   */
  boolean shouldProcess(@NotNull CtType<?> type);

  /**
   * Processes the given reference.
   *
   * @param type the type reference to process.
   * @return {@code true} if the processing was successful, {@code false} otherwise.
   */
  boolean process(@NotNull CtType<?> type);

  /**
   * Handles the post event after all types have been posted to all handlers.
   *
   * @param stack the generation stack, it is the same for all processors added
   * @throws Exception if any exception occurs during the processing
   */
  void postProcess(@NotNull GeneratorStack stack) throws Exception;
}
