param([string]$JarPath = '')
$ErrorActionPreference = 'Stop'
if (!$JarPath) { $JarPath = Join-Path (Split-Path $PSScriptRoot -Parent) 'dist/slime-gun-1.0.0.jar' }
Add-Type -AssemblyName System.IO.Compression.FileSystem
$slimeJar = [System.IO.Compression.ZipFile]::OpenRead($JarPath)
try {
    function Read-SlimeJarJson([string]$Name) {
        $slimeEntry = $slimeJar.GetEntry($Name)
        if (!$slimeEntry) { throw "Missing JAR resource: $Name" }
        $slimeReader = [System.IO.StreamReader]::new($slimeEntry.Open())
        try { return $slimeReader.ReadToEnd() | ConvertFrom-Json } finally { $slimeReader.Dispose() }
    }
    $slimeMetadata = Read-SlimeJarJson 'fabric.mod.json'
    if ($slimeMetadata.id -ne 'slimegun' -or $slimeMetadata.version -ne '1.0.0' -or $slimeMetadata.depends.minecraft -ne '1.21.1') { throw 'Wrong mod metadata.' }
    $slimeMixin = Read-SlimeJarJson 'slimegun.mixins.json'
    $slimeRefmap = Read-SlimeJarJson $slimeMixin.refmap
    if ($slimeRefmap.mappings.'dev/slimegun/mixin/FrozenMobAiMixin'.tickNewAi -notmatch 'method_6023') { throw 'Production mixin mappings are missing.' }
    foreach ($slimeResource in @('assets/slimegun/textures/item/slime_gun.png','assets/slimegun/models/item/slime_gun.json','assets/slimegun/blockstates/slime_cocoon.json','assets/slimegun/textures/mob_effect/slimebound.png','data/slimegun/recipe/slime_gun.json')) {
        if (!$slimeJar.GetEntry($slimeResource)) { throw "Missing release resource: $slimeResource" }
    }
    foreach ($slimeEntry in $slimeJar.Entries) {
        if ($slimeEntry.FullName -match 'GameTests|FreezeRulesTest|^net/minecraft/') { throw 'Test classes or Minecraft classes must not be bundled.' }
    }
    Write-Output 'PASS: release metadata, remapped mixin references, textures/models, recipe, and clean production contents.'
} finally { $slimeJar.Dispose() }
