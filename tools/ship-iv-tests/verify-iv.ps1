$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$out = Join-Path $root 'build\iv-geometry-check'
New-Item -ItemType Directory -Force -Path $out | Out-Null
$ship = Join-Path $root 'src\main\java\org\slavicmyths\ships'
$files = @(
    (Join-Path $ship 'ShipSectionPlan.java'),
    (Join-Path $ship 'ShipSuperstructurePlan.java'),
    (Join-Path $ship 'ShipHullVoxelLayout.java'),
    (Join-Path $ship 'ShipRigidMotion.java'),
    (Join-Path $root 'tools\ship-iv-tests\ShipIVRegression.java')
)
& javac -encoding UTF-8 -d $out @files
if ($LASTEXITCODE -ne 0) { throw 'Java 21 IV geometry compile failed' }
& java -cp $out ShipIVRegression
if ($LASTEXITCODE -ne 0) { throw 'IV geometry/movement regression failed' }
Write-Host 'IV Java regression suite passed' -ForegroundColor Green
