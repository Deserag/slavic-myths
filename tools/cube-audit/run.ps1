param([ValidateSet('before','after','crossover')][string]$Phase='before')
$ErrorActionPreference='Stop'
$root=Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
Set-Location -LiteralPath $root
$state=Join-Path $env:TEMP 'slavicmyths-performance-last-launch.txt'
$gameDir=Join-Path $root 'work/cube-audit-client'
New-Item -ItemType Directory -Force $gameDir | Out-Null
$options=Join-Path $gameDir 'options.txt'
if(!(Test-Path -LiteralPath $options)){@('onboardAccessibility:false','tutorialStep:none','pauseOnLostFocus:false') | Set-Content -LiteralPath $options -Encoding utf8}

$processes=Get-CimInstance Win32_Process | Where-Object {$_.Name -in @('java.exe','javaw.exe') -and $_.CommandLine -match 'BootstrapLauncher|devlaunch|net.minecraft.client.main.Main|net.minecraft.server.Main'}
if($processes){throw 'Another Minecraft is running. Wait for it to close.'}
if(Test-Path -LiteralPath $state){$previous=[DateTimeOffset]::Parse((Get-Content -LiteralPath $state -Raw));if(([DateTimeOffset]::Now-$previous).TotalMinutes -lt 15){throw 'Minecraft launch interval must be >=15 minutes.'}}
$out="docs/implementation/cube-allocation/$Phase"
New-Item -ItemType Directory -Force $out | Out-Null
[DateTimeOffset]::Now.ToString('o') | Set-Content -LiteralPath $state
Copy-Item -LiteralPath $state -Destination "$out/launch.txt"
$env:JAVA_HOME='C:/Program Files/Java/jdk-21.0.12'
$params=@('runClient','-I','tools/cube-audit/init.gradle',"-PcubeOutput=$out");if($Phase -eq 'crossover'){$params+='-PcubeCrossover'}
& ./gradlew.bat @params *> "$out/client.log"
exit $LASTEXITCODE
