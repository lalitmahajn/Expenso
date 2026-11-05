@echo off
echo ====================================================
echo Starting Expenso Android Emulator (medium_phone)...
echo ====================================================

REM 1. Start the emulator in a separate window
start "" "%LOCALAPPDATA%\Android\Sdk\emulator\emulator.exe" -avd medium_phone

echo Waiting for emulator to initialize adb connection...
"%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" wait-for-device

echo Waiting for Android boot to complete...
:wait_boot
for /f "tokens=*" %%a in ('"%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" shell getprop sys.boot_completed 2^>nul') do set BOOT=%%a
if not "%BOOT%"=="1" (
    timeout /t 2 /nobreak >nul
    goto wait_boot
)

echo Android booted! Installing latest build with Custom Categories...
"%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" install -r "%~dp0app\build\outputs\apk\debug\app-debug.apk"

echo Launching Expenso...
"%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" shell am start -n com.expenso.app/com.example.expenso.MainActivity
echo.
echo Expenso is now running in your emulator window!
