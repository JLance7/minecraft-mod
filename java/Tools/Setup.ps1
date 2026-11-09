$ErrorActionPreference = 'Stop'
$slimeRoot = Split-Path $PSScriptRoot -Parent
$slimeConfig = Get-Content -LiteralPath (Join-Path $PSScriptRoot 'toolchain.json') -Raw | ConvertFrom-Json
$slimeRuntime = Join-Path $slimeRoot '.runtime'
$slimeDownloads = Join-Path $slimeRuntime 'downloads'
New-Item -ItemType Directory -Path $slimeDownloads -Force | Out-Null
function Get-SlimeArchive([string]$Url, [string]$Name, [string]$Hash) {
    $slimeArchive = Join-Path $slimeDownloads $Name
    if (!(Test-Path -LiteralPath $slimeArchive)) {
        & curl.exe --fail --location --retry 2 --silent --show-error --output $slimeArchive $Url
        if ($LASTEXITCODE -ne 0) { throw "Download failed: $Url" }
    }
    if ((Get-FileHash -LiteralPath $slimeArchive -Algorithm SHA256).Hash -ne $Hash) { throw "Checksum mismatch: $Name" }
    return $slimeArchive
}
$slimeJdkArchive = Get-SlimeArchive $slimeConfig.jdkUrl 'jdk21.zip' $slimeConfig.jdkHash
if (!(Test-Path -LiteralPath (Join-Path $slimeRuntime 'jdk'))) {
    Expand-Archive -LiteralPath $slimeJdkArchive -DestinationPath (Join-Path $slimeRuntime 'jdk')
}
$slimeGradleArchive = Get-SlimeArchive $slimeConfig.gradleUrl 'gradle.zip' $slimeConfig.gradleHash
if (!(Test-Path -LiteralPath (Join-Path $slimeRuntime 'gradle'))) {
    Expand-Archive -LiteralPath $slimeGradleArchive -DestinationPath (Join-Path $slimeRuntime 'gradle')
}
Write-Output 'Project-local Java 21 and Gradle ready; system Java/PATH unchanged.'
