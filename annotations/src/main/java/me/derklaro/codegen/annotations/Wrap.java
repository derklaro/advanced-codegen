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
 * Wraps the return value of a method into the provided {@link #in()}.
 * This will only work for getter methods generated in classes
 * annotated with {@link Generate}.
 * <p>
 * Example:
 * Your method in your interface may look like:
 * <pre>
 * <code>
 *  {@literal @}Wrap(in = "java.util.Optional.ofNullable(%s)", returnType = "java.lang.String")
 *  {@literal @}Optional{@literal <}String{@literal >} getStreetName();
 * </code>
 * </pre>
 * <br>
 * This will wrap the return value of your method into {@code Optional.ofNullable}.
 * Please note the {@code %s}, that the location for the generator to place the actual field
 * return value in!
 * The extra {@code returnType} which is provided in the annotation is necessary to
 * find out the type of the field. We don't want to actually store the optional in the class
 * but {@code String} and wrap that string in the method call.
 * <br>
 * The generated implementation will look like this:
 * <pre>
 * <code>
 *  public Optional{@literal <}String{@literal >} getStreetName() {
 *    return Optional.ofNullable(this.streetName);
 *  }
 * </code>
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Wrap {
  /**
   * Get the method call to wrap the return value in. Must contain exactly
   * one {@code %s} to indicate the parameter (field) location.
   *
   * @return the method call to wrap the return value in.
   */
  String in();

  /**
   * Get the actual (unwrapped) return type of the method.
   *
   * @return the actual (unwrapped) return type of the method.
   */
  String returnType();
}
