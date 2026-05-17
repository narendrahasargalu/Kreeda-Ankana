param(
    [Parameter(Mandatory=$true)]
    [string]$EnvFile,

    [Parameter(Mandatory=$true)]
    [string]$OutputFile
)

if (-not (Test-Path -LiteralPath $EnvFile)) {
    Write-Error "Env file not found: $EnvFile"
    exit 1
}

Remove-Item -LiteralPath $OutputFile -ErrorAction SilentlyContinue

$lines = Get-Content -LiteralPath $EnvFile -ErrorAction Stop
$outLines = @()
foreach ($line in $lines) {
    $line = $line.Trim()
    if (-not $line -or $line.StartsWith('#')) { continue }
    if ($line -match '^[\s]*export[\s]+') {
        $line = $line -replace '^[\s]*export[\s]+', ''
    }

    $parts = $line -split '=', 2
    if ($parts.Count -ne 2) { continue }

    $name = $parts[0].Trim()
    $value = $parts[1].Trim()

    if ($value.Length -ge 2 -and ((($value.StartsWith('"') -and $value.EndsWith('"')) -or ($value.StartsWith("'") -and $value.EndsWith("'")))) ) {
        $value = $value.Substring(1, $value.Length - 2)
    }

    foreach ($var in 'JAVA_HOME', 'ANDROID_SDK_ROOT', 'ANDROID_HOME', 'ANDROID_SDK_HOME', 'ANDROID_AVD_HOME', 'ANDROID_EMULATOR_HOME', 'PATH') {
        $envValue = [System.Environment]::GetEnvironmentVariable($var, 'Process')
        if ($null -eq $envValue) { $envValue = '' }
        $value = $value.Replace('${' + $var + '}', $envValue)
        $value = $value.Replace('$' + $var, $envValue)
    }

    if ($name -ieq 'PATH') {
        $value = [regex]::Replace($value, ':(?![\\/])', ';')
    }

    [System.Environment]::SetEnvironmentVariable($name, $value, 'Process')

    # Emit a safe batch `set` command to preserve characters and spaces.
    $escaped = $value -replace '"', '""'
    $outLines += "set `"${name}=${escaped}`""

}

# Write as ASCII so cmd.exe can execute the file reliably.
[System.IO.File]::WriteAllLines($OutputFile, $outLines, [System.Text.Encoding]::ASCII)

exit 0
