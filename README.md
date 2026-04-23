# JPen
**JPen** is a Java library for accessing pen/digitizer tablets and pointing devices, such as Wacom tablets.

The [original SourceForge documentation page](http://jpen.sf.net) is a decent starting point to learn about JPen.

This fork was created in 2026, as the original project did not appear to be actively maintained. This fork introduced several changes:
 - Modernize the build system, making it less brittle and more portable
 - Switch to a modern logging setup (SLF4J + Logback)
 - Code style cleanup and enforcement, via Spotless
 - Avoid using deprecated JVM features, like the Security Manager
 - Improve logging to increase visibility into what's happening (or not happening)

This fork has primarily been tested on Windows 11 with a Wacom Cintiq Pro 24. It has been confirmed to build and run in WSL as well as Arch Linux. Testing for OSX is currently planned, but has not been completed.

## Building

To build a jar, run: `mvn clean package`

To install locally, run: `mvn install`

To execute the demo, run: `mvn exec:exec`

### Linux

Dependencies:
 - git
 - gcc
 - g++
 - maven
 - Java SDK (last development was against Java 25)

### Windows

Dependencies:
 - git (git-bash is a good option for this)
 - gcc/g++ ("scoop install gcc" works for both)
 - maven
 - Java SDK (last development was against Java 25)

### OSX

This is supposedly supported, but not yet tested.
