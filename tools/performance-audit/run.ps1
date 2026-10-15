param([switch]$Extended,[switch]$Focused,[ValidateSet('extended','extended2','focused')][string]$Label='extended')
# Opt-in launcher. This file alone never starts Minecraft; invoke explicitly.
$ErrorActionPreference='Stop'
$root=Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
Set-Location -LiteralPath $root
$state=Join-Path $env:TEMP 'slavicmyths-performance-last-launch.txt'
if(Test-Path -LiteralPath $state){$previous=[DateTimeOffset]::Parse((Get-Content -LiteralPath $state -Raw));if(([DateTimeOffset]::Now-$previous).TotalMinutes -lt 15){throw 'Minecraft launch interval must be at least 15 minutes.'}}
$running=Get-CimInstance Win32_Process | Where-Object {$_.Name -in @('java.exe','javaw.exe') -and $_.CommandLine -match 'BootstrapLauncher|net.minecraft.client.main.Main|net.minecraft.server.Main'}
if($running){throw 'Another Minecraft process exists: stop the benchmark to avoid interference.'}
$output=if($Extended -or $Focused){"docs/implementation/performance-audit/$Label/raw"}else{'docs/implementation/performance-audit/raw'}
New-Item -ItemType Directory -Force $output | Out-Null
[DateTimeOffset]::Now.ToString('o') | Set-Content -LiteralPath $state
Copy-Item -LiteralPath $state -Destination (Join-Path $output 'launch.txt')
$env:JAVA_HOME='C:/Program Files/Java/jdk-21.0.12'
$params=@('runClient','-I','tools/performance-audit/init.gradle');if($Extended){$params+='-PauditExtended'};if($Focused){$params+='-PauditFocused'};if($Extended -or $Focused){$params+=('-PauditOutput='+$output)}
& ./gradlew.bat @params *> (Join-Path $output 'client.log')
exit $LASTEXITCODE
