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
 * Defines that a specific method should invoke another method.
 *
 * <p>For example:
 * {@literal @}Invoke(invocations = {{@literal @}SingleInvoke(clazz = "handler.TaskManager", method = "update")}, returns = ReturnValue.SELF)
 * public abstract void update();
 * <br>
 * Will call the {@code update()} method in {@code handler.TaskManager} and returns the
 * instance of the generated class.</p>
 *
 * <p>This is especially useful for keeping an interface nice and clean or
 * invoke methods which are only available in the implementation of the
 * api.</p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Invoke {
  /**
   * Get the invocations of the method done before returning.
   *
   * @return the invocations of the method done before returning.
   */
  SingleInvoke[] invocations() default {};

  /**
   * Get the return value of the method invocation.
   *
   * <p>An {@literal @}Returns annotation must be present for
   * a return value of {@code OTHER}.</p>
   *
   * @return the return value of the method invocation.
   */
  ReturnValue returns() default ReturnValue.NONE;

  /**
   * Defines the return value of the method call. Defaults to {@code DETECT}.
   */
  enum ReturnValue {
    /**
     * Returns nothing. Used for void methods.
     */
    NONE,
    /**
     * Returns the self value of the instance. Example (written): <p>return this;</p>
     */
    SELF,
    /**
     * Returns the result of the last method call coming from the {@code invocations} stack.
     */
    METHOD_CALL
  }

  /**
   * Represents an invocable method.
   */
  @Target(ElementType.METHOD) @Retention(RetentionPolicy.RUNTIME) @interface SingleInvoke {
    /**
     * Get the name of the method to invoke.
     *
     * <p>$0 represents the {@code this} argument.</p>
     * <p>$1, $2 and so on are representing the method arguments.</p>
     *
     * @return the name of the method to invoke.
     */
    String method();
  }
}
