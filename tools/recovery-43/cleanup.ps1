[CmdletBinding(SupportsShouldProcess=$true, ConfirmImpact='High')]
param([string[]]$ApprovedPath = @(), [switch]$Execute)
$ErrorActionPreference = 'Stop'
$cleanupManifestPath = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../../docs/maintenance/issue-43/LOCAL_SIZES_BEFORE_AFTER.csv'))
$cleanupRows = Import-Csv -LiteralPath $cleanupManifestPath
if ($ApprovedPath.Count -eq 0) { $cleanupRows | Where-Object decision -eq 'SAFE_TO_REMOVE' | Select-Object path,bytes,backup; return }
Add-Type -AssemblyName System.IO.Compression.FileSystem
$checkedArchives = @{}
foreach ($requestedPath in $ApprovedPath) {
    $resolvedCleanupPath = (Resolve-Path -LiteralPath $requestedPath).Path
    $cleanupRow = $cleanupRows | Where-Object { $_.path -eq $resolvedCleanupPath -and $_.decision -eq 'SAFE_TO_REMOVE' }
    if (@($cleanupRow).Count -ne 1) { throw "Path is not an exact SAFE_TO_REMOVE manifest entry: $resolvedCleanupPath" }
    $resolvedCleanupRoot = (Resolve-Path -LiteralPath $cleanupRow.root).Path.TrimEnd('\')
    if (-not $resolvedCleanupPath.StartsWith($resolvedCleanupRoot + '\', [StringComparison]::OrdinalIgnoreCase)) { throw 'Target is outside its verified workspace' }
    if ($resolvedCleanupPath -match '[\\/]\.git([\\/]|$)') { throw 'Git storage removal is prohibited' }
    if (-not $checkedArchives.ContainsKey($cleanupRow.backup)) {
        if ((Get-FileHash -LiteralPath $cleanupRow.backup -Algorithm SHA256).Hash -ne $cleanupRow.backup_sha256) { throw 'Backup archive hash mismatch' }
        $cleanupZip = [IO.Compression.ZipFile]::OpenRead($cleanupRow.backup)
        try {
            $cleanupReader = [IO.StreamReader]::new($cleanupZip.GetEntry('_files.json').Open(), [Text.Encoding]::UTF8)
            try { $checkedArchives[$cleanupRow.backup] = @($cleanupReader.ReadToEnd() | ConvertFrom-Json) } finally { $cleanupReader.Dispose() }
        } finally { $cleanupZip.Dispose() }
    }
    $cleanupItems = @(Get-Item -LiteralPath $resolvedCleanupPath; Get-ChildItem -LiteralPath $resolvedCleanupPath -Recurse -Force)
    if ($cleanupItems | Where-Object { $_.Attributes -band [IO.FileAttributes]::ReparsePoint }) { throw 'Reparse point requires a separate review' }
    $cleanupFiles = @($cleanupItems | Where-Object { -not $_.PSIsContainer })
    if ($cleanupFiles | Where-Object { $_.Name -in @('level.dat','instance.cfg') -or $_.Extension -in @('.jfr','.psd','.blend','.aup3') }) { throw 'Protected unique data found' }
    $cleanupRelativePath = [IO.Path]::GetRelativePath($resolvedCleanupRoot, $resolvedCleanupPath).Replace('\','/')
    $cleanupTracked = @(& git -C $resolvedCleanupRoot ls-files -- $cleanupRelativePath)
    if ($LASTEXITCODE -ne 0 -or $cleanupTracked.Count -gt 0) { throw 'Tracked files or Git verification failure' }
    $archivedCleanupFiles = @($checkedArchives[$cleanupRow.backup] | Where-Object { $_.entry.StartsWith($cleanupRow.archive_prefix) })
    if ($cleanupFiles.Count -ne $archivedCleanupFiles.Count) { throw 'Files changed since backup; no deletion allowed' }
    foreach ($savedCleanupFile in $archivedCleanupFiles) {
        if ((Get-FileHash -LiteralPath $savedCleanupFile.source -Algorithm SHA256).Hash -ne $savedCleanupFile.sha256) { throw "Changed file: $($savedCleanupFile.source)" }
    }
    if (-not $Execute) { Write-Output "VERIFIED, NOT REMOVED: $resolvedCleanupPath"; continue }
    if ($PSCmdlet.ShouldProcess($resolvedCleanupPath, 'Remove only this owner-approved backed-up generated directory')) {
        Remove-Item -LiteralPath $resolvedCleanupPath -Recurse -Force
    }
}
