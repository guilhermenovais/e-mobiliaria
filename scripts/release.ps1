$ErrorActionPreference = "Stop"

$root    = (Resolve-Path "$PSScriptRoot\..").Path
$version = (Get-Content "$root\src\main\resources\version.txt").Trim()
$dist    = "$root\target\app"
$mvnw    = "$root\mvnw.cmd"
$jlink   = if ($env:JAVA_HOME) { "$env:JAVA_HOME\bin\jlink.exe" } else { "jlink" }
$jdeps   = if ($env:JAVA_HOME) { "$env:JAVA_HOME\bin\jdeps.exe" } else { "jdeps" }

Write-Host "==> Building e-Mobiliaria $version for release..."

# 1. Build the application JAR
& $mvnw -f "$root\pom.xml" package -DskipTests
if ($LASTEXITCODE -ne 0) { throw "mvn package failed" }

# 2. Prepare dist directory structure: app/{bin,lib,runtime}
if (Test-Path $dist) { Remove-Item $dist -Recurse -Force }
New-Item -ItemType Directory -Path "$dist\bin" | Out-Null
New-Item -ItemType Directory -Path "$dist\lib" | Out-Null

# 3. Copy all runtime dependencies - exclude cross-platform JavaFX JARs (no native DLLs)
Write-Host "==> Copying runtime dependencies..."
& $mvnw -f "$root\pom.xml" dependency:copy-dependencies `
    "-DoutputDirectory=$dist\lib" `
    "-DincludeScope=runtime" `
    "-DexcludeGroupIds=org.openjfx" `
    -q
if ($LASTEXITCODE -ne 0) { throw "dependency:copy-dependencies failed" }

# 4. Download Windows-specific JavaFX JARs (these carry the native .dll files)
Write-Host "==> Downloading Windows JavaFX JARs..."
$jfxVersion   = "21.0.6"
$jfxArtifacts = @("javafx-base", "javafx-controls", "javafx-fxml", "javafx-graphics", "javafx-media")
foreach ($artifact in $jfxArtifacts) {
    & $mvnw -f "$root\pom.xml" dependency:copy `
        "-Dartifact=org.openjfx:${artifact}:${jfxVersion}:jar:win" `
        "-DoutputDirectory=$dist\lib" `
        -q
    if ($LASTEXITCODE -ne 0) { Write-Warning "Could not fetch $artifact-win, skipping" }
}

# 5. Copy the application JAR into lib/
$appJar = Get-ChildItem "$root\target" -Filter "e-mobiliaria-*.jar" |
    Where-Object { $_.Name -notlike "*-tests*" } |
    Select-Object -First 1
if (-not $appJar) { throw "Application JAR not found under target/" }
Copy-Item $appJar.FullName "$dist\lib\"

# 6. Determine which JDK system modules are needed (automatic-module JARs stay in lib/)
Write-Host "==> Detecting required JDK modules with jdeps..."
$allJarsCp = (Get-ChildItem "$dist\lib" -Filter "*.jar" |
    ForEach-Object { $_.FullName }) -join ";"
$jdepsResult = & $jdeps `
    --module-path $allJarsCp `
    --ignore-missing-deps `
    --print-module-deps `
    $appJar.FullName 2>&1

$jdkModules = ($jdepsResult | Where-Object { $_ -match "^java\.|^jdk\." } | Select-Object -Last 1)
if (-not $jdkModules) {
    # Safe fallback covering everything the app transitively needs
    $jdkModules = "java.base,java.desktop,java.sql,java.net.http,java.logging,java.naming,java.management,java.xml,jdk.unsupported"
    Write-Warning "jdeps module detection failed - using fallback set: $jdkModules"
} else {
    # Ensure jdk.unsupported is present (Guice and many reflection-heavy libs need Unsafe)
    if ($jdkModules -notmatch "jdk\.unsupported") {
        $jdkModules = "$jdkModules,jdk.unsupported"
    }
    Write-Host "JDK modules: $jdkModules"
}

# 7. Build a minimal JRE from JDK system modules only (no automatic modules here)
Write-Host "==> Building minimal JRE with jlink..."
& $jlink --no-header-files --no-man-pages --compress zip-6 `
    --add-modules $jdkModules `
    --output "$dist\runtime"
if ($LASTEXITCODE -ne 0) { throw "jlink failed" }

# 8. Create the Windows launcher
#    --module-path puts all JARs (including automatic modules) on the module path at runtime
$launcherLines = (
    '@echo off',
    'set SCRIPT_DIR=%~dp0',
    '"%SCRIPT_DIR%..\runtime\bin\java" --module-path "%SCRIPT_DIR%..\lib" --module com.guilherme.emobiliaria/com.guilherme.emobiliaria.EMobiliariaApplication'
)
[System.IO.File]::WriteAllLines("$dist\bin\app.bat", $launcherLines, [System.Text.Encoding]::ASCII)

# 9. Package into a zip whose root entry is the "app" directory
#    UpdateService.findLauncher() walks 3 levels and expects parent == "bin", name == "app.bat"
#    Resulting structure: app/bin/app.bat  (depth 3 from unzip target - OK)
Write-Host "==> Creating release zip..."
$zipPath = "$root\target\e-mobiliaria-$version.zip"
if (Test-Path $zipPath) { Remove-Item $zipPath }
Compress-Archive -Path $dist -DestinationPath $zipPath
Copy-Item $zipPath "$root\target\app.zip" -Force

Write-Host ""
Write-Host "Release artifact: $zipPath"
