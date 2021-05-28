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
import me.derklaro.codegen.generator.result.ClassResult;
import me.derklaro.codegen.stack.generators.GeneratorStack;
import org.gradle.api.Action;
import org.gradle.api.NonNullApi;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileCopyDetails;
import org.gradle.api.file.FileTree;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CompileClasspath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@NonNullApi
public class CodeGenerationTask extends AbstractCompile implements Action<FileCopyDetails> {

  protected final Set<Object> sources;
  protected final Property<Boolean> validateCode;
  protected final Set<String> generatedOutputFiles;

  public CodeGenerationTask() {
    this.sources = new HashSet<>();
    this.generatedOutputFiles = new HashSet<>();
    this.validateCode = this.getProject().getObjects().property(Boolean.class).value(true);
  }

  @TaskAction
  public void runGeneration() throws Exception {
    // remove the destination directory
    this.getProject().delete(this.getDestinationDir());
    // run the compile
    GeneratorStack stack = Compiler.createDefault().compile(new GradleCompilerConfiguration(this));
    // write the generated classes into the destination directory
    Path destinationDirectory = this.getDestinationDir().toPath();
    // process the class result
    for (ClassResult result : stack.classStack().getFullStack()) {
      // create the parent directory (for the package if there is one)
      Path outputDirectory = destinationDirectory.resolve(result.getPackage().replace('.', File.separatorChar));
      // create the directory
      Files.createDirectories(outputDirectory);
      // get the name of the class in the output directory
      Path fileTarget = outputDirectory.resolve(result.getName() + ".class");
      // dump the class output into a new file in the directory
      Files.write(fileTarget, result.getBytecode(), StandardOpenOption.CREATE);
      // append the file target to the output files set for later use in the copy action
      // gradle always uses '/' instead of the system dependant separator char
      this.generatedOutputFiles.add(destinationDirectory.relativize(fileTarget).toString().replace(File.separatorChar, '/'));
    }
  }

  // configuration

  @Override
  @CompileClasspath
  @InputFiles
  public FileCollection getClasspath() {
    return super.getClasspath();
  }

  @Override
  public void setSource(@NotNull FileTree source) {
    this.setSource((Object) source);
  }

  @Override
  public void setSource(@NotNull Object source) {
    this.sources.clear();
    this.sources.add(source);

    super.setSource(source);
  }

  @Override
  public @NotNull SourceTask source(Object... sources) {
    Collections.addAll(this.sources, sources);
    return super.source(sources);
  }

  public @Internal Set<Object> getSources() {
    return this.sources;
  }

  public @Input Property<Boolean> getValidateCode() {
    return this.validateCode;
  }

  @Override
  public void execute(@NotNull FileCopyDetails fileCopyDetails) {
    if (this.generatedOutputFiles.contains(fileCopyDetails.getPath())
      && this.getDestinationDir().toPath().relativize(fileCopyDetails.getFile().toPath()).startsWith("..")) {
      // exclude duplicate files from the copy when the file already exists
      // this is useful for factory methods which will duplicated in the final jar
      // file if we don't exclude them
      fileCopyDetails.exclude();
    }
  }
}
