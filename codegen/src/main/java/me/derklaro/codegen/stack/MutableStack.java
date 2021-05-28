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

package me.derklaro.codegen.stack;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Represents a stack of objects which can grow, change or shrink.
 *
 * @param <T> the type of objects in the stack.
 */
public interface MutableStack<T> extends Stack<T> {
  /**
   * Removes the specified element from the stack.
   *
   * @param t the element to remove.
   * @return the index the element was located previously or {@code -1} if the element was not present.
   */
  int remove(@NotNull T t);

  /**
   * Removes the element at the specified location in the stack.
   *
   * @param index the index of the element to remove.
   * @return the element which was previously located at the specified location.
   */
  @NotNull Optional<T> remove(int index);

  /**
   * Replaces an element of the stack. The new element must equal the
   * old one for the operation to succeed. If the was no old value found
   * the element will be pushed to the last position of the stack.
   *
   * @param newValue the new value to replace the old one with.
   * @return the old value.
   * @see #pushLast(Object)
   */
  @NotNull Optional<T> replace(@NotNull T newValue);

  /**
   * Resets (clears) the stack.
   *
   * @return the same but cleared stack, for chaining.
   */
  @NotNull MutableStack<T> reset();
}
