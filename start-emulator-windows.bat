@echo off
setlocal enabledelayedexpansion

echo =============================================
echo Kreeda-Ankana Windows emulator starter
echo =============================================

rem Ensure script is running from the repo root.
pushd "%~dp0" || (
  echo ERROR: Failed to change directory to script location.
  exit /b 1
)

rem Load repository-local .env variables if present.
call :loadDotEnv "%~dp0.env"


set "ANDROID_SDK_ROOT=C:\Users\naren\AppData\Local\Android\Sdk"
set "JAVA_HOME=C:\Program Files\jdk-24.0.1"

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

if not defined ANDROID_SDK_ROOT (
  echo WARNING: ANDROID_SDK_ROOT is not set.
  set "ANDROID_SDK_ROOT=%USERPROFILE%\Android\Sdk"
)
if not exist "%ANDROID_SDK_ROOT%" (
  echo ERROR: Android SDK root not found at %ANDROID_SDK_ROOT%
  exit /b 1
)

set "ANDROID_SDK_ROOT=C:\Users\naren\AppData\Local\Android\Sdk"
set "JAVA_HOME=C:\Program Files\jdk-24.0.1"

set "SDK_TOOLS=%ANDROID_SDK_ROOT%\cmdline-tools\latest\bin"
set "PLATFORM_TOOLS=%ANDROID_SDK_ROOT%\platform-tools"
set "EMULATOR=%ANDROID_SDK_ROOT%\emulator"

if not exist "%SDK_TOOLS%\avdmanager.bat" (
  echo ERROR: avdmanager.bat not found in %SDK_TOOLS%
  echo Install the Android SDK command-line tools package.
  exit /b 1
)
if not exist "%PLATFORM_TOOLS%\adb.exe" (
  echo ERROR: adb.exe not found in %PLATFORM_TOOLS%
  exit /b 1
)
if not exist "%EMULATOR%\emulator.exe" (
  echo ERROR: emulator.exe not found in %EMULATOR%
  exit /b 1
)

echo.
echo Starting Windows emulator flow...
echo Java: "%JAVA_HOME%\bin\java.exe"
echo Android SDK root: %ANDROID_SDK_ROOT%

set "AVD_NAME=Pixel_6_API_35"
set "AVD_LIST=%TEMP%\avd-list.txt"
"%SDK_TOOLS%\avdmanager.bat" list avd > "%AVD_LIST%" 2>&1
findstr /R /C:"^Name:" "%AVD_LIST%" >nul
if errorlevel 1 (
  echo.
  echo No AVD definitions found.
  if not exist "%ANDROID_SDK_ROOT%\system-images\android-35\default\x86_64" (
    echo ERROR: Android 35 x86_64 system image is missing.
    echo Install it with:
    echo   "%SDK_TOOLS%\sdkmanager.bat" "system-images;android-35;default;x86_64"
    exit /b 1
  )
  echo Creating AVD %AVD_NAME%...
  "%SDK_TOOLS%\avdmanager.bat" create avd -n "%AVD_NAME%" -k "system-images;android-35;default;x86_64" --device "pixel" --force
  if errorlevel 1 (
    echo ERROR: Failed to create AVD.
    exit /b 1
  )
  echo AVD %AVD_NAME% created.
) else (
  echo AVDs already exist.
)

echo.
echo Checking for connected device...
"%PLATFORM_TOOLS%\adb.exe" devices | findstr /R /C:"device$" >nul
if errorlevel 0 (
  echo Device/emulator already connected.
  exit /b 0
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

:device_ready
echo Emulator connected.
exit /b 0

:loadDotEnv
if not exist "%~1" exit /b 0

echo Loading environment variables from %~1
set "ENV_DOTENV=%TEMP%\kreeda_env_load.txt"
powershell -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%~dp0load-env.ps1" "%~1" "%ENV_DOTENV%"
if errorlevel 1 (
  echo ERROR: Failed to parse %~1
  exit /b 1
)
for /f "usebackq tokens=1* delims==" %%A in ("%ENV_DOTENV%") do (
  set "%%A=%%B"
)
del "%ENV_DOTENV%" 2>nul
exit /b 0
