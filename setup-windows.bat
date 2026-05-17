@echo off
setlocal enabledelayedexpansion

echo =============================================
echo Kreeda-Ankana Windows setup and build helper
echo =============================================

rem Ensure script is running from the repo root.
pushd "%~dp0" || (
  echo ERROR: Failed to change directory to script location.
  exit /b 1
)

rem Load repository-local .env variables if present.
call :loadDotEnv "%~dp0.env"
echo After load: JAVA_HOME=!JAVA_HOME!
echo After load: ANDROID_SDK_ROOT=!ANDROID_SDK_ROOT!
rem (Skipping direct PATH echo to avoid parser issues with special characters)

rem Prerequisite checks/installs happen inline below when necessary.

set "GRADLEW=%~dp0gradlew.bat"
if not exist "%GRADLEW%" (
  echo ERROR: gradlew.bat not found in repository root.
  echo Make sure you run this script from the Kreeda-Ankana project root.
  exit /b 1
)

if not defined JAVA_HOME (
  echo WARNING: JAVA_HOME is not set.
  echo Recommended: set JAVA_HOME to your JDK 17 install directory.
  set "JAVA_HOME=C:\Program Files\jdk-24.0.1"
)
if not exist "%JAVA_HOME%\bin\java.exe" (
  echo ERROR: Java not found at %JAVA_HOME%\bin\java.exe
  echo Please install OpenJDK 17 and set JAVA_HOME correctly.
  exit /b 1
)

set "ANDROID_SDK_ROOT=%ANDROID_SDK_ROOT%"
if not defined ANDROID_SDK_ROOT (
  echo WARNING: ANDROID_SDK_ROOT is not set.
  set "ANDROID_SDK_ROOT=%USERPROFILE%\Android\Sdk"
)
if not exist "%ANDROID_SDK_ROOT%" (
  echo ERROR: Android SDK root not found at %ANDROID_SDK_ROOT%
  echo Please install the Android SDK and set ANDROID_SDK_ROOT appropriately.
  exit /b 1
)

set "SDK_TOOLS=%ANDROID_SDK_ROOT%\cmdline-tools\latest\bin"
set "PLATFORM_TOOLS=%ANDROID_SDK_ROOT%\platform-tools"
set "EMULATOR=%ANDROID_SDK_ROOT%\emulator"

if not exist "%SDK_TOOLS%\sdkmanager.bat" (
  echo ERROR: sdkmanager.bat not found in %SDK_TOOLS%
  echo Install the Android SDK command-line tools in %ANDROID_SDK_ROOT%\cmdline-tools\latest.
  exit /b 1
)
if not exist "%SDK_TOOLS%\avdmanager.bat" (
  echo ERROR: avdmanager.bat not found in %SDK_TOOLS%
  echo Install the Android SDK command-line tools package.
  exit /b 1
)
if not exist "%PLATFORM_TOOLS%\adb.exe" (
  echo ERROR: adb.exe not found in %PLATFORM_TOOLS%
  echo Install platform-tools using sdkmanager.
  exit /b 1
)
if not exist "%EMULATOR%\emulator.exe" (
  echo WARNING: emulator.exe not found in %EMULATOR%
  echo Install the Android emulator package with sdkmanager.
)

echo.
echo Java:
"%JAVA_HOME%\bin\java.exe" -version
echo.
echo Android SDK root: %ANDROID_SDK_ROOT%

echo.
echo Checking required Android tools...
if exist "%SDK_TOOLS%\sdkmanager.bat" (echo sdkmanager OK) else echo sdkmanager MISSING
if exist "%SDK_TOOLS%\avdmanager.bat" (echo avdmanager OK) else echo avdmanager MISSING
if exist "%PLATFORM_TOOLS%\adb.exe" (echo adb OK) else echo adb MISSING
if exist "%EMULATOR%\emulator.exe" (echo emulator OK) else echo emulator MISSING

set "SDK_OK=1"
if not exist "%SDK_TOOLS%\sdkmanager.bat" set "SDK_OK=0"
if not exist "%SDK_TOOLS%\avdmanager.bat" set "SDK_OK=0"
if not exist "%PLATFORM_TOOLS%\adb.exe" set "SDK_OK=0"

if "%SDK_OK%"=="0" (
  echo.
  echo ERROR: Required Android SDK tools are missing.
  echo Use sdkmanager to install platform-tools, emulator, and command-line tools.
  exit /b 1
)

echo.
echo Checking installed system image for Android 35...
if exist "%ANDROID_SDK_ROOT%\system-images\android-35\default\x86_64" (
  echo system image installed.
) else (
  echo WARNING: Android 35 x86_64 system image is not installed.
  echo Install it with:
  echo   "%SDK_TOOLS%\sdkmanager.bat" "system-images;android-35;default;x86_64"
)

set "AVD_NAME=Pixel_6_API_35"
set "AVD_LIST=%TEMP%\avd-list.txt"
"%SDK_TOOLS%\avdmanager.bat" list avd > "%AVD_LIST%" 2>&1
findstr /R /C:"^Name:" "%AVD_LIST%" >nul
if errorlevel 1 (
  echo.
  echo No AVD definitions found.
  if not exist "%ANDROID_SDK_ROOT%\system-images\android-35\default\x86_64" (
    echo Cannot create AVD because the system image is missing.
    goto skip_avd_creation
  )
  echo Creating AVD %AVD_NAME%...
  "%SDK_TOOLS%\avdmanager.bat" create avd -n "%AVD_NAME%" -k "system-images;android-35;default;x86_64" --device "pixel" --force
  if errorlevel 1 (
    echo ERROR: Failed to create AVD.
    goto skip_avd_creation
  )
  echo AVD %AVD_NAME% created.
) else (
  echo AVDs already exist.
)

:skip_avd_creation

echo.
echo Building the debug APK...
call "%GRADLEW%" assembleDebug
if errorlevel 1 (
  echo ERROR: Gradle build failed.
  exit /b 1
)

echo.
echo Checking for connected device...
"%PLATFORM_TOOLS%\adb.exe" devices | findstr /R /C:"device$" >nul
if errorlevel 1 (
  echo No connected device found.
  if not exist "%EMULATOR%\emulator.exe" (
    echo ERROR: emulator.exe is missing, cannot launch emulator.
    exit /b 1
  )
  echo Starting emulator %AVD_NAME%...
  start "Kreeda-Ankana Emulator" "%EMULATOR%\emulator.exe" -avd "%AVD_NAME%" -netspeed full -netdelay none
  echo Waiting for emulator to appear...
  set /a count=0
  :wait_loop
  timeout /t 5 /nobreak >nul
  "%PLATFORM_TOOLS%\adb.exe" devices | findstr /R /C:"device$" >nul
  if errorlevel 0 goto device_ready
  set /a count+=1
  if %count% geq 30 (
    echo ERROR: Emulator did not connect after 150 seconds.
    exit /b 1
  )
  goto wait_loop
)

:device_ready
echo Device/emulator connected.
echo Installing APK on connected device...
call "%GRADLEW%" installDebug
if errorlevel 1 (
  echo ERROR: installDebug failed.
  exit /b 1
)

echo.
echo SUCCESS: APK built and deployed.
exit /b 0

:loadDotEnv
if not exist "%~1" exit /b 0

echo Loading environment variables from %~1
set "ENV_DOTENV=%TEMP%\kreeda_env_load.cmd"
powershell -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%~dp0load-env.ps1" "%~1" "%ENV_DOTENV%"
if errorlevel 1 (
  echo ERROR: Failed to parse %~1
  del "%ENV_DOTENV%" 2>nul
  exit /b 1
)
if exist "%ENV_DOTENV%" (
  call "%ENV_DOTENV%"
  del "%ENV_DOTENV%" 2>nul
)
exit /b 0
