# SongVerse Auto-Installer for Connected Android Device
$adb = "C:\Users\USER\AppData\Local\Android\Sdk\platform-tools\adb.exe"
$apk = "C:\Users\USER\OneDrive\Desktop\songverse\app\build\outputs\apk\debug\app-debug.apk"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host " SongVerse Redmi Note 13 Pro Installer" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Waiting for device authorization..." -ForegroundColor Yellow

$attempt = 0
while ($true) {
    $devices = & $adb devices
    $onlineDevice = $devices | Where-Object { $_ -match "\s+device$" }
    $unauthDevice = $devices | Where-Object { $_ -match "\s+unauthorized$" }

    if ($unauthDevice) {
        Write-Host "`n[!] Device detected but unauthorized. Please tap 'ALLOW' on your Redmi screen!" -ForegroundColor Magenta
    }

    if ($onlineDevice) {
        $devId = ($onlineDevice -split "\s+")[0]
        Write-Host "`n[+] Device connected: $devId" -ForegroundColor Green
        Write-Host "[*] Installing SongVerse APK to your Redmi Note 13 Pro..." -ForegroundColor Cyan
        
        $installResult = & $adb install -r $apk
        Write-Host $installResult
        
        if ($installResult -match "Success") {
            Write-Host "`n==========================================" -ForegroundColor Green
            Write-Host " SUCCESS! SongVerse is now installed on your phone!" -ForegroundColor Green
            Write-Host "==========================================" -ForegroundColor Green
            
            Write-Host "[*] Launching SongVerse on phone..." -ForegroundColor Cyan
            & $adb shell am start -n "com.example.songverse/com.example.MainActivity"
            break
        } elseif ($installResult -match "INSTALL_FAILED_USER_RESTRICTED") {
            Write-Host "[!] Xiaomi Restriction: Please enable 'Install via USB' in Developer Options on your phone and allow the prompt." -ForegroundColor Yellow
        }
    }

    $attempt++
    Start-Sleep -Seconds 2
    if ($attempt -gt 150) {
        Write-Host "`nTimeout waiting for device. Please check USB debugging." -ForegroundColor Red
        break
    }
}
