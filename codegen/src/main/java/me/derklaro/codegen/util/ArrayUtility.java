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

package me.derklaro.codegen.util;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.NoSuchElementException;

@ApiStatus.Internal
public final class ArrayUtility {

  private ArrayUtility() {
    throw new UnsupportedOperationException();
  }

  public static @NotNull <T> T[] addElementAt(@NotNull T[] input, @NotNull T element, int index) {
    int growCount = index - input.length;
    // grow the array
    T[] array;
    if (growCount > 0) {
      array = grow(input, growCount);
    } else {
      array = grow(input, 1);
    }
    // copy the array to the target size
    System.arraycopy(input, 0, array, 0, index);
    System.arraycopy(input, index, array, index + 1, input.length - index - 1);
    // set the element at the specified index
    array[index] = element;
    return array;
  }

  @SuppressWarnings("unchecked")
  private static @NotNull <T> T[] grow(@NotNull T[] input, int size) {
    return (T[]) Array.newInstance(input.getClass().getComponentType(), input.length + size);
  }

  public static @NotNull <T> Iterator<T> newArrayIterator(@NotNull T[] elements) {
    return new ArrayIterator<>(elements);
  }

  private static final class ArrayIterator<T> implements Iterator<T> {

    private final T[] elements;
    private int index;

    public ArrayIterator(@NotNull T[] elements) {
      this.elements = elements;
    }

    @Override
    public boolean hasNext() {
      return this.index < this.elements.length;
    }

    @Override
    public T next() {
      if (this.index >= this.elements.length) {
        throw new NoSuchElementException();
      } else {
        return this.elements[this.index++];
      }
    }
  }
}
