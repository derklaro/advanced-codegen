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

package me.derklaro.codegen.generator.result;

import javassist.CannotCompileException;
import javassist.CtClass;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Represents a result of a processed class mapping it's name, package and new bytecode.
 */
public interface ClassResult {
  /**
   * Creates a new builder for a result, using the parameter provided
   * by the given {@code ctClass}.
   *
   * @param ctClass the class to create the builder from.
   * @return a builder for a class result, already using the provided arguments by the {@code ctClass}.
   * @throws IOException            if an i/o error during the compile of the class occurs.
   * @throws CannotCompileException if the compiler is unable to compile the class.
   */
  static @NotNull Builder builder(@NotNull CtClass ctClass) throws IOException, CannotCompileException {
    return builder()
      .classPackage(ctClass.getPackageName())
      .className(ctClass.getSimpleName())
      .bytecode(ctClass.toBytecode());
  }

  /**
   * Creates a new, empty builder instance.
   *
   * @return a new, empty builder instance.
   */
  static @NotNull Builder builder() {
    return new DefaultClassResult.DefaultClassResultBuilder();
  }

  /**
   * Get the package the class result should be located in.
   *
   * @return the package the class result should be located in.
   */
  @NotNull String getPackage();

  /**
   * Get the name of the generated class.
   *
   * @return the name of the generated class.
   */
  @NotNull String getName();

  /**
   * Get the full path of a possible class output file path.
   *
   * @return the full path of a possible class output file path.
   */
  @NotNull Path getOutputFile();

  /**
   * Get the byte code of the compiled class.
   *
   * @return the byte code of the compiled class.
   */
  byte[] getBytecode();

  /**
   * Writes the bytecode to a file specified by {@link #getOutputFile()}.
   *
   * @throws IOException if an i/o error occurs during the write.
   */
  void writeToFile() throws IOException;

  /**
   * Writes the bytecode to a file specified by {@code path}.
   *
   * @param path the path to write the bytecode to.
   * @throws IOException if an i/o error occurs during the write.
   */
  void writeToFile(@NotNull Path path) throws IOException;

  /**
   * A builder for a class result.
   */
  interface Builder {
    /**
     * Specifies the package the class should be located in.
     *
     * @param packageName the package name.
     * @return the same instance of the class, for chaining.
     */
    @NotNull Builder classPackage(@NotNull String packageName);

    /**
     * Specified the class name of the class result.
     *
     * @param className the class name.
     * @return the same instance of the class, for chaining.
     */
    @NotNull Builder className(@NotNull String className);

    /**
     * Sets the full qualified name of the class. It will read
     * the package and class name from it.
     *
     * @param qualifiedName the qualified name of the class.
     * @return the same instance of the class, for chaining.
     */
    @NotNull Builder packageClassName(@NotNull String qualifiedName);

    /**
     * Specifies the bytecode of the class result.
     *
     * @param bytecode the bytecode of the class.
     * @return the same instance of the class, for chaining.
     */
    @NotNull Builder bytecode(byte[] bytecode);

    /**
     * Builds a class result from the provided arguments.
     *
     * @return the created class result.
     * @throws IllegalStateException if either the class name, package name or bytecode is not provided.
     */
    @NotNull ClassResult build();
  }
}
