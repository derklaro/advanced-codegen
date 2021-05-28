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

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.CopySpec;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.jetbrains.annotations.NotNull;

public class CodeGeneratorRootPlugin implements Plugin<Project> {

  protected Configuration classPath;

  @Override
  public void apply(@NotNull Project project) {
    // apply the java plugin to get access to it's configuration
    project.getPlugins().apply(JavaPlugin.class);
    // find the main source set from the java plugin
    SourceSet mainSourceSet = project.getConvention()
      .getPlugin(JavaPluginConvention.class)
      .getSourceSets()
      .getByName(SourceSet.MAIN_SOURCE_SET_NAME);
    // configure the task
    CodeGenerationTask task = project.getTasks().create("codeGen", CodeGenerationTask.class);
    task.source(mainSourceSet.getAllJava());
    task.conventionMapping("classpath", () -> this.classPath);
    // set output directory of task
    task.getDestinationDirectory().set(project.getLayout().getBuildDirectory().dir("codegen"));
    // include the generated classes in the jar
    ((CopySpec) project.getTasks().getByName(mainSourceSet.getJarTaskName()))
      .from(task)
      .eachFile(task);

    project.afterEvaluate(evaluatedProject -> {
      this.classPath = evaluatedProject.getConfigurations()
        .getByName(mainSourceSet.getCompileClasspathConfigurationName())
        .copyRecursive();
      evaluatedProject.getDependencies().add(mainSourceSet.getImplementationConfigurationName(), project.files(task));
    });
  }
}
