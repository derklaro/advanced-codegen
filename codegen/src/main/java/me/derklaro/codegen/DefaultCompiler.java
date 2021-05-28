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
import me.derklaro.codegen.javaassist.CompilingClassPath;
import me.derklaro.codegen.processor.TypeProcessor;
import me.derklaro.codegen.processor.defaults.GenerationTypeProcessor;
import me.derklaro.codegen.processor.defaults.NonNullParameterProcessor;
import me.derklaro.codegen.spoon.GlobalProcessor;
import me.derklaro.codegen.spoon.RootProcessor;
import me.derklaro.codegen.stack.classes.DefaultClassStack;
import me.derklaro.codegen.stack.generators.DefaultGeneratorStack;
import me.derklaro.codegen.stack.generators.GeneratorStack;
import org.jetbrains.annotations.NotNull;
import spoon.Launcher;
import spoon.SpoonAPI;
import spoon.SpoonModelBuilder;
import spoon.compiler.Environment;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

final class DefaultCompiler implements Compiler {

  private static @NotNull String[] toPathArray(@NotNull Set<File> files) {
    return files.stream().map(File::getAbsolutePath).toArray(String[]::new);
  }

  @Override
  public @NotNull SpoonModelBuilder provideModelBuilder(@NotNull CompilerConfiguration compilerConfiguration) {
    // add the default processor to the spoon api
    SpoonAPI spoon = new Launcher();
    spoon.addProcessor(GlobalProcessor.class.getCanonicalName());
    // setup the environment based on the configuration
    Environment environment = spoon.getEnvironment();
    environment.setNoClasspath(!compilerConfiguration.isValidateCode());
    environment.setComplianceLevel(compilerConfiguration.getLanguageLevel());
    // create the actual spoon based compiler
    SpoonModelBuilder compiler = spoon.createCompiler();
    compiler.setSourceClasspath(toPathArray(compilerConfiguration.getSourceClassPath()));
    // add the input sources provided by the configuration
    for (File file : compilerConfiguration.getInputFiles()) {
      compiler.addInputSource(file);
    }
    // build the compiler instance
    compiler.build();
    return compiler;
  }

  @Override
  public @NotNull ClassPool provideClassPool(@NotNull CompilerConfiguration compilerConfiguration) throws Exception {
    String[] inputPaths = toPathArray(compilerConfiguration.getSourceClassPath());
    // create pool
    ClassPool classPool = ClassPool.getDefault();
    classPool.appendPathList(String.join(File.pathSeparator, inputPaths));
    classPool.appendClassPath(new CompilingClassPath(compilerConfiguration.getInputFiles(), inputPaths));
    // done creation, nothing more to configure here
    return classPool;
  }

  @Override
  public @NotNull GeneratorStack compile(@NotNull CompilerConfiguration compilerConfiguration) throws Exception {
    // create generator stack
    GeneratorStack stack = new DefaultGeneratorStack(new DefaultClassStack());
    // create the root processing units
    ClassPool classPool = this.provideClassPool(compilerConfiguration);
    SpoonModelBuilder modelBuilder = this.provideModelBuilder(compilerConfiguration);
    // provide the set of default processors we have
    Set<TypeProcessor> processors = new HashSet<>(Arrays.asList(new GenerationTypeProcessor(classPool),
      new NonNullParameterProcessor(classPool)));
    // add the custom processors from the configuration
    processors.addAll(compilerConfiguration.getProcessors());
    // create our root processor instance
    RootProcessor<?> processor = new GlobalProcessor(compilerConfiguration.getInputFiles(), stack, processors);
    // post the root processor to the model builder
    modelBuilder.process(Collections.singleton(processor));
    // return the create generator stack used by the processor to process the resources
    return stack;
  }
}
