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

package me.derklaro.codegen.javaassist;

import javassist.ClassPath;
import javassist.NotFoundException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import spoon.Launcher;
import spoon.SpoonAPI;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

/**
 * An java assist classpath which compiles the source files if needed.
 */
public class CompilingClassPath implements ClassPath {

  protected final Set<File> sourceFiles;
  protected final String[] sourceClassPath;
  protected volatile Path temporaryClassFolder;

  public CompilingClassPath(@NotNull Set<File> sourceFiles, @NotNull String[] sourceClassPath) {
    this.sourceFiles = sourceFiles;
    this.sourceClassPath = sourceClassPath;
  }

  @Override
  public InputStream openClassfile(String classname) throws NotFoundException {
    // provide or compile the class if possible
    Path outputPath = this.provideCompiledClass(classname);
    if (outputPath == null) {
      return null;
    }
    // try to open the compiled class
    try {
      return Files.newInputStream(outputPath);
    } catch (IOException exception) {
      // unable to compile the class
      return null;
    }
  }

  @Override
  public URL find(String classname) {
    try {
      // provide or compile the class if possible
      Path compiledClassPath = this.provideCompiledClass(classname);
      return compiledClassPath == null ? null : compiledClassPath.toUri().toURL();
    } catch (NotFoundException | MalformedURLException exception) {
      // unable to compile the class
      return null;
    }
  }

  protected @Nullable Path provideCompiledClass(@NotNull String classname) throws NotFoundException {
    File file = this.associateFile(classname);
    if (file == null) {
      throw new NotFoundException("Called openClassFile for unknown class");
    }
    // compile the class file if needed
    Path tempOutputPath = this.getTemporaryClassFolder().toAbsolutePath();
    Path outputPath = tempOutputPath.resolve(classname.replace('.', '/') + ".class");
    if (Files.exists(outputPath)) {
      // the class is already compiled
      return outputPath;
    }
    // compile the class
    SpoonAPI spoonAPI = new Launcher();
    // set the source class path for the compiler
    spoonAPI.getEnvironment().setSourceClasspath(this.sourceClassPath);
    // add all input files
    for (File sourceFile : this.sourceFiles) {
      spoonAPI.addInputResource(sourceFile.getAbsolutePath());
    }
    // specifically add the file we want to compile
    spoonAPI.addInputResource(file.getAbsolutePath());
    // env setup
    spoonAPI.getEnvironment().setNoClasspath(true);
    spoonAPI.getEnvironment().setAutoImports(false);
    spoonAPI.getEnvironment().setShouldCompile(true);
    spoonAPI.getEnvironment().setCommentEnabled(true);
    spoonAPI.getEnvironment().setSourceClasspath(this.sourceClassPath);
    spoonAPI.getEnvironment().setBinaryOutputDirectory(tempOutputPath.toString());
    spoonAPI.getEnvironment().setSourceOutputDirectory(tempOutputPath.toFile());
    spoonAPI.run();
    // compilation done
    return outputPath;
  }

  protected @Nullable File associateFile(@NotNull String className) {
    String pathName = className.replace('.', File.separatorChar) + ".java";
    for (File sourceFile : this.sourceFiles) {
      if (sourceFile.isDirectory()) {
        File result = this.associateFileRecursive(pathName, sourceFile);
        if (result != null) {
          return result;
        }
      } else if (sourceFile.getAbsolutePath().endsWith(pathName)) {
        return sourceFile;
      }
    }
    return null;
  }

  protected @Nullable File associateFileRecursive(@NotNull String pathName, @NotNull File directory) {
    File[] files = directory.listFiles();
    if (files != null && files.length > 0) {
      for (File file : files) {
        if (file.isDirectory()) {
          File result = this.associateFileRecursive(pathName, file);
          if (result != null) {
            return result;
          }
        } else if (file.getAbsolutePath().endsWith(pathName)) {
          return file;
        }
      }
    }
    return null;
  }

  public Path getTemporaryClassFolder() {
    if (this.temporaryClassFolder == null) {
      this.temporaryClassFolder = this.createTempFolder();
    }
    return this.temporaryClassFolder;
  }

  protected @NotNull Path createTempFolder() {
    try {
      return Files.createTempDirectory("codegen-" + System.currentTimeMillis());
    } catch (IOException exception) {
      throw new RuntimeException("Unable to create temp directory", exception);
    }
  }
}
