param(
    [string]$AvdName = 'Pixel_6',
    [string]$JavaHome = 'C:\Users\naren\.jdk\jdk-17.0.16',
    [string]$AndroidSdkRoot = 'C:\Users\naren\AppData\Local\Android\Sdk',
    [string]$PackageName = 'com.kreeda.ankana.debug'
)

$scriptRoot = $PSScriptRoot
if (-not $scriptRoot) {
    $scriptRoot = Split-Path -Path $MyInvocation.MyCommand.Definition -Parent
}
if (-not $scriptRoot) {
    Write-ErrorAndExit 'Unable to determine the script folder. Run this script from the repository root or use PowerShell 3.0+.'
}
Set-Location -Path $scriptRoot

function Write-ErrorAndExit {
    param([string]$Message)
    Write-Host "ERROR: $Message" -ForegroundColor Red
    exit 1
}

function Set-EnvironmentFromDotEnv {
    param([string]$FilePath)

    if (-not (Test-Path -LiteralPath $FilePath)) { return }

    Get-Content -LiteralPath $FilePath | ForEach-Object {
        $line = $_.Trim()
        if (-not $line -or $line.StartsWith('#')) { return }
        if ($line -match '^[\s]*export[\s]+') { $line = $line -replace '^[\s]*export[\s]+', '' }
        $parts = $line -split '=', 2
        if ($parts.Count -ne 2) { return }
        $name = $parts[0].Trim()
        $value = $parts[1].Trim()
        if ($value.Length -ge 2 -and ((($value.StartsWith('"') -and $value.EndsWith('"')) -or ($value.StartsWith("'") -and $value.EndsWith("'"))))) {
            $value = $value.Substring(1, $value.Length - 2)
        }
        foreach ($var in 'JAVA_HOME','ANDROID_SDK_ROOT','ANDROID_HOME','ANDROID_SDK_HOME','ANDROID_AVD_HOME','ANDROID_EMULATOR_HOME','PATH') {
            $envValue = [Environment]::GetEnvironmentVariable($var, 'Process')
            if ($null -eq $envValue) { $envValue = '' }
            $value = $value.Replace("${var}", $envValue)
            $value = $value.Replace("$$var", $envValue)
        }
        if ($name -ieq 'PATH') {
            $value = [regex]::Replace($value, ':(?![\\/])', ';')
        }
        [Environment]::SetEnvironmentVariable($name, $value, 'Process')
    }
}

$envFile = Join-Path $scriptRoot '.env'
Set-EnvironmentFromDotEnv -FilePath $envFile

# Prefer the specified JDK 17 unless an existing JAVA_HOME is already Java 17.
$desiredJavaHome = $JavaHome
$detectedJavaHome = $env:JAVA_HOME
$useJavaHome = $null
if ($detectedJavaHome) {
    $javaExe = Join-Path $detectedJavaHome 'bin\\java.exe'
    if (Test-Path $javaExe) {
        $verOut = & $javaExe -version 2>&1 | Select-String -Pattern '"(\\d+)'
        $major = $null
        if ($verOut) {
            $m = [regex]::Match($verOut[0].ToString(), '"(\\d+)\\.')
            if ($m.Success) { $major = [int]$m.Groups[1].Value }
        }
        if ($major -eq 17) {
            $useJavaHome = $detectedJavaHome
        } else {
            Write-Host "Detected JAVA_HOME points to Java $major; switching to JDK 17 at $desiredJavaHome" -ForegroundColor Yellow
            $useJavaHome = $desiredJavaHome
        }
    } else {
        Write-Host "Detected JAVA_HOME path $detectedJavaHome missing java.exe; using $desiredJavaHome" -ForegroundColor Yellow
        $useJavaHome = $desiredJavaHome
    }
} else {
    $useJavaHome = $desiredJavaHome
}

if (Test-Path (Join-Path $useJavaHome 'bin\\java.exe')) {
    $env:JAVA_HOME = $useJavaHome
} else {
    Write-Host "Preferred JDK not found at $useJavaHome; leaving existing JAVA_HOME" -ForegroundColor Yellow
}

if (-not $env:ANDROID_SDK_ROOT) { $env:ANDROID_SDK_ROOT = $AndroidSdkRoot }

$gradlew = Join-Path $scriptRoot 'gradlew.bat'
$adb = Join-Path $env:ANDROID_SDK_ROOT 'platform-tools\adb.exe'
$emulator = Join-Path $env:ANDROID_SDK_ROOT 'emulator\emulator.exe'
$emulatorWorkingDir = Join-Path $env:ANDROID_SDK_ROOT 'emulator'
$sdkManager = Join-Path $env:ANDROID_SDK_ROOT 'cmdline-tools\latest\bin\sdkmanager.bat'
$avdManager = Join-Path $env:ANDROID_SDK_ROOT 'cmdline-tools\latest\bin\avdmanager.bat'
$apkPath = Join-Path $scriptRoot 'app\build\outputs\apk\debug\app-debug.apk'
$gradleUserHome = Join-Path $env:TEMP 'Kreeda-Ankana-Gradle'

if (-not (Test-Path -LiteralPath $gradlew)) {
    Write-ErrorAndExit 'gradlew.bat not found in repository root.'
}
if (-not (Test-Path -LiteralPath "$env:JAVA_HOME\bin\java.exe")) {
    Write-ErrorAndExit "Java not found at $env:JAVA_HOME\bin\java.exe. Install JDK 17 or update JAVA_HOME."
}
if (-not (Test-Path -LiteralPath $env:ANDROID_SDK_ROOT)) {
    Write-ErrorAndExit "Android SDK root not found at $env:ANDROID_SDK_ROOT. Install the SDK or update ANDROID_SDK_ROOT."
}
if (-not (Test-Path -LiteralPath $sdkManager)) {
    Write-ErrorAndExit "sdkmanager.bat not found at $sdkManager. Install Android command-line tools."
}
if (-not (Test-Path -LiteralPath $avdManager)) {
    Write-ErrorAndExit "avdmanager.bat not found at $avdManager. Install Android command-line tools."
}
if (-not (Test-Path -LiteralPath $adb)) {
    Write-ErrorAndExit "adb.exe not found at $adb. Install platform-tools."
}
if (-not (Test-Path -LiteralPath $emulator)) {
    Write-ErrorAndExit "emulator.exe not found at $emulator. Install the Android emulator package."
}

New-Item -ItemType Directory -Force -Path $gradleUserHome | Out-Null
$env:GRADLE_USER_HOME = $gradleUserHome

Write-Host '=== Build and Run Script ===' -ForegroundColor Cyan
Write-Host "Repo root: $scriptRoot"
Write-Host "JAVA_HOME: $env:JAVA_HOME"
Write-Host "ANDROID_SDK_ROOT: $env:ANDROID_SDK_ROOT"
Write-Host "AVD Name: $AvdName"
Write-Host "Package name: $PackageName"

Write-Host ''
Write-Host 'Running Gradle assembleDebug...' -ForegroundColor Green
function Invoke-GradleAssembleDebug {
    param([int]$maxAttempts = 2)
    $attempt = 0
    while ($attempt -lt $maxAttempts) {
        $attempt++
        Write-Host "Gradle attempt $attempt of $maxAttempts..."
        $gradleOutput = & "$gradlew" '--no-daemon' '--gradle-user-home' "$gradleUserHome" 'assembleDebug' 2>&1
        $exit = $LASTEXITCODE
        if ($exit -eq 0) { return $true }
        $outStr = ($gradleOutput -join "`n")
        if ($outStr -match 'Unexpected lock protocol') {
            Write-Host 'Detected Gradle lock protocol error; clearing Gradle user home and retrying...' -ForegroundColor Yellow
            Try {
                Remove-Item -LiteralPath $gradleUserHome -Recurse -Force -ErrorAction SilentlyContinue
            } Catch { }
            New-Item -ItemType Directory -Force -Path $gradleUserHome | Out-Null
            $env:GRADLE_USER_HOME = $gradleUserHome
            continue
        }
        Write-Host $outStr
        break
    }
    return $false
}

if (-not (Invoke-GradleAssembleDebug -maxAttempts 2)) { Write-ErrorAndExit 'Gradle assembleDebug failed.' }

if (-not (Test-Path -LiteralPath $apkPath)) {
    Write-ErrorAndExit "APK not found at $apkPath after build."
}

function Get-ConnectedDevice {
    $output = & $adb devices 2>&1
    $lines = $output | Where-Object { $_ -and ($_ -notmatch 'List of devices attached') }
    $connected = $lines | Where-Object { $_ -match '\S+\s+device$' }
    return $connected
}

# Ensure adb server is fresh to reduce race conditions
Write-Host 'Restarting adb server to ensure clean state...' -ForegroundColor Yellow
& $adb kill-server 2>$null
Start-Sleep -Seconds 1
& $adb start-server 2>$null
Start-Sleep -Seconds 1

$connected = Get-ConnectedDevice
if (-not $connected) {
    Write-Host 'No device/emulator connected. Starting emulator (visible window)...' -ForegroundColor Yellow
    $emuArgs = @(
        '-avd', $AvdName,
        '-netspeed', 'full',
        '-netdelay', 'none',
        '-verbose'
    )
    $emuOutLog = Join-Path $scriptRoot 'emulator-start.out.log'
    $emuErrLog = Join-Path $scriptRoot 'emulator-start.err.log'
    if (Test-Path $emuOutLog) { Remove-Item -LiteralPath $emuOutLog -ErrorAction SilentlyContinue }
    if (Test-Path $emuErrLog) { Remove-Item -LiteralPath $emuErrLog -ErrorAction SilentlyContinue }
    Write-Host "Launching emulator: $emulator $($emuArgs -join ' ')" -ForegroundColor DarkCyan
    $emuProcess = Start-Process -FilePath $emulator -ArgumentList $emuArgs -WorkingDirectory $emulatorWorkingDir -RedirectStandardOutput $emuOutLog -RedirectStandardError $emuErrLog -WindowStyle Normal -PassThru
    Start-Sleep -Seconds 2

    $attempts = 0
    while ($attempts -lt 60) {
        if ($emuProcess.HasExited) {
            Write-Host "ERROR: Emulator process exited early with code $($emuProcess.ExitCode)." -ForegroundColor Red
            if (Test-Path $emuOutLog) {
                Write-Host "--- emulator-start.out.log (last 40 lines) ---" -ForegroundColor Yellow
                Get-Content -Path $emuOutLog -Tail 40 | ForEach-Object { Write-Host $_ }
            }
            if (Test-Path $emuErrLog) {
                Write-Host "--- emulator-start.err.log (last 40 lines) ---" -ForegroundColor Yellow
                Get-Content -Path $emuErrLog -Tail 40 | ForEach-Object { Write-Host $_ }
            }
            Write-ErrorAndExit 'Emulator process did not stay running.'
        }
        Start-Sleep -Seconds 5
        $connected = Get-ConnectedDevice
        if ($connected) { break }
        Write-Host "Waiting for emulator to connect... ($($attempts + 1)/60)"
        $attempts++
    }
    if (-not $connected) { Write-ErrorAndExit 'Emulator did not connect within 300 seconds.' }

    Write-Host 'Emulator connected, waiting for Android boot to complete...' -ForegroundColor Yellow
    $bootAttempts = 0
    while ($bootAttempts -lt 60) {
        Start-Sleep -Seconds 5
        $bootStatus = & $adb shell getprop sys.boot_completed 2>$null
        if ($bootStatus -eq '1') { break }
        Write-Host "Waiting for emulator boot complete... ($($bootAttempts + 1)/60)"
        $bootAttempts++
    }
    if ($bootStatus -ne '1') { Write-ErrorAndExit 'Emulator did not finish booting within 300 seconds.' }
}

Write-Host 'Device/emulator connected and booted.' -ForegroundColor Green
Write-Host "Installing APK: $apkPath" -ForegroundColor Green
& $adb install -r $apkPath
if ($LASTEXITCODE -ne 0) { Write-ErrorAndExit 'adb install failed.' }

Write-Host 'Launching app on device...' -ForegroundColor Green
& $adb shell monkey -p $PackageName -c android.intent.category.LAUNCHER 1
if ($LASTEXITCODE -ne 0) {
    Write-Host 'WARN: App launch command failed. The APK was installed successfully.' -ForegroundColor Yellow
} else {
    Write-Host 'App launched successfully.' -ForegroundColor Green
}

Write-Host 'Build, install, and launch complete.' -ForegroundColor Cyan
