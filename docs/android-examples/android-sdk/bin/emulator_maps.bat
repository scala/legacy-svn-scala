@echo off

rem ##########################################################################
rem # Copyright 2009-2010, LAMP/EPFL
rem #
rem # This is free software; see the distribution for copying conditions.
rem # There is NO warranty; not even for MERCHANTABILITY or FITNESS FOR A
rem # PARTICULAR PURPOSE.
rem ##########################################################################

if "%OS%"=="Windows_NT" @setlocal

set _ANDROID_SDK_HOME=%USERPROFILE%/.android

if "%ANDROID_SDK_ROOT%"=="" goto error1
set _ANDROID_SDK_ROOT=%ANDROID_SDK_ROOT%

if "%ANDROID_API_LEVEL%"=="" (
  set _API_LEVEL=8
) else (
  set _API_LEVEL=%ANDROID_API_LEVEL%
)

set _IMAGES_DIR=%_ANDROID_SDK_ROOT%\add-ons\addon_google_apis_google_inc_%_API_LEVEL%\images
set _SYSTEM=%_IMAGES_DIR%\system.img
set _RAMDISK=%_IMAGES_DIR\ramdisk.img
set _USERDATA=%_IMAGES_DIR%\userdata.img

set _ANDROID_EMULATOR=%ANDROID_SDK_ROOT%/tools/emulator

if "%ANDROID_EMULATOR_OPTS%"=="" (
  set _EMULATOR_OPTS=-no-boot-anim -no-skin -no-jni -memory 1024
) else (
  set _EMULATOR_OPTS=%ANDROID_EMULATOR_OPTS%
)

if "%ANDROID_AVD%"=="" (
  set _AVD=2.2_128M_HVGA
) else (
  set _AVD=%ANDROID_AVD%
)

"%_EMULATOR%" -system %_SYSTEM% -ramdisk %_RAMDISK% -init-data %_USERDATA% %_EMULATOR_OPTS% -avd %_AVD%
goto end

rem ##########################################################################
rem # errors

:error1
echo ERROR: environment variable ANDROID_SDK_ROOT is undefined. It should point to your installation directory.
goto end

:end
if "%OS%"=="Windows_NT" @endlocal
