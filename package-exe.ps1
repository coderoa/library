param(
    [switch]$Installer
)

$ErrorActionPreference = "Stop"

$AppName = "LibraryJava"
$AppJar = "LibraryJava-1.0-SNAPSHOT.jar"
$MainClass = "Main"
$InputDir = Join-Path $PSScriptRoot "target\jpackage-input"
$OutputDir = Join-Path $PSScriptRoot "target\jpackage"
$AppImageDir = Join-Path $OutputDir $AppName

function Invoke-Checked {
    param(
        [string]$FilePath,
        [string[]]$Arguments
    )

    & $FilePath @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "$FilePath failed with exit code $LASTEXITCODE"
    }
}

Push-Location $PSScriptRoot
try {
    if (Test-Path $OutputDir) {
        Remove-Item -LiteralPath $OutputDir -Recurse -Force
    }

    Invoke-Checked "mvn" @("clean", "package")

    if (Test-Path $InputDir) {
        Remove-Item -LiteralPath $InputDir -Recurse -Force
    }
    New-Item -ItemType Directory -Path $InputDir | Out-Null

    Invoke-Checked "mvn" @("dependency:copy-dependencies", "-DincludeScope=runtime", "-DoutputDirectory=$InputDir")
    Copy-Item -LiteralPath (Join-Path "target" $AppJar) -Destination (Join-Path $InputDir $AppJar) -Force

    $commonArgs = @(
        "--name", $AppName,
        "--input", $InputDir,
        "--main-jar", $AppJar,
        "--main-class", $MainClass,
        "--dest", $OutputDir,
        "--vendor", "Ozodbek",
        "--java-options", "--enable-native-access=javafx.graphics"
    )

    if ($Installer) {
        $InstallerPath = Join-Path $OutputDir "$AppName-1.0.exe"
        if (Test-Path $InstallerPath) {
            Remove-Item -LiteralPath $InstallerPath -Force
        }

        Invoke-Checked "jpackage" (@("--type", "exe") + $commonArgs)
    } else {
        if (Test-Path $AppImageDir) {
            Remove-Item -LiteralPath $AppImageDir -Recurse -Force
        }

        Invoke-Checked "jpackage" (@("--type", "app-image") + $commonArgs)
    }

    Write-Host ""
    if ($Installer) {
        Write-Host "Installer created in: $OutputDir"
    } else {
        Write-Host "Application created at: $AppImageDir"
        Write-Host "Run: $AppImageDir\$AppName.exe"
    }
} finally {
    Pop-Location
}
