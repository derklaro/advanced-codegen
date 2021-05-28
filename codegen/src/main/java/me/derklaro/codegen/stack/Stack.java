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
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Optional;

/**
 * Represents a stack of objects which can only grow, not change or shrink.
 *
 * @param <T> the type of objects in the stack.
 */
public interface Stack<T> extends Iterable<T> {
  /**
   * Pushes an element to the first position of the stack, moving all
   * previously tracked elements one up.
   *
   * @param t the element to push.
   * @return the position the element was pushed to.
   */
  int pushFirst(@NotNull T t);

  /**
   * Pushes an element to a specific position in the stack, moving the element
   * at and all elements after the {@code index} one up.
   *
   * @param index the index to push the element to.
   * @param t     the element to push.
   * @return the position the element was pushed to.
   */
  int pushAt(int index, @NotNull T t);

  /**
   * Pushes an element to a specific position in the stack, moving the element
   * at and all elements after the {@code index - 1} one up.
   *
   * @param index the index after the location to push the element to.
   * @param t     the element to push.
   * @return the position the element was pushed to.
   */
  int pushBefore(int index, @NotNull T t);

  /**
   * Pushes an element to a specific position in the stack, moving the element
   * at and all elements after the {@code index + 1} one up.
   *
   * @param index the index before the location to push the element to.
   * @param t     the element to push.
   * @return the position the element was pushed to.
   */
  int pushAfter(int index, @NotNull T t);

  /**
   * Pushes an element to the last position of the stack.
   *
   * @param t the element to push.
   * @return the position the element was pushed to.
   */
  int pushLast(@NotNull T t);

  /**
   * Checks if the stack has an element at the specified location.
   *
   * @param index the index to check.
   * @return {@code true} if the stack has an element at the specified location, {@code false} otherwise.
   */
  boolean has(int index);

  /**
   * Get the element at the specified location in the stack.
   *
   * @param index the index of the element to get.
   * @return the element at the specified location.
   */
  @NotNull Optional<T> at(int index);

  /**
   * Get the first element of the stack.
   *
   * @return the first element of the stack.
   */
  @NotNull Optional<T> first();

  /**
   * Get the last element of the stack.
   *
   * @return the last element of the stack.
   */
  @NotNull Optional<T> last();

  /**
   * Get an unmodifiable copy of the whole stack.
   *
   * @return an unmodifiable copy of the whole stack.
   */
  @NotNull @Unmodifiable Collection<T> getFullStack();
}
