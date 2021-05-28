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

package me.derklaro.codegen.stack.defaults;

import me.derklaro.codegen.stack.Stack;
import me.derklaro.codegen.util.ArrayUtility;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractStack<T> implements Stack<T> {

  protected final AtomicReference<T[]> reference;

  protected AbstractStack(@NotNull T[] array) {
    this.reference = new AtomicReference<>(array);
  }

  @Override
  public int pushFirst(@NotNull T t) {
    this.reference.set(ArrayUtils.addFirst(this.reference.get(), t));
    return 0;
  }

  @Override
  public int pushAt(int index, @NotNull T t) {
    this.reference.set(ArrayUtility.addElementAt(this.reference.get(), t, index));
    return index;
  }

  @Override
  public int pushBefore(int index, @NotNull T t) {
    this.reference.set(ArrayUtility.addElementAt(this.reference.get(), t, index - 1));
    return index;
  }

  @Override
  public int pushAfter(int index, @NotNull T t) {
    this.reference.set(ArrayUtility.addElementAt(this.reference.get(), t, index + 1));
    return index;
  }

  @Override
  public int pushLast(@NotNull T t) {
    this.reference.set(ArrayUtils.add(this.reference.get(), t));
    return this.reference.get().length;
  }

  @Override
  public boolean has(int index) {
    T[] array = this.reference.get();
    return index >= 0 && array.length > index;
  }

  @Override
  public @NotNull Optional<T> at(int index) {
    T[] array = this.reference.get();
    if (array.length <= index) {
      return Optional.empty();
    } else {
      return Optional.of(array[index]);
    }
  }

  @Override
  public @NotNull Optional<T> first() {
    T[] array = this.reference.get();
    return array.length == 0 ? Optional.empty() : Optional.of(array[0]);
  }

  @Override
  public @NotNull Optional<T> last() {
    T[] array = this.reference.get();
    if (array.length == 0) {
      return Optional.empty();
    } else {
      return Optional.of(array[array.length - 1]);
    }
  }

  @Override
  public @NotNull @Unmodifiable Collection<T> getFullStack() {
    return Arrays.asList(this.reference.get());
  }

  @NotNull
  @Override
  public Iterator<T> iterator() {
    return ArrayUtility.newArrayIterator(this.reference.get());
  }
}
