# Kreeda-Ankana Setup Guide

This guide covers Ubuntu setup for building and running Kreeda-Ankana from source.

## Prerequisites

- Ubuntu Linux
- `git`, `unzip`, `wget` or `curl`
- OpenJDK 17
- Android SDK installed under `$HOME/Android/Sdk`

## Install Android SDK on Ubuntu

1. Install required packages:

```bash
sudo apt update
sudo apt install openjdk-17-jdk unzip wget -y
```

2. Create the Android SDK directory:

```bash
mkdir -p "$HOME/Android/Sdk"
```

3. Download command-line tools and extract them:

```bash
cd /tmp
wget https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip -O cmdline-tools.zip
unzip cmdline-tools.zip
mkdir -p "$HOME/Android/Sdk/cmdline-tools/latest"
mv cmdline-tools/* "$HOME/Android/Sdk/cmdline-tools/latest/"
```

4. Configure environment variables in `~/.bashrc`, `~/.zshrc`, or a local `.env` file:

```bash
export JAVA_HOME="/usr/lib/jvm/java-17-openjdk-amd64"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_SDK_ROOT="$HOME/Android/Sdk"
export ANDROID_HOME="$ANDROID_SDK_ROOT"
export ANDROID_SDK_HOME="$HOME/.android"
export ANDROID_AVD_HOME="$ANDROID_SDK_HOME/avd"
export PATH="$PATH:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/platform-tools"
```

Then reload the shell:

```bash
source ~/.bashrc
# or
source ~/.zshrc
```

If you prefer, source a local env file instead:

```bash
cp .env.example .env
source .env
```

5. Install the Android SDK packages used by this project:

```bash
sdkmanager --sdk_root="$ANDROID_SDK_ROOT" \
  "platform-tools" \
  "platforms;android-35" \
  "build-tools;34.0.0" \
  "emulator"
```

If you want an emulator image too, install one:

```bash
sdkmanager --sdk_root="$ANDROID_SDK_ROOT" \
  "system-images;android-35;default;x86_64"
```

Create an AVD if you do not already have one:

```bash
avdmanager create avd -n Pixel_6_API_35 -k "system-images;android-35;default;x86_64" --device "pixel"
```

6. Accept licenses:

```bash
sdkmanager --sdk_root="$ANDROID_SDK_ROOT" --licenses
```

## Windows setup

1. Install required tools:

- OpenJDK 17
- Android SDK
- Android command-line tools

2. Set up environment variables in PowerShell or Command Prompt.

PowerShell example (session only):

```powershell
$env:JAVA_HOME = 'C:\Program Files\jdk-24.0.1'
$env:ANDROID_SDK_ROOT = "$env:USERPROFILE\Android\Sdk"
$env:ANDROID_HOME = $env:ANDROID_SDK_ROOT
$env:ANDROID_SDK_HOME = "$env:USERPROFILE\.android"
$env:ANDROID_AVD_HOME = "$env:ANDROID_SDK_HOME\avd"
$env:Path = "$env:JAVA_HOME\bin;$env:ANDROID_SDK_ROOT\cmdline-tools\latest\bin;$env:ANDROID_SDK_ROOT\platform-tools;$env:Path"
```

Command Prompt example (session only):

```cmd
set JAVA_HOME=C:\Program Files\jdk-24.0.1
set ANDROID_SDK_ROOT=%USERPROFILE%\Android\Sdk
set ANDROID_HOME=%ANDROID_SDK_ROOT%
set ANDROID_SDK_HOME=%USERPROFILE%\.android
set ANDROID_AVD_HOME=%ANDROID_SDK_HOME%\avd
set PATH=%JAVA_HOME%\bin;%ANDROID_SDK_ROOT%\cmdline-tools\latest\bin;%ANDROID_SDK_ROOT%\platform-tools;%PATH%
```

For persistent Windows settings, use `setx` instead of `set`.

3. Install the required Android SDK packages:

```cmd
sdkmanager.bat "platform-tools" "platforms;android-35" "build-tools;34.0.0" "emulator"
```

4. Optionally install a system image for emulator use:

```cmd
sdkmanager.bat "system-images;android-35;default;x86_64"
```

5. Accept licenses:

```cmd
sdkmanager.bat --licenses
```

6. Create an AVD (if none exists):

```cmd
avdmanager.bat create avd -n Pixel_6_API_35 -k "system-images;android-35;default;x86_64" --device "pixel"
```

7. Build, start emulator, and install the app using `setup-windows.bat`:

```cmd
setup-windows.bat
```

If you only need to start the emulator and wait for a device before running the app, use:

```cmd
start-emulator-windows.bat
```

This script checks Java, SDK paths, required tools, builds `assembleDebug`, starts the emulator if needed, waits for the device, and installs the debug APK.

Then reload the shell:

```bash
source ~/.bashrc
# or
source ~/.zshrc
```

If you prefer, source a local env file instead:

```bash
cp .env.example .env
source .env
```

5. Install the Android SDK packages used by this project:

```bash
sdkmanager --sdk_root="$ANDROID_SDK_ROOT" \
  "platform-tools" \
  "platforms;android-35" \
  "build-tools;34.0.0" \
  "emulator"
```

If you want an emulator image too, install one:

```bash
sdkmanager --sdk_root="$ANDROID_SDK_ROOT" \
  "system-images;android-35;default;x86_64"
```

Create an AVD if you do not already have one:

```bash
avdmanager create avd -n Pixel_6_API_35 -k "system-images;android-35;default;x86_64" --device "pixel"
```

6. Accept licenses:

```bash
sdkmanager --sdk_root="$ANDROID_SDK_ROOT" --licenses
```

## Project setup

1. Clone or open the repository.
2. From the project root, create `local.properties` with the SDK path:

```bash
printf 'sdk.dir=%s/Android/Sdk\n' "$HOME" > local.properties
```

3. (Optional) Copy the example environment file and source it:

```bash
cp .env.example .env
source .env
```

4. Make sure the Gradle wrapper is used from the repo root:

```bash
chmod +x ./gradlew
```

## Build and run

1. Build the debug APK:

```bash
./gradlew assembleDebug
```

2. Verify the APK output:

```bash
ls -l app/build/outputs/apk/debug/app-debug.apk
```

3. Check for connected devices/emulators:

```bash
adb devices
```

If no device is connected, start an emulator manually:

```bash
emulator -list-avds
emulator -avd <avd-name> &
adb wait-for-device

# or
emulator -list-avds
source .env ~/Android/Sdk/cmdline-tools/latest/bin/avdmanager create avd -n Pixel_6_API_33 -k "system-images;android-33;default;x86_64" --device "pixel" emulator -avd Pixel_6_API_33 & adb wait-for-device
```

4. Install the APK to a connected device or emulator:

```bash
./gradlew installDebug
```

## Notes

- `adb` will start its daemon automatically, but it will not launch an emulator on its own.
- `./gradlew installDebug` requires a connected device/emulator; if none are available, it fails with `No connected devices!`.
- Do not use `sudo` with Gradle commands.
- `./gradle` is not the correct wrapper command in this repo; use `./gradlew`.
- `./gradlew lint` may fail if the current codebase has lint issues. Use the lint reports or a baseline to resolve them.

## Troubleshooting

- If `sdkmanager` fails, ensure `JAVA_HOME` is set to JDK 17 and `PATH` includes `cmdline-tools/latest/bin`.
- If the emulator fails to start, install KVM support and add your user to the `kvm` group:

```bash
sudo apt install qemu-kvm libvirt-daemon-system libvirt-clients -y
sudo usermod -aG kvm "$USER"
```
