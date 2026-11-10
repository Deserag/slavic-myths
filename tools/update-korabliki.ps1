param(
    [string]$ModsDirectory,
    [switch]$BuildOnly
)

$ErrorActionPreference = 'Stop'
$branch = 'feature/2.6-ship-superstructures-20261110'
$repo = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
Push-Location $repo
try {
    $dirty = @(git status --porcelain)
    if ($LASTEXITCODE -ne 0) { throw 'Not a Git repository.' }
    if (@($dirty | Where-Object { $_.Trim() }).Count -ne 0) {
        throw 'Uncommitted local changes. Commit or stash them first.'
    }
    git fetch origin $branch
    if ($LASTEXITCODE -ne 0) { throw 'git fetch failed.' }
    git switch $branch
    if ($LASTEXITCODE -ne 0) {
        git switch -c $branch --track "origin/$branch"
        if ($LASTEXITCODE -ne 0) { throw 'Cannot switch to branch.' }
    }
    git pull --ff-only origin $branch
    if ($LASTEXITCODE -ne 0) { throw 'git pull failed.' }

    & (Join-Path $repo 'gradlew.bat') --no-daemon clean build
    if ($LASTEXITCODE -ne 0) { throw 'Build failed. No JAR installed.' }

    $candidates = @(Get-ChildItem -LiteralPath (Join-Path $repo 'build/libs') -Filter 'slavicmyths*.jar' -File |
        Where-Object { $_.Name -notmatch '(-sources|-javadoc|-dev|-all)\.jar$' })
    if ($candidates.Count -ne 1) {
        throw "Expected one release JAR, found $($candidates.Count). No JAR installed."
    }
    $jar = $candidates[0]
    $hash = (Get-FileHash -LiteralPath $jar.FullName -Algorithm SHA256).Hash
    Write-Host "Built: $($jar.FullName)"
    Write-Host "SHA256: $hash"
    if ($BuildOnly) { return }

    if ([string]::IsNullOrWhiteSpace($ModsDirectory) -or
            -not (Test-Path -LiteralPath $ModsDirectory -PathType Container)) {
        throw 'Specify a valid -ModsDirectory for the Korabliki profile.'
    }
    $existing = @(Get-ChildItem -LiteralPath $ModsDirectory -Filter 'slavicmyths*.jar' -File)
    $backup = Join-Path $ModsDirectory ('_backup_slavicmyths_' + (Get-Date -Format 'yyyyMMdd_HHmmss'))
    if ($existing.Count -gt 0) {
        New-Item -ItemType Directory -Path $backup -ErrorAction Stop | Out-Null
        foreach ($old in $existing) {
            Copy-Item -LiteralPath $old.FullName -Destination $backup -ErrorAction Stop
        }
    }

    $target = Join-Path $ModsDirectory $jar.Name
    $stage = $target + '.staging'
    Copy-Item -LiteralPath $jar.FullName -Destination $stage -Force
    if ((Get-FileHash -LiteralPath $stage -Algorithm SHA256).Hash -ne $hash) {
        Remove-Item -LiteralPath $stage -Force
        throw 'SHA256 mismatch during staging. Original JARs remain untouched.'
    }
    Copy-Item -LiteralPath $stage -Destination $target -Force
    Remove-Item -LiteralPath $stage -Force
    if ((Get-FileHash -LiteralPath $target -Algorithm SHA256).Hash -ne $hash) {
        throw 'Installed JAR SHA256 mismatch. Restore from the backup folder.'
    }
    foreach ($old in $existing) {
        if ($old.Name -ne $jar.Name) {
            Remove-Item -LiteralPath $old.FullName -Force
        }
    }
    Write-Host "Installed: $target"
    Write-Host "Backup: $backup"
} finally {
    Pop-Location
}
