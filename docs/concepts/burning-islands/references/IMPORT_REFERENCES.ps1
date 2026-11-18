# Slavic Myths 2.0.5 — Import original PNG sources from the ZIP attached to the ChatGPT conversation.
# This helper intentionally DOES NOT commit, push, overwrite, launch Minecraft, or modify main.
# Example (PowerShell inside repository root, on branch hell):
# powershell -ExecutionPolicy Bypass -File "docs/concepts/burning-islands/references/IMPORT_REFERENCES.ps1" -ZipPath "C:\Users\user\Downloads\BURNING_ISLANDS_205_COMPLETE_REFERENCES_FOR_GITHUB.zip"

param(
 [Parameter(Mandatory=$true)][string]$ZipPath,
 [string]$RepoRoot = "."
)
$ErrorActionPreference = "Stop"
$repo = (Resolve-Path -LiteralPath $RepoRoot).Path
$zip = (Resolve-Path -LiteralPath $ZipPath).Path
$branch = (& git -C $repo branch --show-current).Trim()
if ($LASTEXITCODE -ne 0 -or $branch -ne "hell") { throw "Expected Git branch 'hell', got '$branch'; refusing to modify other branch." }
$expectedArchive = "22256bf4df4f8c8abf6c5aec2ec6a9d30c3be02b0a3d7b59c5751908bc6edada"
$actualArchive = (Get-FileHash -LiteralPath $zip -Algorithm SHA256).Hash.ToLowerInvariant()
if ($actualArchive -ne $expectedArchive) { throw "ZIP SHA256 mismatch: $actualArchive"; }
$expected = [ordered]@{
 "BURNING_ISLANDS_205_BIOME_CONCEPT.png" = "a770acc9cb8401baa8199994f73cf28ee1fb3d43eff25fe0fd5bdbff92fbf605"
 "BURNING_ISLANDS_205_CREATURES_CONCEPT.png" = "a06f8d7730e43f93003a0a9c465c806e6e28a41260e7a61640d1f336924651d1"
 "BURNING_ISLANDS_205_RUNE_MATERIALS_CONCEPT.png" = "47a9f11cd0e3c98e48983cf28c8b5ed7c6687689d9abb9cc5ee28edd7a47fdab"
 "BURNING_ISLANDS_205_BOSS_CONCEPT.png" = "354c2c318c01ee67b21b60adc653a0a4747acc81786a433cb28e1c3fc26881a6"
 "BURNING_ISLANDS_205_MAGMA_LORD_HEAD_PLACED_INVENTORY.png" = "287ce3f15e28b1a646cf31de1e51fab85519bb03dc935d83bb7aea920395bc93"
}
$sub = "docs/concepts/burning-islands/references"
$tmp = Join-Path ([IO.Path]::GetTempPath()) ("slavic-205-art-" + [Guid]::NewGuid().ToString("N"))
try {
  New-Item -ItemType Directory -Path $tmp -Force | Out-Null
  Expand-Archive -LiteralPath $zip -DestinationPath $tmp
  foreach ($name in $expected.Keys) {
    $relative = "$sub/$name"
    $src = Join-Path $tmp $relative
    $dst = Join-Path $repo $relative
    if (-not (Test-Path -LiteralPath $src)) { throw "Missing source in ZIP: $relative" }
    $hash = (Get-FileHash -LiteralPath $src -Algorithm SHA256).Hash.ToLowerInvariant()
    if ($hash -ne $expected[$name]) { throw "File SHA256 mismatch: $relative"; }
    if (Test-Path -LiteralPath $dst) {
      $existing = (Get-FileHash -LiteralPath $dst -Algorithm SHA256).Hash.ToLowerInvariant()
      if ($existing -ne $hash) { throw "Existing destination differs; refusing to overwrite: $relative"; }
    }
  }
  foreach ($name in $expected.Keys) {
    $relative = "$sub/$name"
    $src = Join-Path $tmp $relative
    $dst = Join-Path $repo $relative
    if (-not (Test-Path -LiteralPath $dst)) {
      $dir = Split-Path -Parent $dst
      New-Item -ItemType Directory -Path $dir -Force | Out-Null
      Copy-Item -LiteralPath $src -Destination $dst
      Write-Host "Imported $relative"
    } else { Write-Host "Verified existing $relative" }
  }
  Write-Host "Original references verified in working tree on branch hell."
  Write-Host "Review: git status --short; commit/push only with owner approval and an exclusive writer."
} finally {
  if (Test-Path -LiteralPath $tmp) { Remove-Item -LiteralPath $tmp -Recurse -Force }
}
