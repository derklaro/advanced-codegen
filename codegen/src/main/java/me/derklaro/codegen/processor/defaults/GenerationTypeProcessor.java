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

package me.derklaro.codegen.processor.defaults;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import me.derklaro.codegen.annotations.Constructor;
import me.derklaro.codegen.annotations.Equals;
import me.derklaro.codegen.annotations.FieldName;
import me.derklaro.codegen.annotations.Generate;
import me.derklaro.codegen.annotations.HashCode;
import me.derklaro.codegen.annotations.Invoke;
import me.derklaro.codegen.annotations.ToString;
import me.derklaro.codegen.annotations.Wrap;
import me.derklaro.codegen.generation.ConstructorGenerator;
import me.derklaro.codegen.generation.EqualsGenerator;
import me.derklaro.codegen.generation.FieldGenerator;
import me.derklaro.codegen.generation.HashCodeGenerator;
import me.derklaro.codegen.generation.MethodGenerator;
import me.derklaro.codegen.generation.MethodNonNullParameterGenerator;
import me.derklaro.codegen.generation.ToStringGenerator;
import me.derklaro.codegen.generator.Generator;
import me.derklaro.codegen.processor.AbstractTypeProcessor;
import me.derklaro.codegen.util.BytecodeUtility;
import me.derklaro.codegen.util.MethodFieldPair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtInterface;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtTypeReference;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenerationTypeProcessor extends AbstractTypeProcessor {

  protected static final String INVOKE_ANNOTATION = Invoke.class.getCanonicalName();
  protected static final String GENERATE_ANNOTATION = Generate.class.getCanonicalName();
  protected static final String GENERATE_EXCLUDE_ANNOTATION = Generate.Exclude.class.getCanonicalName();

  protected static final Set<Pattern> METHOD_NAMING_PATTERN = new CopyOnWriteArraySet<>(Arrays.asList(
    Pattern.compile("^get([A-Z].*)"), // getter
    Pattern.compile("^is([A-Z].*)"), // boolean getter
    Pattern.compile("^(has[A-Z].*)"), // has getter
    Pattern.compile("^keeps([A-Z].*)") // keeps getter
  ));
  protected static final Pattern MUTATOR_PATTERN = Pattern.compile("^set([A-Z].*)");

  protected final boolean allowFluentMethods;

  public GenerationTypeProcessor(@NotNull ClassPool classPool) {
    this(classPool, true);
  }

  public GenerationTypeProcessor(ClassPool classPool, boolean allowFluentMethods) {
    super(classPool);
    this.allowFluentMethods = allowFluentMethods;
  }

  @Override
  public boolean shouldProcess(@NotNull CtType<?> type) {
    return BytecodeUtility.isAnnotationPresent(type, GENERATE_ANNOTATION);
  }

  @Override
  public boolean process(@NotNull CtType<?> type) {
    // processing data holders
    Set<String> visitedFields = new HashSet<>();
    Set<String> methodDescriptors = new HashSet<>();
    Deque<MethodFieldPair> processedGetterMethods = new ArrayDeque<>();
    // store constructors of the class supertype
    Collection<? extends CtConstructor<?>> constructors = this.getSuperClassConstructors(type);

    Deque<CtType<?>> processingQueue = new ArrayDeque<>();
    processingQueue.push(type);

    while (!processingQueue.isEmpty()) {
      CtType<?> processingType = processingQueue.pop();
      // processing of the methods
      for (CtMethod<?> method : processingType.getMethods()) {
        // check if the method is excluded from generation
        Set<ModifierKind> modifiers = method.getModifiers();
        if (!modifiers.contains(ModifierKind.ABSTRACT)) {
          // ignore implemented methods
          continue;
        }
        if (BytecodeUtility.isAnnotationPresent(method, GENERATE_EXCLUDE_ANNOTATION)) {
          // ignore excluded methods
          methodDescriptors.add(BytecodeUtility.provideMethodSignature(method));
          continue;
        }
        // get the method descriptor and check if we already processed the method
        if (!methodDescriptors.add(BytecodeUtility.provideMethodSignature(method))) {
          continue;
        }
        // check if we need a non-null parameter processor for the method
        @Nullable MethodNonNullParameterGenerator generator = NonNullParameterProcessor.findConfigurations(method);
        // get the field name for the method name
        String fieldName = this.associateSetterToFieldName(method);
        if (fieldName != null) {
          // it is a setter method.
          // push the creation request to the associated queue
          Deque<Generator> pendingGenerations = this.pendingGenerations.computeIfAbsent(type,
            $ -> new ArrayDeque<>());

          pendingGenerations.offerLast(new MethodGenerator(method, String.format("this.%s = $1;", fieldName)));
          // push the field after the method because the field must be there before
          // we can compile the method
          if (visitedFields.add(fieldName)) {
            pendingGenerations.push(new FieldGenerator(fieldName, method.getType(), method));
          }
          // add the non-null generator if needed
          if (generator != null) {
            pendingGenerations.offerLast(generator);
          }
          continue;
        }
        // try to associate the field name to a getter method
        fieldName = this.associateGetterToFieldName(method);
        boolean invokeAnnotationPresent = BytecodeUtility.isAnnotationPresent(method, INVOKE_ANNOTATION);
        if (fieldName != null && !invokeAnnotationPresent && !method.getType().getQualifiedName().equals("void")) {
          // it is a getter method
          // push the creation request to the associated queue
          Deque<Generator> pendingGenerations = this.pendingGenerations.computeIfAbsent(type,
            $ -> new ArrayDeque<>());
          // check if the method call should be wrapped
          Wrap wrap = method.getAnnotation(Wrap.class);
          if (wrap != null && !wrap.in().isEmpty() && !wrap.returnType().isEmpty()) {
            // wrap the method call into the provided string pattern for the creation
            pendingGenerations.offerLast(new MethodGenerator(method, String.format("return %s;",
              String.format(wrap.in(), fieldName))));
            // push the field after the method because the field must be there before
            // we can compile the method
            if (visitedFields.add(fieldName)) {
              pendingGenerations.push(new FieldGenerator(fieldName, wrap.returnType(), method));
            }
            // save the getter method for post processing
            processedGetterMethods.push(new MethodFieldPair(method, fieldName, wrap));
          } else {
            // plain return call
            pendingGenerations.offerLast(new MethodGenerator(method, String.format("return this.%s;", fieldName)));
            // push the field after the method because the field must be there before
            // we can compile the method
            if (visitedFields.add(fieldName)) {
              pendingGenerations.push(new FieldGenerator(fieldName, method.getType(), method));
            }
            // save the getter method for post processing
            processedGetterMethods.push(new MethodFieldPair(method, fieldName));
          }
          // add the non-null generator if needed
          if (generator != null) {
            pendingGenerations.offerLast(generator);
          }
        } else if (invokeAnnotationPresent) {
          // the method is generated just using @Invoke, push the creation of it
          this.pendingGenerations.computeIfAbsent(type, $ -> new ArrayDeque<>()).push(new MethodGenerator(method));
          // add the non-null generator if needed
          if (generator != null) {
            this.pendingGenerations.computeIfAbsent(type, $ -> new ArrayDeque<>()).offerLast(generator);
          }
        }
      }
      // push the superclasses and interfaces to the stack
      if (processingType.getSuperclass() != null) {
        processingQueue.push(processingType.getSuperclass().getDeclaration());
      }
      for (CtTypeReference<?> superInterface : processingType.getSuperInterfaces()) {
        processingQueue.push(superInterface.getDeclaration());
      }
    }
    // Generate the constructor if enabled
    Constructor constructor = type.getAnnotation(Constructor.class);
    if (constructor != null && constructor.types().length > 0) {
      Constructor.Type[] types = Arrays.stream(constructor.types()).distinct().toArray(Constructor.Type[]::new);
      Deque<Generator> generators = this.pendingGenerations.computeIfAbsent(type,
        $ -> new ArrayDeque<>());

      for (Constructor.Type constructorType : types) {
        switch (constructorType) {
          case NO_ARGS:
            this.offerAllLast(generators, ConstructorGenerator.noArgs(type, constructors));
            break;
          case REQUIRED_ARGS:
            this.offerAllLast(generators, ConstructorGenerator.requiredArgs(type, processedGetterMethods, constructors));
            break;
          case ALL_ARGS:
            this.offerAllLast(generators, ConstructorGenerator.allArgs(type, processedGetterMethods, constructors));
            break;
          default:
            break;
        }
      }
    }
    // Generate the toString method if enabled
    ToString toString = type.getAnnotation(ToString.class);
    if (toString != null) {
      this.pendingGenerations.computeIfAbsent(type, $ -> new ArrayDeque<>()).offerLast(new ToStringGenerator(
        toString.callSuper(), toString.useToStringHelper(), new ArrayDeque<>(processedGetterMethods)));
    }
    // Generate the equals method if enabled
    Equals equals = type.getAnnotation(Equals.class);
    if (equals != null) {
      this.pendingGenerations.computeIfAbsent(type, $ -> new ArrayDeque<>()).offerLast(new EqualsGenerator(
        equals.callSuper(), equals.preventNullabilityIssues(), new ArrayDeque<>(processedGetterMethods)));
    }
    // Generate the hashCode method if enabled
    HashCode hashCode = type.getAnnotation(HashCode.class);
    if (hashCode != null) {
      this.pendingGenerations.computeIfAbsent(type, $ -> new ArrayDeque<>()).offerLast(new HashCodeGenerator(
        hashCode.callSuper(), new ArrayDeque<>(processedGetterMethods)));
    }
    // Success!
    return true;
  }

  @Override
  protected @Nullable CtClass provideCtClass(@NotNull CtType<?> type) throws Exception {
    Generate settings = type.getAnnotation(Generate.class);
    if (settings == null) {
      // The type is not a generation target
      return null;
    }
    // read the settings from the annotation
    String packageName = settings.targetPackage().isEmpty()
      ? type.getPackage().getQualifiedName()
      : settings.targetPackage();
    String classSuffix = settings.classSuffix().isEmpty()
      ? "Impl"
      : settings.classSuffix();
    // the class name is provided by the package, the name of the interface and the class suffix
    String className = packageName + '.' + type.getSimpleName() + classSuffix;
    // We first try to use the existing class (if loaded) and we will fall back to creating
    // one when no class is available.
    CtClass ctClass = BytecodeUtility.provideCtClass(this.classPool, className);
    // add the element we are generating for as interface or super class
    CtClass superClass = this.getCtClass(type.getQualifiedName());
    if (type instanceof CtInterface<?>) {
      ctClass.addInterface(superClass);
    } else {
      ctClass.setSuperclass(superClass);
    }
    // finished class generation process
    return ctClass;
  }

  protected @Nullable Collection<? extends CtConstructor<?>> getSuperClassConstructors(@NotNull CtType<?> ctType) {
    // check if the class has a super class
    CtTypeReference<?> superClass = ctType.getSuperclass();
    if (superClass != null) {
      // check if the super class is a class reference (always should be)
      CtType<?> superClassType = superClass.getDeclaration();
      if (superClassType instanceof spoon.reflect.declaration.CtClass<?>) {
        // return the constructors of the super class
        return ((spoon.reflect.declaration.CtClass<?>) superClassType).getConstructors();
      }
    }
    // return null to indicate no constructors
    return null;
  }

  protected @Nullable String associateGetterToFieldName(@NotNull CtMethod<?> method) {
    // check for overridden field name using @FieldName
    FieldName fieldName = method.getAnnotation(FieldName.class);
    if (fieldName != null && !fieldName.value().isEmpty()) {
      return fieldName.value();
    } else {
      // get the field name by the name of the method
      Matcher matcher;
      for (Pattern pattern : METHOD_NAMING_PATTERN) {
        matcher = pattern.matcher(method.getSimpleName());
        if (matcher.matches()) {
          return this.extractFieldName(matcher.group(1));
        }
      }
      // none of the pattern matches, check if we allow fluent style naming.
      // If fluent style naming is enabled we assume that the name of the field
      // matches the name of the method.
      if (this.allowFluentMethods) {
        return method.getSimpleName();
      }
    }
    // If we are unable to find a field name we return null to skip the method.
    return null;
  }

  protected @Nullable String associateSetterToFieldName(@NotNull CtMethod<?> method) {
    // setter are required to only take one parameter
    if (method.getParameters().size() == 1) {
      // check for overridden field name using @FieldName
      FieldName fieldName = method.getAnnotation(FieldName.class);
      if (fieldName != null && !fieldName.value().isEmpty()) {
        return fieldName.value();
      } else {
        Matcher matcher = MUTATOR_PATTERN.matcher(method.getSimpleName());
        if (matcher.matches()) {
          return this.extractFieldName(matcher.group(1));
        } else if (this.allowFluentMethods) {
          return method.getSimpleName();
        }
      }
    }
    // If we are unable to find a field name we return null to skip the method.
    return null;
  }

  protected @NotNull String extractFieldName(@NotNull String methodName) {
    // the method name matches one of the method naming pattern
    // just make the first char of the method name lower case
    // to follow the naming conventions applied to field as the
    // return value is the name of the field prefix with for
    // example 'get'.
    return Character.toLowerCase(methodName.charAt(0)) + methodName.substring(1);
  }

  protected @NotNull CtClass getCtClass(@NotNull String name) {
    // return the ct class if loaded or throw an exception
    // if the class is not available
    try {
      return this.classPool.get(name);
    } catch (NotFoundException exception) {
      throw new RuntimeException("Missing class " + name);
    }
  }

  protected <T> void offerAllLast(@NotNull Deque<T> deque, @NotNull Collection<? extends T> elements) {
    for (T element : elements) {
      deque.offerLast(element);
    }
  }
}
