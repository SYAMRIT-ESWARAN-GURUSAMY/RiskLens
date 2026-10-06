@echo off
setlocal
cd /d "%~dp0"

echo Compiling SentinelX...
if not exist out mkdir out
powershell -NoProfile -Command "$n=(Get-Location).Path.Length+1; Get-ChildItem -Recurse -Filter *.java src | ForEach-Object { $_.FullName.Substring($n) } | Set-Content -Encoding ascii sources.txt"
javac --release 21 -d out @sources.txt
if errorlevel 1 (
    echo.
    echo Compilation failed. Check that JDK 21 is installed.
    pause
    exit /b 1
)

java -cp out sentinelx.app.Main
if errorlevel 1 pause
