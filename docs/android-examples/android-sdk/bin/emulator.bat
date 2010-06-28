@echo off

rem ##########################################################################
rem # Copyright 2009-2010, LAMP/EPFL
rem #
rem # This is free software; see the distribution for copying conditions.
rem # There is NO warranty; not even for MERCHANTABILITY or FITNESS FOR A
rem # PARTICULAR PURPOSE.
rem ##########################################################################

if "%OS%"=="Windows_NT" @setlocal

if "%ANDROID_SDK_HOME%"=="" (
  ANDROID_SDK_HOME=%USERPROFILE%
)

if not "%ANDROID_SDK_ROOT%"=="" goto emulator

rem guess1
if not exist "%SystemDrive%\android-sdk-win32\tools\emulator.exe" goto guess2
set ANDROID_SDK_ROOT=%SystemDrive%\android-sdk-win32
goto emulator

:guess2
if not exist "%ProgramFiles%\android-sdk-win32\tools\emulator.exe" goto error1
set ANDROID_SDK_ROOT=%ProgramFiles%\android-sdk-win32

:emulator
set _EMULATOR=%ANDROID_SDK_ROOT%\tools\emulator.exe
if not exist "%_EMULATOR%" goto error2

if "%ANDROID_AVD%"=="" (
  set _AVD=2.2_128M_HVGA
) else (
  set _AVD=%ANDROID_AVD%
)

if not exist "%ANDROID_SDK_HOME%\androi~1\avd\%_AVD%.ini" goto error3
set _AVD_HOME=%ANDROID_SDK_HOME%\androi~1\avd

if "%ANDROID_EMULATOR_OPTS%"=="" (
  set _EMULATOR_OPTS=-no-boot-anim -no-skin
  if exist "%_AVD_HOME%\%_AVD%.avd-custom\ramdisk.img%" (
    set _RAMDISK=%_AVD_HOME%\%_AVD%.avd-custom\ramdisk.img
  )
)
if not "%_RAMDISK"=="" (
  set _EMULATOR_OPTS=%_EMULATOR_OPTS% -ramdisk "%_RAMDISK%"
)

rem echo "%_EMULATOR%" %_EMULATOR_OPTS% -avd %_AVD%
"%_EMULATOR%" %_EMULATOR_OPTS% -avd %_AVD%
goto end

rem ##########################################################################
rem # errors

:error1
echo Error: environment variable ANDROID_SDK_ROOT is undefined. It should point to your installation directory.
goto end

:error2
echo Error: Emulator '%_EMULATOR%' is unknown.
goto end

:error3
echo Error: Device '%_AVD%' is unknown.
echo   We cannot execute %_EMULATOR%
goto end

:end
if "%OS%"=="Windows_NT" @endlocal
