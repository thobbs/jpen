# JPen
**JPen** is a Java library for accessing pen/digitizer tablets and pointing devices, such as Wacom tablets.

The [original SourceForge documentation page](http://jpen.sf.net) is a decent starting point to learn about JPen.

This fork is attempting to modernize the build system and clean up the code a bit to make it easier to build and use.

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

I was able to get this to run on Windows 11 as of April, 2026. I'm using a Wacom Cintiq Pro 24, and the Wacom drivers are installed. Pressure sensitivity and tilt detection work with the demo.

### OSX

This is supposedly supported, but not yet tested.
