param(
 [ValidatePattern('^[a-z0-9-]+$')][string]$Name,
 [long]$Seed=30001,
 [ValidateSet('census','yaga-flat','yaga-slope','yaga-cliff','yaga-forest','yaga-restart','baseline','story','legacy','survey')][string]$Mode='census',
 [switch]$Resume,[switch]$SkipBuild
)
$ErrorActionPreference='Stop'
$qaRoot=[IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$qaWork=[IO.Path]::GetFullPath((Join-Path $qaRoot 'work'))
$qaDirectory=[IO.Path]::GetFullPath((Join-Path $qaWork ('issue300-'+$Name)))
if(-not $Name -or -not $qaDirectory.StartsWith($qaWork+[IO.Path]::DirectorySeparatorChar,[StringComparison]::OrdinalIgnoreCase)){throw 'Invalid isolated world path'}
$qaGames=Get-CimInstance Win32_Process | Where-Object { $_.Name -match '^javaw?\.exe$' -and $_.CommandLine -match 'net\.neoforged\.devlaunch\.Main|org\.multimc\.EntryPoint|cpw\.mods\.bootstraplauncher' }
if($qaGames){throw 'A Minecraft process already exists; no second process will be started'}
$qaMarker=Join-Path $qaDirectory '.issue300-owned'
if(Test-Path -LiteralPath $qaDirectory){if(-not $Resume -or -not (Test-Path -LiteralPath $qaMarker)){throw 'Existing world requires Resume and its ownership marker'}}
else{
 New-Item -ItemType Directory -Path $qaDirectory | Out-Null
 Set-Content -LiteralPath $qaMarker 'Disposable #300 world, no user saves or PolyMC.'
 Set-Content -LiteralPath (Join-Path $qaDirectory 'eula.txt') 'eula=true'
 Set-Content -LiteralPath (Join-Path $qaDirectory 'server.properties') "level-seed=$Seed`nonline-mode=false`nserver-ip=127.0.0.1`nserver-port=0`nview-distance=2`nsimulation-distance=2`ndifficulty=normal`nmax-tick-time=60000`n"
}
$qaPreviousJava=$env:JAVA_HOME
Push-Location $qaRoot
try{
 $env:JAVA_HOME='C:/Program Files/Java/jdk-21.0.12'
 if(-not $SkipBuild){& ./gradlew.bat clean build --max-workers=1 '-Dorg.gradle.jvmargs=-Xmx1536M -XX:ActiveProcessorCount=2' *> (Join-Path $qaDirectory ('build-'+$Mode+'.log'));if($LASTEXITCODE -ne 0){throw 'Build failed'}}
 & python -X utf8 tools/verify_current_resources.py *> (Join-Path $qaDirectory ('resources-'+$Mode+'.log'))
 if($LASTEXITCODE -ne 0){throw 'Resource/JAR gate failed'}
 Get-ChildItem -LiteralPath build/libs -Filter '*.jar' | Get-FileHash -Algorithm SHA256 | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $qaDirectory ('jar-'+$Mode+'.json'))
 & ./gradlew.bat -I tools/worldgen300.gradle runWorldgen300Server "-Pqa300Run=$Name" "-Pqa300Mode=$Mode" --max-workers=1 '-Dorg.gradle.jvmargs=-Xmx1024M -XX:ActiveProcessorCount=2' *> (Join-Path $qaDirectory ('runtime-'+$Mode+'.log'))
 if($LASTEXITCODE -ne 0){throw 'Runtime failed; inspect isolated log'}
 $qaResult=Join-Path $qaDirectory $(if($Mode -eq 'census'){'census300.json'}elseif($Mode.StartsWith('yaga')){'yaga300-'+$Mode+'.json'}elseif($Mode -eq 'story'){'story300.json'}elseif($Mode -eq 'legacy'){'legacy300.json'}elseif($Mode -eq 'survey'){'survey300.json'}else{'worldgen300.json'})
 if(-not (Test-Path -LiteralPath $qaResult)){throw 'Server produced no receipt'}
 $qaReceipt=Get-Content -LiteralPath $qaResult -Raw | ConvertFrom-Json
 if($qaReceipt.error){throw ('Native scenario failed: '+$qaReceipt.error)}
 if($Mode -eq 'census' -and @($qaReceipt.samples | Where-Object {$_.status -notlike 'PASS*'}).Count -gt 0){throw 'Census has FAIL/UNKNOWN samples'}
 if($Mode -eq 'story' -and $qaReceipt.status -ne 'PASS_STORY_SERVICE_ONLY'){throw 'Story placement service failed'}
 if($Mode.StartsWith('yaga') -and $qaReceipt.status -ne 'PASS_SCOPED_FIXTURE'){throw 'Yaga scenario has failed checks'}
 Write-Output $qaResult
}finally{$env:JAVA_HOME=$qaPreviousJava;Pop-Location}
