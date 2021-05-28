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

package me.derklaro.codegen.plugin;

import me.derklaro.codegen.Compiler;
import me.derklaro.codegen.processor.TypeProcessor;
import org.gradle.api.JavaVersion;
import org.gradle.api.file.SourceDirectorySet;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class GradleCompilerConfiguration implements Compiler.CompilerConfiguration {

  private final Set<File> inputFiles;
  private final Set<File> sourceClassPath;

  private int languageLevel;
  private boolean validateCode;

  protected GradleCompilerConfiguration(@NotNull CodeGenerationTask task) {
    this.languageLevel = Integer.parseInt(JavaVersion.toVersion(task.getSourceCompatibility()).getMajorVersion());
    this.validateCode = task.getValidateCode().get();
    this.inputFiles = task.getSources().stream()
      .filter(source -> source instanceof SourceDirectorySet)
      .map(source -> ((SourceDirectorySet) source).getSrcDirs())
      .flatMap(Set::stream)
      .collect(Collectors.toSet());
    this.sourceClassPath = task.getClasspath().getFiles();
  }

  @Override
  public int getLanguageLevel() {
    return this.languageLevel;
  }

  @Override
  public void setLanguageLevel(int languageLevel) {
    this.languageLevel = languageLevel;
  }

  @Override
  public boolean isValidateCode() {
    return this.validateCode;
  }

  @Override
  public void setValidateCode(boolean validateCode) {
    this.validateCode = validateCode;
  }

  @Override
  public @NotNull Set<File> getInputFiles() {
    return this.inputFiles;
  }

  @Override
  public @NotNull Set<File> getSourceClassPath() {
    return this.sourceClassPath;
  }

  @Override
  public @NotNull Set<TypeProcessor> getProcessors() {
    return Collections.emptySet();
  }
}
