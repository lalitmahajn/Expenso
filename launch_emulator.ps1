# Launch Expenso Android Emulator & Install Latest Build
Write-Host "====================================================" -ForegroundColor Cyan
Write-Host "Starting Expenso Android Emulator (medium_phone)..." -ForegroundColor Cyan
Write-Host "====================================================" -ForegroundColor Cyan

$emulatorPath = "$env:LOCALAPPDATA\Android\Sdk\emulator\emulator.exe"
$adbPath = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
$apkPath = "$PSScriptRoot\app\build\outputs\apk\debug\app-debug.apk"

# Start emulator as an independent GUI process
Start-Process -FilePath $emulatorPath -ArgumentList "-avd", "medium_phone"

Write-Host "Waiting for device to connect to ADB..." -ForegroundColor Yellow
& $adbPath wait-for-device

Write-Host "Waiting for system boot..." -ForegroundColor Yellow
while ((& $adbPath shell getprop sys.boot_completed).Trim() -ne "1") {
    Start-Sleep -Seconds 1
}

Write-Host "Installing latest Expenso build with Custom Categories..." -ForegroundColor Yellow
& $adbPath install -r $apkPath

Write-Host "Launching Expenso..." -ForegroundColor Green
& $adbPath shell am start -n com.expenso.app/com.example.expenso.MainActivity
Write-Host "Done! Expenso is running on your screen." -ForegroundColor Green
