param([switch]$Client)
$ErrorActionPreference = 'Stop'
if (!(Test-Path -LiteralPath (Join-Path $PSScriptRoot '.runtime/jdk'))) { & (Join-Path $PSScriptRoot 'Tools/Setup.ps1') }
. (Join-Path $PSScriptRoot 'Tools/Environment.ps1')
Push-Location $PSScriptRoot
try {
    if ($Client) {
        & $slimeGradle --no-daemon runClient
        if ($LASTEXITCODE -ne 0) { throw 'Development client failed.' }
        return
    }
    & $slimeGradle --no-daemon build
    if ($LASTEXITCODE -ne 0) { throw 'Build/tests failed. See the output above.' }
    New-Item -ItemType Directory -Path dist -Force | Out-Null
    Copy-Item -LiteralPath 'build/libs/slime-gun-1.0.0.jar' -Destination 'dist/slime-gun-1.0.0.jar' -Force
    & './Tools/Verify-Jar.ps1'
    Compress-Archive -LiteralPath 'dist/slime-gun-1.0.0.jar','README.md','LICENSE' -DestinationPath 'dist/Slime-Gun-Fabric-1.21.1.zip' -Force
    Write-Output 'Ready: dist/slime-gun-1.0.0.jar'
} finally { Pop-Location }
