param(
    [string]$ModsDirectory,
    [switch]$BuildOnly,
    [string]$Branch = 'integration/2.6-modular-ships-20261110'
)

$ErrorActionPreference = 'Stop'
$Repo = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
Push-Location $Repo
try {
    if (-not $BuildOnly) {
    $changes = @(git status --porcelain)
    if ($LASTEXITCODE -ne 0) { throw 'Git repository cannot be inspected.' }
    if ($changes.Count -gt 0 -and ($changes -join '').Trim()) {
        throw 'Uncommitted changes found. Commit or stash them before switching branches.'
    }

    git fetch origin $Branch
    if ($LASTEXITCODE -ne 0) { throw 'Could not fetch ship branch.' }
    git switch $Branch 2>$null
    if ($LASTEXITCODE -ne 0) {
        git switch --track "origin/$Branch"
        if ($LASTEXITCODE -ne 0) { throw 'Could not switch to ship branch.' }
    }
    git pull --ff-only origin $Branch
    if ($LASTEXITCODE -ne 0) { throw 'Pull failed (not a fast-forward).' }
    }

    & (Join-Path $Repo 'gradlew.bat') --no-daemon clean build
    if ($LASTEXITCODE -ne 0) { throw 'Gradle build failed; installed mods were not modified.' }

    $candidate = @(Get-ChildItem (Join-Path $Repo 'build/libs') -Filter 'slavicmyths*.jar' |
        Where-Object { $_.Name -notmatch '(sources|javadoc|dev|all)\.jar$' } |
        Sort-Object LastWriteTime -Descending)
    if ($candidate.Count -ne 1) {
        throw "Expected exactly one release candidate, found $($candidate.Count). Nothing installed."
    }
    $jar = $candidate[0]
    $hash = (Get-FileHash -LiteralPath $jar.FullName -Algorithm SHA256).Hash
    Write-Host "Built: $($jar.FullName)"
    Write-Host "SHA256: $hash"

    if (-not $BuildOnly) {
        if ([string]::IsNullOrWhiteSpace($ModsDirectory)) { throw 'Pass -ModsDirectory for installation or use -BuildOnly.' }
        if (-not (Test-Path -LiteralPath $ModsDirectory -PathType Container)) {
            throw "Mods folder does not exist: $ModsDirectory"
        }
        $existing = @(Get-ChildItem -LiteralPath $ModsDirectory -Filter 'slavicmyths*.jar' -File)
        $backup = Join-Path $ModsDirectory ('_backup_slavicmyths_' + (Get-Date -Format 'yyyyMMdd_HHmmss'))
        if ($existing.Count -gt 0) {
            New-Item -ItemType Directory -Path $backup -ErrorAction Stop | Out-Null
            foreach ($old in $existing) {
                Copy-Item -LiteralPath $old.FullName -Destination $backup -ErrorAction Stop
            }
        }
        $staged = Join-Path $ModsDirectory ($jar.Name + '.staging')
        Copy-Item -LiteralPath $jar.FullName -Destination $staged -Force
        $stagedHash = (Get-FileHash -LiteralPath $staged -Algorithm SHA256).Hash
        if ($stagedHash -ne $hash) {
            Remove-Item -LiteralPath $staged -Force
            throw 'Staged JAR SHA256 differs. Previous versions remain unchanged.'
        }
        Copy-Item -LiteralPath $staged -Destination (Join-Path $ModsDirectory $jar.Name) -Force
        Remove-Item -LiteralPath $staged -Force
        $installedHash = (Get-FileHash -LiteralPath (Join-Path $ModsDirectory $jar.Name) -Algorithm SHA256).Hash
        if ($installedHash -ne $hash) {
            throw 'Installed JAR verification failed. Restore the previous file from backup.'
        }
        foreach ($old in $existing) {
            if ($old.Name -ne $jar.Name) { Remove-Item -LiteralPath $old.FullName -Force }
        }
        Write-Host "Installed into $ModsDirectory; previous JARs backed up to $backup"
    }
} finally {
    Pop-Location
}
