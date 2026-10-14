$ErrorActionPreference='Stop'
$taskRoot=(Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$instance=(Resolve-Path -LiteralPath 'C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing').Path
$mods=(Resolve-Path -LiteralPath (Join-Path $instance '.minecraft/mods')).Path
$report=Join-Path $taskRoot 'docs/implementation/reward-polymc-update'
function Digest([string]$path){(Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash.ToLowerInvariant()}
$gate=Get-Content -LiteralPath (Join-Path $report 'RESOURCE_JAR_CHECKS.json') -Raw | ConvertFrom-Json
$source=[IO.Path]::GetFullPath($gate.jar)
if($gate.status -ne 'PASS' -or (Digest $source) -ne $gate.sha256){throw 'Production artifact does not match passed gates'}
$expectedSource=[IO.Path]::GetFullPath((Join-Path $taskRoot ('build/libs/slavicmyths-'+$gate.version+'.jar')))
if($source -ne $expectedSource){throw 'Unexpected production artifact path'}
$version=(Select-String -LiteralPath (Join-Path $taskRoot 'gradle.properties') -Pattern '^mod_version=(.+)$').Matches.Groups[1].Value
if($version -ne $gate.version){throw 'Version changed since gates'}
if((Get-Content -LiteralPath (Join-Path $taskRoot 'docs/implementation/reward-polymc-update/FINAL_BUILD_RUNTIME.log') -Raw) -notmatch 'BUILD SUCCESSFUL'){throw 'Clean build gate missing'}
$pack=Get-Content -LiteralPath (Join-Path $instance 'mmc-pack.json') -Raw | ConvertFrom-Json
if(($pack.components | Where-Object uid -eq 'net.minecraft').version -ne '1.21.1' -or ($pack.components | Where-Object uid -eq 'net.neoforged.neoforge').version -ne '21.1.255'){throw 'Wrong target platform'}
$game=@(Get-CimInstance Win32_Process | Where-Object {$_.Name -in @('java.exe','javaw.exe') -and $_.CommandLine -match 'net.minecraft.client.main.Main|--gameDir|cpw.mods.bootstraplauncher.BootstrapLauncher|org.multimc|org.polymc'})
if($game.Count){throw 'Minecraft is already running; installation aborted'}
$prior=@(Get-ChildItem -LiteralPath $mods -Filter 'slavicmyths-*.jar' -File)
if(-not $prior.Count){throw 'Expected existing mod installation not found'}
$target=[IO.Path]::GetFullPath((Join-Path $mods ('slavicmyths-'+$gate.version+'.jar')))
$staged=$target+'.new'
foreach($path in @($target,$staged)+@($prior.FullName)){
 if([IO.Path]::GetDirectoryName([IO.Path]::GetFullPath($path)) -ne $mods){throw 'Unsafe file operation outside selected mods folder'}
}
if(Test-Path -LiteralPath $staged){throw 'Existing staging file must be preserved'}
$before=@{};$previous=@{}
$cfg=Join-Path $instance 'instance.cfg'
$cfgBefore=[IO.File]::ReadAllText($cfg)
foreach($p in $prior){$previous[$p.FullName]=Digest $p.FullName}
Get-ChildItem -LiteralPath $instance -Recurse -File | Where-Object {$_.FullName -notin $prior.FullName} | ForEach-Object {$before[$_.FullName]=Digest $_.FullName}
$stamp=[TimeZoneInfo]::ConvertTimeBySystemTimeZoneId([DateTime]::UtcNow,'Russian Standard Time').ToString('yyyyMMdd-HHmmss')
$backup=[IO.Path]::GetFullPath((Join-Path $instance ('.slavicmyths-backups/pre-reward-polymc-update-'+$stamp)))
if(-not $backup.StartsWith($instance+[IO.Path]::DirectorySeparatorChar,[StringComparison]::OrdinalIgnoreCase)){throw 'Unsafe backup path'}
New-Item -ItemType Directory -Path $backup | Out-Null
Copy-Item -LiteralPath $cfg -Destination (Join-Path $backup 'instance.cfg')
foreach($p in $prior){$copy=Join-Path $backup $p.Name;Copy-Item -LiteralPath $p.FullName -Destination $copy;if((Digest $copy) -ne $previous[$p.FullName]){throw 'Backup checksum failed'}}
Copy-Item -LiteralPath $source -Destination $staged
if((Digest $staged) -ne $gate.sha256){throw 'Staging checksum failed'}
$mutated=$false
try{
 foreach($p in $prior){if((Digest $p.FullName) -ne $previous[$p.FullName]){throw 'Installed JAR changed concurrently'}}
 if([IO.File]::ReadAllText($cfg) -ne $cfgBefore){throw 'Instance name changed concurrently'}
 $mutated=$true
 foreach($p in $prior){Remove-Item -LiteralPath $p.FullName}
 Move-Item -LiteralPath $staged -Destination $target
 if((Digest $target) -ne $gate.sha256 -or @(Get-ChildItem -LiteralPath $mods -Filter 'slavicmyths-*.jar' -File).Count -ne 1){throw 'Installed checksum/duplicate check failed'}
 foreach($entry in $before.GetEnumerator()){if((Digest $entry.Key) -ne $entry.Value){throw ('Unrelated profile file changed: '+$entry.Key)}}
 $cfgAfter=[regex]::Replace($cfgBefore,'(?m)^name=Slavic Myths [0-9]+(?:\.[0-9]+)+',('name=Slavic Myths '+$gate.version))
 if($cfgAfter -eq $cfgBefore -and $cfgBefore -notmatch ('(?m)^name=Slavic Myths '+[regex]::Escape($gate.version)+' ')){throw 'Unexpected profile name format'}
 if($cfgAfter -notmatch 'Исправления урона и навыков'){$cfgAfter=[regex]::Replace($cfgAfter,'(?m)^name=(.*?)(\r?)$',{param($m)'name='+$m.Groups[1].Value+' — Исправления урона и навыков'+$m.Groups[2].Value})}
 [IO.File]::WriteAllText($cfg,$cfgAfter,[Text.UTF8Encoding]::new($false))
 foreach($entry in $before.GetEnumerator()){if($entry.Key -ne $cfg -and (Digest $entry.Key) -ne $entry.Value){throw ('Unrelated profile file changed after name update: '+$entry.Key)}}
}catch{
 if($mutated){
  if(Test-Path -LiteralPath $target){Remove-Item -LiteralPath $target}
  foreach($p in $prior){Copy-Item -LiteralPath (Join-Path $backup $p.Name) -Destination $p.FullName -Force}
  if([IO.File]::ReadAllText($cfg) -ne $cfgBefore){Copy-Item -LiteralPath (Join-Path $backup 'instance.cfg') -Destination $cfg -Force}
 }
 if(Test-Path -LiteralPath $staged){Remove-Item -LiteralPath $staged}
 throw
}
$cfgChanged=[int]($cfgAfter -ne $cfgBefore)
$receipt=[ordered]@{status='PASS';version=$gate.version;installed_jar=$target;sha256=(Digest $target);backup=$backup;previous_hashes=$previous;other_files_preserved=$before.Count-$cfgChanged;configuration_changes=$cfgChanged;name_before=([regex]::Match($cfgBefore,'(?m)^name=(.*)$').Groups[1].Value.Trim());name_after=([regex]::Match($cfgAfter,'(?m)^name=(.*)$').Groups[1].Value.Trim());client_starts=0;server_starts=0;worlds='NOT_TOUCHED';old_1_16_5='NOT_TOUCHED'}
$receipt | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath (Join-Path $report 'POLYMC_INSTALLATION.json') -Encoding utf8
$receipt | ConvertTo-Json -Depth 5
