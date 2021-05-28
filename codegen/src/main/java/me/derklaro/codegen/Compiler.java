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

package me.derklaro.codegen;

import javassist.ClassPool;
import me.derklaro.codegen.processor.TypeProcessor;
import me.derklaro.codegen.stack.generators.GeneratorStack;
import org.jetbrains.annotations.NotNull;
import spoon.SpoonModelBuilder;

import java.io.File;
import java.util.Set;

/**
 * Represents a compiler/processor for all configured classes.
 */
public interface Compiler {
  /**
   * Creates a new, default instance of the compiler.
   *
   * @return the created compiler.
   */
  static @NotNull Compiler createDefault() {
    return new DefaultCompiler();
  }

  /**
   * Provides the actual model builder (compiler) for this compiler to use.
   *
   * @param compilerConfiguration the configuration of the compiler.
   * @return the created model builder.
   * @throws Exception if an exception occurs during the compiler init.
   */
  @NotNull SpoonModelBuilder provideModelBuilder(@NotNull CompilerConfiguration compilerConfiguration) throws Exception;

  /**
   * Provides the class pool for the actual class/method/field generation.
   *
   * @param compilerConfiguration the configuration of the compiler.
   * @return the created class pool.
   * @throws Exception if an exception occurs during the class pool init.
   */
  @NotNull ClassPool provideClassPool(@NotNull CompilerConfiguration compilerConfiguration) throws Exception;

  /**
   * Compiles/processes the files which are required to be compiled/processed.
   *
   * @param compilerConfiguration the configuration of the compiler.
   * @return the stack of generators which were called during the compile/processing.
   * @throws Exception if an exception occurs during the compile.
   */
  @NotNull GeneratorStack compile(@NotNull CompilerConfiguration compilerConfiguration) throws Exception;

  /**
   * Represents a configuration for the compiler.
   */
  interface CompilerConfiguration {
    /**
     * Get the language level the compiler should target.
     *
     * @return the language level the compiler should target.
     */
    int getLanguageLevel();

    /**
     * Sets the language level the compiler should target.
     *
     * @param languageLevel the language level the compiler should target.
     */
    void setLanguageLevel(int languageLevel);

    /**
     * Get weather or not the compiler should validate code before compiling it.
     *
     * @return if the compiler validates code before compiling.
     */
    boolean isValidateCode();

    /**
     * Sets weather or not the compiler should validate code before compiling it.
     *
     * @param validateCode if the compiler validates code before compiling.
     */
    void setValidateCode(boolean validateCode);

    /**
     * Get the input files needed for the compilation.
     *
     * @return the input files needed for the compilation.
     */
    @NotNull Set<File> getInputFiles();

    /**
     * Get the class path of the classes which are added
     * to the runtime (such as dependencies).
     *
     * @return the source class path to work with.
     */
    @NotNull Set<File> getSourceClassPath();

    /**
     * Get the extra processors which should be applied to the compiler.
     *
     * @return the extra processors which should be applied to the compiler.
     */
    @NotNull Set<TypeProcessor> getProcessors();
  }
}
