INTRODUCTION

Triangle is a sample application designed to be as simple
as possible while still demonstrating how to create an
Android OpenGLES application. Feature include:

- Initializing EGL and OpenGL ES.
- Using a separate thread to render.
- Reinitializing EGL and OpenGL ES after resuming from a pause and/or a lost surface.
- Cleanly exiting when the view is removed from the window.

BUILDING

The project directory is ready for use from either Eclipse or with the
activityCreator.py script (if you want to use something other than
eclipse).

To use Eclipse, install Eclipse, the Android SDK and the Eclipse ADT
plugin (if you haven't already, see
http://code.google.com/android/intro/installing.html for more details)
then start Eclipse, and create a new Android project. Use the
"from existing source" option, and browse to this directory. The
Android project should then be created, and you can browse the source,
build the application and run it in the emulator.

To build the application with activityCreator, start a command line
and change to the Triangle directory, then type
activityCreator.py com.google.android.opengles.triangle.Triangle

(It is assumed you have set up activityCreator.py, ant and emulator
to run from the command line. Please see
http://code.google.com/android/intro/installing.html#otherides
for more details)

It should give some warnings about files already existing, this is
expected. Then type:

ant

and the project should build.

Start the emulator and then install the Triangle.apk file from the
bin directory:

adb install Triangle.apk

You should then be able to run the app.

