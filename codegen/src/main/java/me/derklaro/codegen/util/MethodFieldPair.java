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

import me.derklaro.codegen.annotations.Wrap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import spoon.reflect.declaration.CtMethod;

import java.util.Map;

/**
 * Represents a mapping of a method to a field name.
 */
@ApiStatus.Internal
public class MethodFieldPair implements Map.Entry<CtMethod<?>, String> {

  protected final String returnType;
  protected final CtMethod<?> method;
  protected final String associatedFieldName;

  public MethodFieldPair(CtMethod<?> method, String associatedFieldName) {
    this(method, associatedFieldName, null);
  }

  public MethodFieldPair(CtMethod<?> method, String associatedFieldName, @Nullable Wrap wrappingConfiguration) {
    this.method = method;
    this.associatedFieldName = associatedFieldName;
    this.returnType = wrappingConfiguration != null
      ? wrappingConfiguration.returnType()
      : method.getType().getQualifiedName();
  }

  public MethodFieldPair(String returnType, CtMethod<?> method, String associatedFieldName) {
    this.returnType = returnType;
    this.method = method;
    this.associatedFieldName = associatedFieldName;
  }

  public @NotNull CtMethod<?> getMethod() {
    return this.method;
  }

  public @NotNull String getAssociatedFieldName() {
    return this.associatedFieldName;
  }

  public @NotNull String getReturnType() {
    return this.returnType;
  }

  @Override
  public CtMethod<?> getKey() {
    return this.method;
  }

  @Override
  public String getValue() {
    return this.associatedFieldName;
  }

  @Override
  public String setValue(String value) {
    throw new UnsupportedOperationException("Class is read-only");
  }
}
