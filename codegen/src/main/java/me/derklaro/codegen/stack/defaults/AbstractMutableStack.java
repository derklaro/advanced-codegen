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

import me.derklaro.codegen.stack.MutableStack;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Supplier;

public abstract class AbstractMutableStack<T> extends AbstractStack<T> implements MutableStack<T> {

  protected final Supplier<T[]> arrayCreator;

  public AbstractMutableStack(@NotNull Supplier<T[]> arrayCreator) {
    super(arrayCreator.get());
    this.arrayCreator = arrayCreator;
  }

  @Override
  public int remove(@NotNull T t) {
    int index = ArrayUtils.indexOf(this.reference.get(), t);
    if (index == -1) {
      return -1;
    } else {
      this.reference.set(ArrayUtils.remove(this.reference.get(), index));
      return index;
    }
  }

  @Override
  public @NotNull Optional<T> remove(int index) {
    T[] array = this.reference.get();
    if (array.length > index) {
      T element = array[index];
      this.reference.set(ArrayUtils.remove(this.reference.get(), index));
      return Optional.of(element);
    } else {
      return Optional.empty();
    }
  }

  @Override
  public @NotNull Optional<T> replace(@NotNull T newValue) {
    int index = ArrayUtils.indexOf(this.reference.get(), newValue);
    if (index != -1) {
      T[] array = this.reference.get();
      T oldValue = array[index];
      array[index] = newValue;

      return Optional.of(oldValue);
    } else {
      this.pushLast(newValue);
      return Optional.empty();
    }
  }

  @Override
  public @NotNull MutableStack<T> reset() {
    this.reference.set(this.arrayCreator.get());
    return this;
  }
}
