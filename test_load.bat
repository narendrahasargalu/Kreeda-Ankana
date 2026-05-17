@echo off
setlocal enabledelayedexpansion
set "ENV_DOTENV=.\temp-env-output.txt"
echo Processing %ENV_DOTENV%
for /f "usebackq tokens=1* delims==" %%A in ("%ENV_DOTENV%") do (
	call set "%%A=%%B"
)
echo JAVA_HOME=%JAVA_HOME%
exit /b 0
