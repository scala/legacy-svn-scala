================================================================================
                                ANDROID EXAMPLES
                         Code examples written in Scala
                        and adapted from the Android SDK
================================================================================

This document describes code examples written in Scala and adapted from the
Java examples available together with the Android SDK from Google Inc.
(http://developer.android.com/sdk/). Both the Java source code and the Scala
source code are licensed under the Apache License, Version 2.0.

For information about Scala as a language, you can visit the web site
http://www.scala-lang.org/

In the following we describe the build and installation of our Android
applications written in Scala (and in Java) using the Apache Ant build tool.
Note that the build instructions below apply to both the Unix and Windows
environments.

All Android examples have been run successfully on the virtual Android device
"2.2_128M_HVGA" configured as follows: 2.2 target, 128M SD card and HVGA skin
(for more details see the documentation page
$ANDROID_SDK_ROOT/docs/guide/developing/tools/avd.html).

NB. The file INSTALL.txt gives VERY USEFUL informations about the small
development framework (directories "bin/" and "configs/") included in this
software distribution; in particular it permits to greatly improve the build
time of Android applications written in Scala.


Software Requirements
---------------------

In order to build/run our Android examples we need to install the following
free software distributions (tested versions and download sites are given in
parenthesis) :

1) Sun Java SDK 1.6 or newer (1.6.0_20 , www.sun.com/java/jdk/)
2) Scala SDK 2.7.5 or newer  (2.8.0_RC5, www.scala-lang.org/downloads/)
3) Android SDK 1.5 or newer  (2.2      , developer.android.com/sdk/)
4) Apache Ant 1.7.0 or newer (1.8.1    , ant.apache.org/)
5) YGuard 2.3 or newer       (2.3.0.1  , www.yworks.com/products/yguard/)

NB. In this document we rely on Ant tasks featured by the Scala SDK, the
Android SDK and the YGuard obfuscator tool (we will say more about YGuard when
we look at the modified Ant build script).


Project Structure
-----------------

The project structure of an Android application follows the directory layout
prescribed by the Android system (for more details see the documentation page
$ANDROID_SDK_ROOT/docs/guide/developing/other-ide.html#CreatingAProject).

In particular:

* The "AndroidManifest.xml" file contains essential information the Android
  system needs to run the application's code (for more details see the docu-
  mentation page $ANDROID_SDK_ROOT/docs/guide/topics/manifest/manifest-intro.html)

* The "build.properties" file defines customizable Ant properties for the
  Android build system; in our case we need to define at least the following
  properties (please adapt the respective values to your own environment):

  Unix:                         Windows:
     sdk.dir=/opt/android          sdk.dir=c:\\Progra~1\\Android
     scala.dir=/opt/scala          sdk.dir=c:\\Progra~1\\Scala
     yguard.dir=/opt/yguard        sdk.dir=c:\\Progra~1\\YGuard

* The "default.properties" file defines the default API level of an Android
  (for more details see the documentation page
  $ANDROID_SDK_ROOT/docs/guide/appendix/api-levels.html).

* The "build.xml" Ant build script defines targets such as "clean", "install"
  and "uninstall" and has been slightly modified to handle also Scala source
  files. Concretely, we override the default behavior of the "-dex" target and
  modify its dependency list by adding the imported target "scala-compile" :

    <import file="build-scala.xml"/>

    <!-- Converts this project's .class files into .dex files -->
    <target name="-dex" depends="compile, scala-compile, scala-shrink">
        <dex-helper />
    </target>

* The "build-scala.xml" Ant build script defines the targets "scala-compile"
  and "scala-shrink" where respectively the "<scalac>" Ant task generates
  Java bytecode from the Scala source files and the "<yguard>" task creates a
  shrinked version of the Scala standard library by removing the unreferenced
  code (see next section for more details). Those two tasks are featured by
  the Scala and YGuard software distributions respectively.


Project Build
-------------

We assume here the Android emulator is up and running; if not we start it
using the shell command (let us assume the existence of the "2.2_128M_HVGA"
virtual device) :

   android-sdk> emulator -no-boot-anim -no-jni -avd 2.2_128M_HVGA &

Then we move for instance to the "Snake" project directory and execute one of
the following Ant targets :

   android-sdk> cd Snake
   Snake> ant clean
   Snake> ant scala-compile
   Snake> ant debug
   Snake> ant install
   (now let us play with our application on the emulator !)
   Snake> ant uninstall


================================================================================


Note about YGuard
-----------------

The main issue when building an Android application written in Scala is related
to the code integration of the Scala standard library into the generated Android
bytecode. Concretely, we have two choices :

1) We bundle (or better to say -- see the note below --, we try to bundle)
   the full Scala library code (an external library) together with our Android
   application as prescribed by the Android system (only system libraries can
   exist outside an application package).
   In Scala 2.8 the "scala-library.jar" library file has a size of about 5659K;
   it means that even the simplest Android application written in Scala would
   have a respectable foot print of at least 6M in size !

   NB. At this date (May 2010) we could not generate Android bytecode for the
   Scala standard library using the dx tool of the Android SDK. Thus, the
   execution of the following shell command fails with the error message
   "trouble writing output: format == null" :

   /tmp> dx -JXmx1024M -JXms1024M -JXss4M --no-optimize --debug --dex
         --output=/tmp/scala-library.jar /opt/scala/lib/scala-library.jar

2) We find a (possibly efficient) way to shrink the size of the Scala standard
   library by removing the library code not referenced by our Android
   application. Our current solution relies on YGuard, a free Ant-aware
   obfuscator tool; the result is size efficient while the proccessing is quite
   time consuming (to be improved e.g. using a compiler generated dependency
   list).

   Application     <myapp>.jar  scala-library-shrinked.jar  classes.dex
   (in Scala)      (entry point)  (orig. 3900 classes)   (Android bytecode)
   -----------------------------------------------------------------------
   ApiDemos          982K          361K (482 classes)         785K
   ContactManager     32K          303K (410 classes)         246K
   CubeLiveWallpaper  31K          347K (469 classes)         279K 
   FileBrowser        22K          371K (471 classes)         292K
   GestureBuilder     44K          410K (520 classes)         341K
   HelloActivity       4K            2K (  2 classes)           3K
   Home               54K          321K (439 classes)         289K
   JetBoy             45K          320K (446 classes)         280K
   LunarLander        28K          363K (481 classes)         299K
   NotePad            36K          299K (431 classes)         250K
   PhoneDialer         6K          292K (401 classes)         215K
   Snake              41K          375K (496 classes)         305K

   NB. The above files are generated into the "bin" output directory; the
   "yshrinklog.xml" logging file provides more information about the shrinking
   task of YGuard.

   We now compare the build times and sizes of our Android applications
   (written in Scala) with the orginal examples (written in Java) from the
   Android distribution :

   Application        classes.dex   <app>-debug.apk(1)  build time(2)
                    (Scala / Java)   (Scala / Java)    (Scala / Java)
   ------------------------------------------------------------------
   ApiDemos           785K / 468K     2337K / 2174K    8m 18s / 19s
   ContactManager     246K /  17K      103K /   25K    8m 24s /  8s
   CubeLiveWallpaper  279K /  15K      109K /   19K    8m 10s /  5s
   GestureBuilder     341K /  20K      132K /   28K    8m 23s /  5s
   Home               289K /  32K      342K /  247K    9m 21s /  6s
   JetBoy             280K /  24K     1628K / 1530K    7m 44s / 13s
   LunarLander        299K /  18K      221K /  120K    7m 50s /  5s
   NotePad            250K /  21K      114K /   48K    9m 10s /  3s
   Snake              305K /  14K      118K /   18K    8m 12s /  4s

   (1) Sizes of application packages include bytecode and resources.
   (2) Elapsed times for Scala builds include YGuard processing time.

   NB. The above results were measured with YGuard 3.2.0.1 on a 2.0 GHz
   Pentium M with 2 GB of memory, using Sun JDK 1.6.0_20 and Scala 2.8.0_RC5
   on Ubuntu 8.04 Linux.


Have fun!
The Scala Team

