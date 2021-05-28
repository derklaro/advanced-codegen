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

package me.derklaro.codegen.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Generates a constructor for a generated class.
 *
 * <p>{@link Type#NO_ARGS} will generate a constructor for no arguments.</p>
 *
 * <p>{@link Type#REQUIRED_ARGS} will generate a constructor for all non-final arguments.
 * An argument is non-final when it is marked as {@link OptionalField}.</p>
 *
 * <p>{@link Type#ALL_ARGS} will generate a constructor for all arguments.</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Constructor {
  /**
   * Get all constructor types to generate. An array sized 0 will behave like the
   * annotation is not present. Duplicate entries are omitted.
   *
   * @return all constructor types to generate.
   */
  Type[] types() default {Type.REQUIRED_ARGS};

  /**
   * The available constructor generation types.
   */
  enum Type {
    /**
     * A constructor with no parameters.
     */
    NO_ARGS,
    /**
     * A constructor with parameters for all non-final fields.
     */
    REQUIRED_ARGS,
    /**
     * A constructor with parameters for all fields.
     */
    ALL_ARGS
  }
}
