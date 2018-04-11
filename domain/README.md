# Immerse - Domain

## Introduction

The Immerse domain module contains all classes that make up the world of dynamic sound. It has classes for real world items such as a room,
speaker and sound card. It uses concepts like scenario, snapshot and playback to define the desired playback scenarios. And for the dynamic part
we have locations and volume algorithms that calculate the volume distribution among the speakers. For more detailed information about these
classes, see their javadoc.

## Design

Some important points about the design considerations of the domain module.

### Extensible

The case classes like `Scenario`, `Room` and `Speaker` are fixed, but the domain module defines several extension points for dynamic logic.
At those points an interface is defined that defines what kind of behavior is expected. Several possible implementation are
provided in the [toolbox module](../toolbox/README.md), but of course it's possible to create your own implementations as well.

The following interfaces exist:
* `AudioResource`, that provides an AudioInputStream
* `Playback`, that defines when to stop the playback of a scenario
* `DynamicLocation`, that represents a location that changes over time
* `VolumeRatiosAlgorithm`, that calculated the relative volumes for all speakers
* `NormalizeAlgorithm`, that normalizes the relative volumes for the speakers

### Immutable

All classes in the domain module are designed to be immutable. This means, any fields they have must be set at construction time and
cannot change afterwards. A lot of classes can be seen as a sort of case class (Scala) or struct (C/C++) that is just meant to hold
a few fields and give them a nice descriptive name. So the domain does not follow the principles of DDD with logic inside domain
classes and mutable state. Immutability is generally considered good practice in terms of testability and program complexity.
A further reason within Immerse is that a `Scenario` object (or any domain object for that matter) can be re-used / re-played multiple
times without the risk of state pollution by previous actions or tricky reset methods.

There are a few points where this concept is hard to uphold. Implementations of the extension point interfaces mentioned above might contain state.
For instance an implementation of `Playback` may need to keep state about the amount of loops done or of elapsed time.
And an `AudioResource` should not return the same `AudioInputStream` each time, cause
that has internal state about the playback position. In those cases the solution is to let the domain object return a `Supplier` that
can construct a new instance (that has state) every time a certain producer method is called. The domain object itself has no state,
but can produce an object that has. The code that uses this domain should then keep track of those stateful objects and their lifecycle.

### Validation

The choice is to have very minimal validation on the domain classes, in line with the case classes / structs concept. The domain does not
know how to validate itself. You can freely choose the field values via the builders. When these constructed objects are used in another module,
that module should perform the validation that is applicable in the specific context. The downside is that no validation is provided by this module,
not even very basic checks. But the upside is that the usage of the domain is very flexible and no build-in checks can get in the way of creative usage.
Furthermore, this approach fits well with the fact that the [audio-streaming module](../audio-streaming/README.md) 
has one 'point of entry' for a new `Scenario` that can then be validated easily before being accepted into the streaming system.

### Builders

Since the domain classes are created to be immutable (see above) all field values must be supplied at construction time. One option is to create
a constructor with a lot of parameters, but this is messy and error-prone. A popular alternative are builders, which make it very flexible and readable
to create objects with lots of fields, while maintaining the immutability. Most classes in the domain module have builders and often that is the only way
you can create an instance of them, cause they will have a private constructor. There are several possible implementations of a builder with different
upsides and downsides. This module takes the pragmatic approach where the fields are not duplicated by the builder, but set directly on the instance while building.
This makes the builders small and readable. The only downside is that the fields in the domain class cannot be declared `final`, but since there are private and
there are no methods defined that change them, they still are effectively final.  

## Usage

This domain module has little use on it's own. It is primarily meant to serve as a model for the [audio-streaming module](../audio-streaming/README.md),
although you could use it as a base for another streaming implementation.
