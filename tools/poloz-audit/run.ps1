
$ErrorActionPreference='Stop'
$root=Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
Set-Location -LiteralPath $root
$state=Join-Path $env:TEMP 'slavicmyths-performance-last-launch.txt'
$gameDir=Join-Path $root 'work/poloz-audit-client'
New-Item -ItemType Directory -Force $gameDir | Out-Null
$options=Join-Path $gameDir 'options.txt'
if(!(Test-Path -LiteralPath $options)){@('onboardAccessibility:false','tutorialStep:none','pauseOnLostFocus:false') | Set-Content -LiteralPath $options -Encoding utf8}

$processes=Get-CimInstance Win32_Process | Where-Object {$_.Name -in @('java.exe','javaw.exe') -and $_.CommandLine -match 'BootstrapLauncher|devlaunch|net.minecraft.client.main.Main|net.minecraft.server.Main'}
if($processes){throw 'Another Minecraft is running. Wait for it to close.'}
if(Test-Path -LiteralPath $state){$previous=[DateTimeOffset]::Parse((Get-Content -LiteralPath $state -Raw));if(([DateTimeOffset]::Now-$previous).TotalMinutes -lt 15){throw 'Minecraft launch interval must be >=15 minutes.'}}
$out="docs/implementation/poloz-pressure/native"
New-Item -ItemType Directory -Force $out | Out-Null
[DateTimeOffset]::Now.ToString('o') | Set-Content -LiteralPath $state
Copy-Item -LiteralPath $state -Destination "$out/launch.txt"
$env:JAVA_HOME='C:/Program Files/Java/jdk-21.0.12'
& ./gradlew.bat runClient -I tools/poloz-audit/init.gradle *> "$out/client.log"
exit $LASTEXITCODE
