$slimeRoot = Split-Path $PSScriptRoot -Parent
$slimeJava = Get-ChildItem -LiteralPath (Join-Path $slimeRoot '.runtime/jdk') -Directory | Select-Object -First 1
if (!$slimeJava) { throw 'Run Tools/Setup.ps1 first.' }
$env:JAVA_HOME = $slimeJava.FullName
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
$env:GRADLE_USER_HOME = Join-Path $slimeRoot '.runtime/gradle-cache'
$slimeGradle = Join-Path $slimeRoot '.runtime/gradle/gradle-8.14.3/bin/gradle.bat'
