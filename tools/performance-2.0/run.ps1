param([Parameter(Mandatory=$true)][string]$Label,[ValidateSet('cube','world','city')][string]$Suite='cube',[switch]$Control,[string]$EvidenceRoot,[switch]$SkipInterval,[string]$OpenWorld,[switch]$BossesOnly)
$ErrorActionPreference='Stop'
if($Control -and $Suite -ne 'world'){throw 'Control mode is available only for the common world suite.'}
if($OpenWorld -and ($Suite -ne 'city' -or $OpenWorld -notmatch '^[A-Za-z0-9_-]+$')){throw 'OpenWorld must be the name of an isolated copied city-suite save.'}
if($BossesOnly -and ($Suite -ne 'city' -or $OpenWorld)){throw 'BossesOnly requires its own fresh flat-world follow-up.'}
$root=Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
Set-Location -LiteralPath $root
$state=Join-Path $env:TEMP 'slavicmyths-performance-last-launch.txt'
$processes=Get-CimInstance Win32_Process | Where-Object {$_.Name -in @('java.exe','javaw.exe') -and $_.CommandLine -match 'BootstrapLauncher|devlaunch|net.minecraft.client.main.Main|net.minecraft.server.Main'}
if($processes){throw 'Another Minecraft is running. Wait for it to close.'}
if(!$SkipInterval -and (Test-Path -LiteralPath $state)){$previous=[DateTimeOffset]::Parse((Get-Content -LiteralPath $state -Raw));if(([DateTimeOffset]::Now-$previous).TotalMinutes -lt 15.5){throw 'Wait >=15.5min since prior launch (30s JVM startup buffer).'}}
if(!$EvidenceRoot){$EvidenceRoot=Join-Path $root 'docs/implementation/performance-2.0'}
$out=Join-Path $EvidenceRoot $Label
if(Test-Path -LiteralPath $out){throw 'Existing run directory; use a new label.'}
New-Item -ItemType Directory -Force $out | Out-Null
$folder=if($Control){'control'}elseif($BossesOnly){'bosses-flat'}else{$Suite}
$gameDir=Join-Path $root "work/performance-2-$folder"
New-Item -ItemType Directory -Force $gameDir | Out-Null
$options=Join-Path $gameDir 'options.txt'
if(!(Test-Path -LiteralPath $options)){@('onboardAccessibility:false','tutorialStep:none','pauseOnLostFocus:false') | Set-Content -LiteralPath $options -Encoding utf8}
$env:JAVA_HOME='C:/Program Files/Java/jdk-21.0.12'
[DateTimeOffset]::Now.ToString('o') | Set-Content -LiteralPath $state
Copy-Item -LiteralPath $state -Destination "$out/launch.txt"
@{interval_bypassed=[bool]$SkipInterval;authorization=if($SkipInterval){'User explicitly waived launch intervals on 2026-10-17 to accelerate this audit.'}else{'Default >=15.5min gate.'}} | ConvertTo-Json | Set-Content "$out/launch-policy.json"
@{suite=$Suite;control=[bool]$Control;bosses_only=[bool]$BossesOnly;copied_world=$OpenWorld} | ConvertTo-Json | Set-Content "$out/launch-mode.json"
Get-FileHash 'src/main/java/org/slavicmyths/client/GuardianClient.java' | Select-Object Hash | ConvertTo-Json | Set-Content "$out/guardian-source.json"
git rev-parse HEAD | Set-Content "$out/head.txt"
$probeFolders=@("tools/performance-2.0/$Suite");if($Suite -eq 'city'){$probeFolders+='tools/performance-2.0/world'}
Get-ChildItem $probeFolders -Recurse -File | Get-FileHash | Select-Object @{Name='File';Expression={$_.Path.Substring($root.Length+1)}},Hash | ConvertTo-Json | Set-Content "$out/probe-sources.json"
$runArgs=@('runClient','-I',"tools/performance-2.0/$Suite/init.gradle","-PperfOutput=$out");if($Control){$runArgs+='-PperfVanilla'}
if($OpenWorld){$runArgs+="-PperfOpenWorld=$OpenWorld"}
if($BossesOnly){$runArgs+='-PperfBossesOnly'}
& ./gradlew.bat @runArgs *> "$out/client.log"
exit $LASTEXITCODE
