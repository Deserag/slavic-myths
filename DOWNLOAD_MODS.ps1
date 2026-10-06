param([switch]$CoreOnly)
$ErrorActionPreference = 'Stop'
$lock = Get-Content -LiteralPath (Join-Path $PSScriptRoot 'MODS_LOCK.json') -Raw -Encoding UTF8 | ConvertFrom-Json
$modsDirectory = Join-Path $PSScriptRoot 'mods'
New-Item -ItemType Directory -Path $modsDirectory -Force | Out-Null
$mainMod = Join-Path $modsDirectory $lock.slavicmyths.filename
if (!(Test-Path -LiteralPath $mainMod) -or (Get-FileHash -LiteralPath $mainMod -Algorithm SHA256).Hash.ToLowerInvariant() -ne $lock.slavicmyths.sha256) {
    throw 'Slavic Myths JAR is missing or checksum is incorrect. Extract the distribution ZIP again.'
}
foreach ($mod in $lock.mods) {
    if ($CoreOnly -and !$mod.required) { continue }
    if ([IO.Path]::GetFileName($mod.filename) -ne $mod.filename -or !$mod.filename.EndsWith('.jar')) { throw 'Invalid manifest filename.' }
    $target = Join-Path $modsDirectory $mod.filename
    if (Test-Path -LiteralPath $target) {
        if ((Get-FileHash -LiteralPath $target -Algorithm SHA256).Hash.ToLowerInvariant() -ne $mod.sha256) { throw "Existing file differs: $($mod.filename). Move it aside manually." }
        Write-Host "Verified: $($mod.filename)"
        continue
    }
    $temporary = Join-Path $modsDirectory ($mod.filename + '.' + [Guid]::NewGuid().ToString('N') + '.download')
    try {
        Write-Host "Downloading: $($mod.filename)"
        Invoke-WebRequest -UseBasicParsing -Uri $mod.downloadUrl -OutFile $temporary
        if ((Get-Item -LiteralPath $temporary).Length -ne $mod.size -or (Get-FileHash -LiteralPath $temporary -Algorithm SHA256).Hash.ToLowerInvariant() -ne $mod.sha256) { throw "Download verification failed: $($mod.filename)" }
        Move-Item -LiteralPath $temporary -Destination $target
    } finally {
        if (Test-Path -LiteralPath $temporary) { Remove-Item -LiteralPath $temporary }
    }
}
Write-Host 'Done. Copy the mods folder contents into a separate Minecraft 1.21.1 / NeoForge 21.1.255 profile using Java 21.'
Write-Host 'Minecraft has not been started. Existing game profiles, worlds and configs have not been modified.'
