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

package me.derklaro.codegen.generation;

import javassist.CtNewConstructor;
import javassist.NotFoundException;
import me.derklaro.codegen.annotations.Factory;
import me.derklaro.codegen.annotations.OptionalField;
import me.derklaro.codegen.generator.Generator;
import me.derklaro.codegen.stack.generators.GeneratorStack;
import me.derklaro.codegen.util.BytecodeUtility;
import me.derklaro.codegen.util.MethodFieldPair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ConstructorGenerator implements Generator {

  protected static final Deque<MethodFieldPair> PAIR_EMPTY_DEQUE = new ArrayDeque<>();
  protected static final String OPTIONAL_ANNOTATION = OptionalField.class.getCanonicalName();

  protected final Deque<MethodFieldPair> getterMethods;
  protected final @Nullable Generator factoryGenerator;
  protected final @Nullable Collection<Integer> superParameters;

  public ConstructorGenerator(Deque<MethodFieldPair> getterMethods, @Nullable CtConstructor<?> superConstructor) {
    this.getterMethods = getterMethods;
    this.factoryGenerator = null;
    this.superParameters = this.searchSuperParameters(superConstructor, getterMethods);
  }

  public ConstructorGenerator(CtType<?> type, Deque<MethodFieldPair> getterMethods,
                              @Nullable CtConstructor<?> superConstructor) {
    this.getterMethods = getterMethods;
    this.superParameters = this.searchSuperParameters(superConstructor, getterMethods);

    Factory factory = type.getAnnotation(Factory.class);
    if (factory != null && !factory.location().isEmpty() && !factory.method().isEmpty()) {
      this.factoryGenerator = new FactoryMethodGenerator(factory.location(), factory.method(),
        factory.overrideReturn(), new ArrayDeque<>(getterMethods));
    } else {
      this.factoryGenerator = null;
    }
  }

  public static @NotNull Collection<ConstructorGenerator> noArgs(@NotNull CtType<?> type,
                                                                 @Nullable Collection<? extends CtConstructor<?>> constructors) {
    // find all super constructors with the most amount of argument (all args constructors)
    Collection<? extends CtConstructor<?>> ctConstructors = findConstructors(constructors, true);
    if (ctConstructors != null && !ctConstructors.isEmpty()) {
      return ctConstructors.stream()
        .map(ctConstructor -> new ConstructorGenerator(type, PAIR_EMPTY_DEQUE, ctConstructor))
        .collect(Collectors.toList());
    } else {
      // no super constructors, just generate one
      return Collections.singleton(new ConstructorGenerator(type, PAIR_EMPTY_DEQUE, null));
    }
  }

  public static @NotNull Collection<ConstructorGenerator> requiredArgs(@NotNull CtType<?> type,
                                                                       @NotNull Deque<MethodFieldPair> getterMethods,
                                                                       @Nullable Collection<? extends CtConstructor<?>> constructors) {
    getterMethods.removeIf(pair -> BytecodeUtility.isAnnotationPresent(pair.getMethod(), OPTIONAL_ANNOTATION));
    return allArgs(type, getterMethods, constructors);
  }

  public static @NotNull Collection<ConstructorGenerator> allArgs(@NotNull CtType<?> type,
                                                                  @NotNull Deque<MethodFieldPair> getterMethods,
                                                                  @Nullable Collection<? extends CtConstructor<?>> constructors) {
    getterMethods.removeIf(pair -> pair.getMethod().getType().getQualifiedName().equals("void"));
    // find all super constructors with the most amount of argument (all args constructors)
    Collection<? extends CtConstructor<?>> ctConstructors = findConstructors(constructors, false);
    if (ctConstructors != null && !ctConstructors.isEmpty()) {
      return ctConstructors.stream()
        .map(ctConstructor -> new ConstructorGenerator(type, new ArrayDeque<>(getterMethods), ctConstructor))
        .collect(Collectors.toList());
    } else {
      // no super constructors, just generate one
      return Collections.singleton(new ConstructorGenerator(type, new ArrayDeque<>(getterMethods), null));
    }
  }

  protected static @Nullable Collection<? extends CtConstructor<?>> findConstructors(@Nullable Collection<? extends CtConstructor<?>> constructors,
                                                                                     boolean lestArguments) {
    // just return null when there is no possibility for a constructor
    if (constructors == null || constructors.isEmpty()) {
      return null;
    }
    // filter the constructors with the most or least amount of parameters
    return constructors.stream()
      .filter(Objects::nonNull)
      .distinct()
      .collect(Collectors.collectingAndThen(Collectors.groupingBy(constructor -> constructor, Collectors.counting()),
        map -> {
          Integer parameterCount = (lestArguments
            ? map.values().stream().min(Comparator.naturalOrder())
            : map.values().stream().max(Comparator.naturalOrder())
          ).map(Long::intValue).orElse(null);
          if (parameterCount == null) {
            return null;
          } else {
            Map<Integer, Set<CtConstructor<?>>> countToConstructors = map.entrySet().stream()
              .collect(Collectors.groupingBy(
                entry -> entry.getValue().intValue(),
                Collectors.mapping(Map.Entry::getKey, Collectors.toSet())
              ));
            return countToConstructors.get(parameterCount);
          }
        }));
  }

  @Override
  public void applyTo(@NotNull GeneratorStack stack) throws Exception {
    try {
      // check if the constructor we want to create already exists
      String desc = BytecodeUtility.provideVagueConstructorSignature(this.getterMethods);
      if (stack.getGeneratingClass().getConstructor(desc) != null) {
        // the constructor already exists
        return;
      }
    } catch (NotFoundException ignored) {
    }
    // no such constructor, make one
    javassist.CtConstructor constructor = CtNewConstructor.make(String.format(
      "public %s(%s) { %s }",
      stack.getGeneratingClass().getSimpleName(),
      this.joinParameterNames(this.getterMethods),
      this.provideConstructorBody(this.getterMethods)
    ), stack.getGeneratingClass());
    stack.getGeneratingClass().addConstructor(constructor);
    // generate factory method if needed
    if (this.factoryGenerator != null) {
      this.factoryGenerator.applyTo(stack);
    }
  }

  protected @NotNull String joinParameterNames(@NotNull Deque<MethodFieldPair> getterMethods) {
    StringBuilder builder = new StringBuilder();
    // process the getter methods
    for (MethodFieldPair pair : getterMethods) {
      // build the parameter
      builder
        .append(pair.getReturnType())
        .append(" ")
        .append(pair.getAssociatedFieldName())
        .append(",");
    }
    return builder.substring(0, builder.length() - 1);
  }

  protected @NotNull String provideConstructorBody(@NotNull Deque<MethodFieldPair> getterMethods) {
    StringBuilder builder = new StringBuilder();
    // add super arguments for super constructor
    if (this.superParameters != null && !this.superParameters.isEmpty()) {
      builder.append("super(");
      for (Integer parameter : this.superParameters) {
        builder.append("$").append(parameter).append(",");
      }
      builder.delete(builder.length() - 1, builder.length()).append(");");
    }
    // process getter methods
    int i = 1;
    for (MethodFieldPair pair : getterMethods) {
      builder
        .append("this.")
        .append(pair.getAssociatedFieldName())
        .append(" = $")
        .append(i++)
        .append(";");
    }
    return builder.toString();
  }

  protected @Nullable Collection<Integer> searchSuperParameters(@Nullable CtConstructor<?> constructor,
                                                                @NotNull Deque<MethodFieldPair> methodFieldPairs) {
    // check if we have a constructor and if the constructors has parameters
    if (constructor != null && !constructor.getParameters().isEmpty()) {
      Collection<Integer> superParameters = new HashSet<>();
      // find all parameters we need to add and their indexes
      for (CtParameter<?> parameter : constructor.getParameters()) {
        int index = this.getIndex(methodFieldPairs, parameter.getType().getQualifiedName(), parameter.getSimpleName());
        if (index == -1) {
          // no such parameter yet
          methodFieldPairs.add(new MethodFieldPair(parameter.getType().getQualifiedName(), null,
            parameter.getSimpleName()));
          superParameters.add(methodFieldPairs.size());
        } else {
          // just add the index
          superParameters.add(index);
        }
      }
      // return the parameters we found
      return superParameters;
    } else {
      // no super constructor or no parameters
      return null;
    }
  }

  protected int getIndex(@NotNull Deque<MethodFieldPair> methodFieldPairs, @NotNull String type, @NotNull String fieldName) {
    // get the index of a field or -1 in the given deque
    int index = 1;
    for (MethodFieldPair pair : methodFieldPairs) {
      if (pair.getReturnType().equals(type) && pair.getAssociatedFieldName().equals(fieldName)) {
        return index;
      }
      // next parameter
      index++;
    }
    // no matching parameter found
    return -1;
  }
}
