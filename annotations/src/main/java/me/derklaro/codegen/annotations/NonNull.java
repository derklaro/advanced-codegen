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

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is a specific {@code NotNull} annotation
 * which will generate a check if a specific parameter is
 * null or not.
 */
@NotNull
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface NonNull {
  /**
   * The default null message template used when no other message is supplied.
   */
  String DEFAULT_TEMPLATE_MESSAGE = "Argument index {0} must not be null";

  /**
   * Get the handler which is used to generate the null check.
   *
   * <p>{@link Handler#CUSTOM} takes care of the provided
   * {@link #message()} and {@link #exception()} and will generate
   * a basic null check and exception throw.</p>
   *
   * <p>{@link Handler#GUAVA_PRECONDITIONS} uses the guava preconditions
   * class and invokes {@code Preconditions.checkNotNull(Object, Object)}.
   * This method only uses the {@link #message()} if supplied.</p>
   *
   * <p>{@link Handler#APACHE_VALIDATE} uses the apache3 validate
   * class and invokes {@code Validate.notNull(Object, Object, Object...}.
   * This method only uses the {@link #message()} if supplied.</p>
   *
   * @return the handler which is used to generate the null check.
   */
  Handler handler() default Handler.CUSTOM;

  /**
   * Get the custom exception message template. {@code {0}} indicates
   * the parameter name.
   *
   * <p>An empty value defaults to the default value.</p>
   *
   * @return the custom exception message.
   */
  String message() default DEFAULT_TEMPLATE_MESSAGE;

  /**
   * Get the exception type which should be thrown when a null value is detected.
   *
   * <p>Default to {@link NullPointerException}.</p>
   *
   * @return the exception type which should be thrown when a null value is detected.
   */
  Class<? extends Exception> exception() default NullPointerException.class;

  /**
   * Represents all supported generation methods for non-null checking.
   */
  enum Handler {
    /**
     *
     */
    CUSTOM,
    /**
     *
     */
    GUAVA_PRECONDITIONS,
    /**
     *
     */
    APACHE_VALIDATE
  }
}
