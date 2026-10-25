param(
 [ValidateSet('city','medium','hills','flat','cancel','cancelpartial','protection','waterlogged','terrain','snapshot','reload','partialreload','bayunreload')][string]$Scenario='city',
 [long]$Seed=29501,
 [ValidatePattern('^[a-z0-9-]+$')][string]$Name=('local-'+(Get-Date -Format 'yyyyMMdd-HHmmss')),
 [int]$X=0,
 [switch]$Resume
)
$ErrorActionPreference='Stop'
$qaRoot=[IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$qaWork=[IO.Path]::GetFullPath((Join-Path $qaRoot 'work'))
$qaDirectory=[IO.Path]::GetFullPath((Join-Path $qaWork ('qa295-'+$Name)))
if(-not $qaDirectory.StartsWith($qaWork+[IO.Path]::DirectorySeparatorChar,[StringComparison]::OrdinalIgnoreCase)){throw 'Test path escaped workspace'}
$qaMarker=Join-Path $qaDirectory '.generation295-owned'
if(Test-Path -LiteralPath $qaDirectory){
 if(-not $Resume -or -not (Test-Path -LiteralPath $qaMarker)){throw 'Existing directory is not an explicitly resumed owned test world'}
}else{
 New-Item -ItemType Directory -Path $qaDirectory | Out-Null
 Set-Content -LiteralPath $qaMarker -Value 'Disposable generation295 world; no user saves or PolyMC.' -Encoding utf8
 Set-Content -LiteralPath (Join-Path $qaDirectory 'eula.txt') -Value 'eula=true' -Encoding ascii
 $qaProperties="level-seed=$Seed`nonline-mode=false`nserver-ip=127.0.0.1`nserver-port=0`nview-distance=2`nsimulation-distance=2`nmax-tick-time=60000`n"
 if($Scenario -eq 'flat'){$qaProperties+='level-type=minecraft:flat'+"`n"+'generator-settings={"layers":[{"block":"minecraft:bedrock","height":1},{"block":"minecraft:dirt","height":2},{"block":"minecraft:grass_block","height":1}],"biome":"minecraft:plains"}'+"`n"}
 Set-Content -LiteralPath (Join-Path $qaDirectory 'server.properties') -Value $qaProperties -Encoding ascii
}
$qaResult=Join-Path $qaDirectory 'generation295.json'
if(Test-Path -LiteralPath $qaResult){Remove-Item -LiteralPath $qaResult}
$qaPreviousJavaHome=$env:JAVA_HOME
Push-Location $qaRoot
try{
 $env:JAVA_HOME='C:/Program Files/Java/jdk-21.0.12'
 & ./gradlew.bat -I tools/generation295-runtime.gradle runGeneration295 "-PqaRun=$Name" "-PqaScenario=$Scenario" "-PqaX=$X"
 if($LASTEXITCODE -ne 0){throw "Gradle failed: $LASTEXITCODE"}
 if(-not (Test-Path -LiteralPath $qaResult)){throw 'Native server produced no result; Gradle success is insufficient'}
 $qaMetrics=Get-Content -LiteralPath $qaResult -Raw | ConvertFrom-Json
 if($qaMetrics.result -ne 'PASS'){throw ('Native scenario failed: '+($qaMetrics | ConvertTo-Json -Compress))}
 $qaMetrics | ConvertTo-Json -Depth 8
}finally{$env:JAVA_HOME=$qaPreviousJavaHome;Pop-Location}
