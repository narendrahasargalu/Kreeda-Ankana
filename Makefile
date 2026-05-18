SHELL := powershell.exe
.ONESHELL:
.DEFAULT_GOAL := help

ANDROID_SDK_ROOT := C:\Users\naren\AppData\Local\Android\Sdk
JAVA_HOME      := C:\Users\naren\.jdk\jdk-17.0.16
ifndef ANDROID_SDK_ROOT
$(error ANDROID_SDK_ROOT is not set. Export it before running make.)
endif
ifndef JAVA_HOME
$(error JAVA_HOME is not set. Export it before running make.)
endif
EMULATOR := $(ANDROID_SDK_ROOT)\emulator\emulator.exe
ADB := $(ANDROID_SDK_ROOT)\platform-tools\adb.exe
GRADLEW := $(CURDIR)\gradlew.bat
APK := $(CURDIR)\app\build\outputs\apk\debug\app-debug.apk
AVD_NAME ?= Pixel_6
PACKAGE_NAME ?= com.kreeda.ankana.debug

help:
	@Write-Host 'Available targets:'
	@Write-Host '  make build         Build the debug APK'
	@Write-Host '  make clean         Clean the project'
	@Write-Host '  make emulator      Launch the Android emulator'
	@Write-Host '  make adb-devices   List connected devices/emulators'
	@Write-Host '  make install       Install the debug APK on a connected device/emulator'
	@Write-Host '  make launch        Launch the app on a connected device/emulator'
	@Write-Host '  make script        Run build-run-emulator.ps1 (build, emulator, install, launch)'
	@Write-Host '  make run           Alias for make script'

# working
build:
	@powershell.exe -NoProfile -Command "if (-not (Test-Path '$(GRADLEW)')) { throw 'gradlew.bat not found in repository root.' }; Write-Host 'Stopping Gradle daemons...'; & '$(GRADLEW)' --stop; Write-Host 'Cleaning and building (refreshing dependencies)...'; & '$(GRADLEW)' clean assembleDebug --refresh-dependencies"

# working
build-no-refresh:
	@powershell.exe -NoProfile -Command "if (-not (Test-Path '$(GRADLEW)')) { throw 'gradlew.bat not found in repository root.' }; Write-Host 'Stopping Gradle daemons...'; & '$(GRADLEW)' --stop; Write-Host 'Cleaning and building...'; & '$(GRADLEW)' clean assembleDebug"

# working
build-without-clean:
	@powershell.exe -NoProfile -Command "if (-not (Test-Path '$(GRADLEW)')) { throw 'gradlew.bat not found in repository root.' }; Write-Host 'Stopping Gradle daemons...'; & '$(GRADLEW)' --stop; Write-Host 'Building without cleaning...'; & '$(GRADLEW)' assembleDebug"

# working
adb-devices:
	@powershell.exe -NoProfile -Command "if (-not (Test-Path '$(ADB)')) { throw 'adb.exe not found. Ensure ANDROID_SDK_ROOT is set and platform-tools are installed.' }; Write-Host 'Connected devices:'; & '$(ADB)' devices"

# not tested
clean:
	@powershell.exe -NoProfile -Command "if (-not (Test-Path '$(GRADLEW)')) { throw 'gradlew.bat not found in repository root.' }; Write-Host 'Cleaning project...'; & '$(GRADLEW)' clean"

# working
emulator:
	@powershell.exe -NoProfile -Command "if (-not (Test-Path '$(EMULATOR)')) { throw 'emulator.exe not found. Ensure ANDROID_SDK_ROOT is set and emulator is installed.' }; Write-Host "Starting emulator $(AVD_NAME)..."; & '$(EMULATOR)' -avd '$(AVD_NAME)' -netspeed full -netdelay none -verbose"

# working
install: build-without-clean
	@powershell.exe -NoProfile -Command "if (-not (Test-Path '$(APK)')) { throw 'APK not found. Run make build first.' }; if (-not (Test-Path '$(ADB)')) { throw 'adb.exe not found. Ensure ANDROID_SDK_ROOT is set and platform-tools are installed.' }; Write-Host 'Installing APK...'; & '$(ADB)' install -r '$(APK)'"

# working
launch:
	@powershell.exe -NoProfile -Command "if (-not (Test-Path '$(ADB)')) { throw 'adb.exe not found. Ensure ANDROID_SDK_ROOT is set and platform-tools are installed.' }; Write-Host "Launching $(PACKAGE_NAME) on connected device..."; & '$(ADB)' shell monkey -p '$(PACKAGE_NAME)' -c android.intent.category.LAUNCHER 1"

script:
	@powershell.exe -NoProfile -Command "if (-not (Test-Path './build-run-emulator.ps1')) { throw 'build-run-emulator.ps1 not found in repository root.' }; Write-Host 'Running build-run-emulator.ps1...'; powershell.exe -NoProfile -ExecutionPolicy Bypass -File './build-run-emulator.ps1'"

run: script

.PHONY: help build clean adb-devices emulator install launch script run
