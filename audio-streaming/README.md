# Immerse - Audio Streaming

The Immerse audio streaming module is 'the beating heart' of the Immerse system. After you started a mixer, you can play one or more scenarios
that will be dynamically streamed to the speakers according to the settings. There is a lot to tell about how this audio streaming works.
The sections below will highlight a few important topics. More information can also be found in the Javadoc of the various classes.

## Scenarios

A scenario consists of:
* An audio resource (file, URL, generated / custom audio)
* A dynamic location of the sound source (follow a path, circle, static)
* A dynamic location of the listener (static, dynamic based on sensors)
* An algorithm that can calculate the distribution of sound over the speakers (field of hearing, only closest, fixed)
* An algorithm that can normalize the volumes (max sum, fractional)
* A playback defition (loop, time based, forever)
* A room with speakers and sound cards that should play the scenario

This allows you to imagine a real world scenario and configure it accordingly.

## Mixer

The mixer (ImmerseMixer) is able to play scenarios. It should be constructed, initialized and started. After that is done, the main audio loop is running
and audio (initially just silence) is actually streaming to the speakers. Now the mixer is ready to play scenarios. Any scenario that is
played will be included in the mixer calculations and audio streaming to the speakers. The mixer is set up to be the API for this module to the outside world.

## Smooth playback

One of the most challenging parts of Immerse is to keep audio playback as smooth as possible. Because Immerse is designed to be dynamic,
it must keep the buffer size very small to allow for (near) real time interactions. But a small buffer means little protection against
system hickups. A few mechanisms have been put in place to optimize for smooth playback.

### Single JVM & computer purpose

It's recommended to only have Immerse running in the JVM. Furthermore the computer should only run Immerse and no other (heavy) processes.
This protects against CPU spikes and heavy memory usage and makes playback very stable.

### JVM Warmup

A Java Virtual Machine (JVM) is known to need some warmup before it will achieve optimal performance. That is why a mixer will explicitly perform
some warmup before it accepts scenarios for playback.

### Control over Garbage Collection

One other infamous part of the JVM is garbage collection. When the memory is almost full, it will need some 'stop-the-world' time to mark all live objects
and collect the rest as garbage. Immerse will try to plan garbage collection at the best possible time to prevent hickups.
