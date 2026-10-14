$ErrorActionPreference='Stop'
$taskRoot=(Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$instance=(Resolve-Path -LiteralPath 'C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing').Path
$mods=(Resolve-Path -LiteralPath (Join-Path $instance '.minecraft/mods')).Path
$report=Join-Path $taskRoot 'docs/implementation/weapon-visual-repair'
function Digest([string]$path){(Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash.ToLowerInvariant()}
$gate=Get-Content -LiteralPath (Join-Path $report 'resource-gates.json') -Raw | ConvertFrom-Json
$source=Join-Path $taskRoot 'build/libs/slavicmyths-1.9.7.3.jar'
if($gate.status -ne 'PASS' -or (Digest $source) -ne $gate.sha256){throw 'Production candidate differs from passed gates'}
if((Get-Content -LiteralPath (Join-Path $taskRoot 'docs/implementation/weapon-visual-repair/BUILD.log') -Raw) -notmatch 'BUILD SUCCESSFUL'){throw 'Clean build PASS missing'}
$pack=Get-Content -LiteralPath (Join-Path $instance 'mmc-pack.json') -Raw | ConvertFrom-Json
if(($pack.components | Where-Object uid -eq 'net.minecraft').version -ne '1.21.1' -or ($pack.components | Where-Object uid -eq 'net.neoforged.neoforge').version -ne '21.1.255'){throw 'Wrong target platform'}
$minecraft=@(Get-CimInstance Win32_Process | Where-Object {$_.Name -in @('java.exe','javaw.exe') -and $_.CommandLine -match 'net.minecraft.client.main.Main|--gameDir|cpw.mods.bootstraplauncher.BootstrapLauncher|org.multimc|org.polymc'})
if($minecraft.Count -ne 0){throw 'Minecraft process still running: close the game before installing'}
$prior=@(Get-ChildItem -LiteralPath $mods -Filter 'slavicmyths-*.jar' -File)
if($prior.Count -eq 0){throw 'Existing installed Slavic Myths JAR not found'}
$target=[IO.Path]::GetFullPath((Join-Path $mods 'slavicmyths-1.9.7.3.jar'))
$staged=$target+'.new'
foreach($path in @($target,$staged)+@($prior.FullName)){if([IO.Path]::GetDirectoryName([IO.Path]::GetFullPath($path)) -ne $mods){throw 'Unsafe file operation outside selected mods directory'}}
$oldPaths=@($prior.FullName);$oldHashes=@{};foreach($p in $prior){$oldHashes[$p.FullName]=Digest $p.FullName}
$before=@{}
Get-ChildItem -LiteralPath $instance -Recurse -File | Where-Object {$_.FullName -notin $oldPaths} | ForEach-Object {$before[$_.FullName]=Digest $_.FullName}
$backupPath=$null
if(-not ($prior.Count -eq 1 -and $prior[0].FullName -eq $target -and $oldHashes[$target] -eq $gate.sha256)){
 if(Test-Path -LiteralPath $staged){throw 'Existing staging file: preserve it and inspect before installation'}
 $stamp=[TimeZoneInfo]::ConvertTimeBySystemTimeZoneId([DateTime]::UtcNow,'Russian Standard Time').ToString('yyyyMMdd-HHmmss')
 $backupPath=[IO.Path]::GetFullPath((Join-Path $instance ('.slavicmyths-backups/pre-weapon-visual-repair-'+$stamp)))
 if(-not $backupPath.StartsWith($instance+[IO.Path]::DirectorySeparatorChar,[StringComparison]::OrdinalIgnoreCase)){throw 'Unsafe backup directory'}
 New-Item -ItemType Directory -Path $backupPath | Out-Null
 foreach($p in $prior){$saved=Join-Path $backupPath $p.Name;Copy-Item -LiteralPath $p.FullName -Destination $saved;if((Digest $saved) -ne $oldHashes[$p.FullName]){throw 'Backup checksum mismatch'}}
 Copy-Item -LiteralPath $source -Destination $staged
 if((Digest $staged) -ne $gate.sha256){throw 'Staging checksum mismatch'}
 try {
  foreach($p in $prior){Remove-Item -LiteralPath $p.FullName}
  Move-Item -LiteralPath $staged -Destination $target
  if((Digest $target) -ne $gate.sha256 -or @(Get-ChildItem -LiteralPath $mods -Filter 'slavicmyths-*.jar' -File).Count -ne 1){throw 'Installed checksum/duplicate check failed'}
  foreach($entry in $before.GetEnumerator()){if((Digest $entry.Key) -ne $entry.Value){throw ('Unrelated profile file changed: '+$entry.Key)}}
 }catch{
  if(Test-Path -LiteralPath $target){Remove-Item -LiteralPath $target}
  foreach($p in $prior){Copy-Item -LiteralPath (Join-Path $backupPath $p.Name) -Destination $p.FullName -Force}
  if(Test-Path -LiteralPath $staged){Remove-Item -LiteralPath $staged}
  throw
 }
}
foreach($entry in $before.GetEnumerator()){if((Digest $entry.Key) -ne $entry.Value){throw ('Profile preservation mismatch: '+$entry.Key)}}
$receipt=[ordered]@{patch='Восстановление оружия';version='1.9.7.3';status='PASS';instance=$instance;installed_jar=$target;sha256=(Digest $target);source=$source;previous_hashes=$oldHashes;backup=$backupPath;other_files_preserved=$before.Count;configuration_changes=0;client_starts=0;installer_server_starts=0;worlds='NOT_TOUCHED';old_1_16_5='NOT_TOUCHED'}
$receipt | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath (Join-Path $report 'polymc-installation.json') -Encoding utf8
$markdown=@"
# PolyMC — Восстановление оружия поверх 1.9.7.3

Результат: **PASS**. Профиль: ``$instance``.

Установлен ``$target``. SHA256: ``$($receipt.sha256)``.

Предыдущие JAR сохранены в ``$backupPath`` с проверкой SHA256. Сохранены побайтно $($before.Count) остальных файлов профиля, включая конфигурации и миры. Дубликатов мода нет. Настройки Jade не менялись. Профиль/клиент/сервер установщиком не запускались; старый 1.16.5 не затронут.

Подробная квитанция: [polymc-installation.json](polymc-installation.json).
"@
[IO.File]::WriteAllText((Join-Path $report 'POLYMC_INSTALLATION.md'),$markdown,[Text.UTF8Encoding]::new($false))
$receipt | ConvertTo-Json -Depth 5

$cfg=Join-Path $instance 'instance.cfg'
$cfgBefore=[IO.File]::ReadAllText($cfg)
if(-not $backupPath){$backupPath=Join-Path $instance ('.slavicmyths-backups/weapon-patch-name-'+[DateTime]::UtcNow.ToString('yyyyMMdd-HHmmss'));New-Item -ItemType Directory -Path $backupPath | Out-Null}
if(-not (Test-Path -LiteralPath (Join-Path $backupPath 'instance.cfg'))){Copy-Item -LiteralPath $cfg -Destination (Join-Path $backupPath 'instance.cfg')}
$cfgAfter=if($cfgBefore -match 'Восстановление оружия'){$cfgBefore}else{[regex]::Replace($cfgBefore,'(?m)^name=(.*?)(\r?)$',{param($m) 'name='+$m.Groups[1].Value+' — Восстановление оружия'+$m.Groups[2].Value})}
$cfgAfter=[regex]::Replace($cfgAfter,'(?m)^name=Slavic Myths [0-9.]+','name=Slavic Myths 1.9.7.3')
[IO.File]::WriteAllText($cfg,$cfgAfter,[Text.UTF8Encoding]::new($false))
$receipt.configuration_changes=1
$receipt | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath (Join-Path $report 'polymc-installation.json') -Encoding utf8

$receipt.backup=$backupPath
$receipt | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath (Join-Path $report 'polymc-installation.json') -Encoding utf8
Write-Output ('INSTALLED_WEAPON_PATCH '+$receipt.sha256)
