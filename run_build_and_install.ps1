# run_build_and_install.ps1
# Usage: Open PowerShell, cd to project root and run:
#   powershell -ExecutionPolicy Bypass -File .\run_build_and_install.ps1

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Definition
Set-Location $projectRoot
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "       SongVerse Android Builder        " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Project root: $projectRoot" -ForegroundColor White

# Auto-detect Android Studio JDK
$studioJdk = "C:\Program Files\Android\Android Studio\jbr"
if (Test-Path $studioJdk) {
    $env:JAVA_HOME = $studioJdk
    $env:PATH = "$studioJdk\bin;" + $env:PATH
    Write-Host "Using Android Studio JDK: $studioJdk" -ForegroundColor Green
}

# Auto-detect Android SDK and Platform-Tools
$sdkDir = "C:\Users\$env:USERNAME\AppData\Local\Android\Sdk"
if (-Not (Test-Path $sdkDir)) {
    $sdkDir = "C:\Users\USER\AppData\Local\Android\Sdk"
}
if (Test-Path $sdkDir) {
    $env:ANDROID_HOME = $sdkDir
    $platformTools = Join-Path $sdkDir "platform-tools"
    if (Test-Path $platformTools) {
        $env:PATH = "$platformTools;" + $env:PATH
        Write-Host "Using Android SDK: $sdkDir" -ForegroundColor Green
    }
}

# Ensure .env exists
if (-Not (Test-Path (Join-Path $projectRoot ".env"))) {
    if (Test-Path (Join-Path $projectRoot ".env.example")) {
        Copy-Item (Join-Path $projectRoot ".env.example") (Join-Path $projectRoot ".env")
        Write-Host "Created .env from .env.example" -ForegroundColor Green
    }
}

# Run build
Write-Host "Building debug APK with Gradle..." -ForegroundColor Cyan
& .\gradlew.bat assembleDebug
$gradleExit = $LASTEXITCODE

if ($gradleExit -ne 0) {
    Write-Host "Gradle build failed with exit code $gradleExit." -ForegroundColor Red
    exit 1
}

$apkPath = Join-Path $projectRoot 'app\build\outputs\apk\debug\app-debug.apk'
if (-Not (Test-Path $apkPath)) {
    Write-Host "APK not found at expected path: $apkPath" -ForegroundColor Red
    exit 1
}
Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host " BUILD SUCCEEDED!" -ForegroundColor Green
Write-Host " APK location: $apkPath" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

# Verify connected device / emulator
Write-Host "Checking connected Android devices..." -ForegroundColor Cyan
try {
    $devices = & adb devices 2>&1
    Write-Host $devices
    if ($devices -match "device`$" -or $devices -match "device\r?\n") {
        Write-Host "Device detected! Installing APK..." -ForegroundColor Green
        & adb install -r $apkPath
        Write-Host "APK installed successfully! Launch SongVerse on your device." -ForegroundColor Green
    } else {
        Write-Host "No active Android device or emulator detected." -ForegroundColor Yellow
        Write-Host "You can transfer and install the APK directly to any phone: $apkPath" -ForegroundColor Cyan
    }
} catch {
    Write-Host "adb check skipped." -ForegroundColor Yellow
}

Write-Host "All done!" -ForegroundColor Green
