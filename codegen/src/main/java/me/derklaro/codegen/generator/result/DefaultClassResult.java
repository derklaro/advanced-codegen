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

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

final class DefaultClassResult implements ClassResult {

  private final String className;
  private final String packageName;
  private final Path outputFilePath;
  private final byte[] classByteCode;

  public DefaultClassResult(String className, String packageName, byte[] classByteCode) {
    this.className = className;
    this.packageName = packageName;
    this.classByteCode = classByteCode;
    this.outputFilePath = Paths.get(packageName.replace('.', File.separatorChar), className + ".class");
  }

  @Override
  public @NotNull String getPackage() {
    return this.packageName;
  }

  @Override
  public @NotNull String getName() {
    return this.className;
  }

  @Override
  public @NotNull Path getOutputFile() {
    return this.outputFilePath;
  }

  @Override
  public byte[] getBytecode() {
    return this.classByteCode;
  }

  @Override
  public void writeToFile() throws IOException {
    this.writeToFile(this.outputFilePath);
  }

  @Override
  public void writeToFile(@NotNull Path path) throws IOException {
    Files.write(this.outputFilePath, this.classByteCode, StandardOpenOption.CREATE);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (o == null || this.getClass() != o.getClass()) {
      return false;
    } else {
      DefaultClassResult that = (DefaultClassResult) o;
      return Objects.equals(this.className, that.className) && Objects.equals(this.packageName, that.packageName);
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.className, this.packageName);
  }

  static final class DefaultClassResultBuilder implements ClassResult.Builder {

    private String className;
    private String packageName;
    private byte[] classByteCode;

    @Override
    public @NotNull Builder classPackage(@NotNull String packageName) {
      this.packageName = packageName;
      return this;
    }

    @Override
    public @NotNull Builder className(@NotNull String className) {
      this.className = className;
      return this;
    }

    @Override
    public @NotNull Builder packageClassName(@NotNull String qualifiedName) {
      int index = qualifiedName.lastIndexOf('.');
      if (index == -1) {
        this.className = qualifiedName;
        this.packageName = "";
      } else if (index + 1 == qualifiedName.length()) {
        throw new IllegalStateException("Qualified name is malformed: " + qualifiedName);
      } else {
        this.className = qualifiedName.substring(index + 1);
        this.packageName = qualifiedName.substring(0, index);
      }

      return this;
    }

    @Override
    public @NotNull Builder bytecode(byte[] bytecode) {
      this.classByteCode = bytecode;
      return this;
    }

    @Override
    public @NotNull ClassResult build() {
      if (this.packageName == null || this.className == null || this.classByteCode == null) {
        throw new IllegalStateException("Incomplete builder");
      } else {
        return new DefaultClassResult(this.className, this.packageName, this.classByteCode);
      }
    }
  }
}
