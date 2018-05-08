# Immerse - Toolbox

The Immerse toolbox module contains a 'toolbox' full of implementations of the extension interfaces defined in the
[domain module](../domain/README.md). For more information about the individual implementation classes, see their Javadoc.

## Design

Some important points about the design considerations of the toolbox module.

### Static initializers

Since the `ImmerseSettings` class in the domain expects factories of interface implementations, every implementation class comes with static initializer methods
that returns a factory of that type. This way, it's easy to create an object of an implementing class that can be used
in a settings builder for the domain, without having to deal with the `Factory` layer. Also, since these static methods can
be statically imported (so you can omit the class name), the initialization code stays very concise and readable.

## Usage

This toolbox module has little use on it's own. It is primarily meant to serve as input objects for the audio-streaming module, although you could use it as a base
for another streaming implementation.
