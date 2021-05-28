# Automated CodeGen [![Build Status](https://travis-ci.com/derklaro/codegen.svg?branch=master)](https://travis-ci.com/derklaro/codegen)

This project is designed for projects which have lots of data classes, which are just implementing a single interface or
class and having an enormous amount of fields in these classes. This project automatically generates these classes which
means you can leave your data interfaces nice and clean and there is no need to mess around with them.

## Compile time processing

Compile time code processing is currently only available to gradle projects using the codegen gradle plugin.

## Usage

The generator will generate implementation classes for all interfaces and abstract classes annotated with `@Generate`.
Further customization is done using the arguments of the annotation. The methods `toString`,
`equals` and `hashCode` have their own annotation to be generated. Constructors can be enabled and customized
using `@Constructor`. Methods which fields should be non-final in a class can be marked with `@OptionalField`, the name
of a field associated to a method can be customized using `@FieldName`. Wrapping the return value of a method can be
done using `@Wrap`. Factory methods for every constructor in the class can be generated using
`@Factory`. The factory method can be created before compile with a special body, the compiler will replace the last
return statement with a statement to the actual class constructor. `@Invoke` can specify the body of a specific method
which cannot be generated in a convenient way. `@NonNull` will ensure that the parameter supplied to the method will be
non-null. Please read the documentation of the annotations for more information about the way they are working.

## Support our work

If you like the project and want to support our work you can **star** :star2: the repository on github.

## Developers

##### Clone & build the project

```
git clone https://github.com/derklaro/codegen.git
cd codegen/
./gradlew OR gradlew.bat
```

##### Enable the processing & start

You need the central repository when you want to use the plugin as well as jitpack:

```groovy
maven {
  name 'jitpack.io'
  url 'https://jitpack.io'
}
```

Gradle's dependency to get access to the custom annotations for generation:

```groovy
compile group: 'me.derklaro.codegen', name: 'annotations', version: '1.0.0-SNAPSHOT'
```

The plugin can be used with:

```groovy
plugins {
  id 'java'
  id 'me.derklaro.codegen' version '1.0.0-SNAPSHOT'
}
```

## Licence and copyright notice

The project is licenced under the [MIT Licence](https://github.com/derklaro/codegen/license.txt). All files are
Copyright (c) 2020 Pasqual K. and all contributors.
