throw 'ARCHIVED historical tool. See docs/maintenance/issue-43/HISTORICAL_ARTIFACTS.md; restore its original revision and verified inputs in a separate historical checkout. Current source is not its baseline.'

$ErrorActionPreference='Stop'
$taskRoot=(Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$instance=(Resolve-Path -LiteralPath 'C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing').Path
$mods=(Resolve-Path -LiteralPath (Join-Path $instance '.minecraft/mods')).Path
function Digest([string]$path) { (Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash.ToLowerInvariant() }
$gate=Get-Content -LiteralPath (Join-Path $taskRoot 'docs/verification/feedback-1.7.9/resource-gates.json') -Raw | ConvertFrom-Json
$source=Join-Path $taskRoot 'build/libs/slavicmyths-1.7.9.jar'
if ($gate.status -ne 'PASS' -or (Digest $source) -ne $gate.sha256) { throw 'Candidate differs from passed production gates' }
if ((Get-Content -LiteralPath (Join-Path $taskRoot 'work/feedback-1.7.9-build.log') -Raw) -notmatch 'BUILD SUCCESSFUL') { throw 'Clean build missing' }
$running=@(Get-CimInstance Win32_Process -Filter "Name='java.exe' OR Name='javaw.exe'" | Where-Object { $_.CommandLine -match 'net.minecraft.client.main.Main|--gameDir|org.multimc|org.prismlauncher|cpw.mods.bootstraplauncher.BootstrapLauncher' })
if ($running.Count -ne 0) { throw 'Minecraft running: installation and config changes deferred' }
$pack=Get-Content -LiteralPath (Join-Path $instance 'mmc-pack.json') -Raw | ConvertFrom-Json
if (($pack.components | Where-Object uid -eq 'net.minecraft').version -ne '1.21.1' -or ($pack.components | Where-Object uid -eq 'net.neoforged.neoforge').version -ne '21.1.255') { throw 'Wrong platform' }
$prior=@(Get-ChildItem -LiteralPath $mods -Filter 'slavicmyths*.jar' -File)
if ($prior.Count -ne 1 -or $prior[0].Name -ne 'slavicmyths-1.7.8.jar') { throw 'Expected one installed 1.7.8' }
$priorPath=$prior[0].FullName;$priorHash=Digest $priorPath
if ($priorHash -ne (Digest (Join-Path $taskRoot 'release/slavicmyths-1.7.8.jar'))) { throw 'Installed previous JAR differs from accepted release' }
$jade=(Resolve-Path -LiteralPath (Join-Path $instance '.minecraft/config/jade/jade.json')).Path
$plugins=(Resolve-Path -LiteralPath (Join-Path $instance '.minecraft/config/jade/plugins.json')).Path
$options=(Resolve-Path -LiteralPath (Join-Path $instance '.minecraft/options.txt')).Path
$configPaths=@($jade,$plugins,$options)
foreach ($path in @($mods)+$configPaths) { if (-not ([IO.Path]::GetFullPath($path)).StartsWith($instance+[IO.Path]::DirectorySeparatorChar,[StringComparison]::OrdinalIgnoreCase)) { throw 'Target outside selected instance' } }
$originalJade=[IO.File]::ReadAllText($jade);$originalPlugins=[IO.File]::ReadAllText($plugins);$originalOptions=[IO.File]::ReadAllText($options)
if ($originalOptions -match '(?m)^key_(?!key.jade.show_overlay:).+:key.keyboard.keypad.6(?:\r?$|:)') { throw 'NumPad6 is already assigned' }
$newJade=$originalJade -replace '"displayTooltip"\s*:\s*false','"displayTooltip": true'
$newPlugins=$originalPlugins -replace '"entity_health.max_for_render"\s*:\s*\d+','"entity_health.max_for_render": 0' -replace '"entity_health.show_fractions"\s*:\s*false','"entity_health.show_fractions": true'
$newOptions=$originalOptions -replace '(?m)^key_key.jade.show_overlay:[^\r\n]+','key_key.jade.show_overlay:key.keyboard.keypad.6'
if (-not ($newJade | ConvertFrom-Json).general.displayTooltip -or ($newPlugins | ConvertFrom-Json).minecraft.'entity_health.max_for_render' -ne 0 -or ($newOptions -notmatch 'key_key.jade.show_overlay:key.keyboard.keypad.6')) { throw 'Configuration preparation failed' }
if (($newOptions -replace '(?m)^key_key.jade.show_overlay:[^\r\n]+','') -cne ($originalOptions -replace '(?m)^key_key.jade.show_overlay:[^\r\n]+','')) { throw 'Other key/options change' }
$before=@{}
Get-ChildItem -LiteralPath $instance -Recurse -File | Where-Object { $_.FullName -ne $priorPath -and $_.FullName -notin $configPaths } | ForEach-Object { $before[$_.FullName]=Digest $_.FullName }
$backupPath=[IO.Path]::GetFullPath((Join-Path $instance ('.slavicmyths-backups/pre-feedback-'+(Get-Date -Format 'yyyyMMdd-HHmmss'))))
if (-not $backupPath.StartsWith($instance+[IO.Path]::DirectorySeparatorChar,[StringComparison]::OrdinalIgnoreCase)) { throw 'Unsafe backup path' }
$null=New-Item -ItemType Directory -Path $backupPath
$saved=Join-Path $backupPath $prior[0].Name;Copy-Item -LiteralPath $priorPath -Destination $saved
foreach ($p in $configPaths) { Copy-Item -LiteralPath $p -Destination (Join-Path $backupPath ([IO.Path]::GetFileName($p))) }
if ((Digest $saved) -ne $priorHash) { throw 'Backup JAR mismatch' }
foreach ($p in $configPaths) { if ((Digest $p) -ne (Digest (Join-Path $backupPath ([IO.Path]::GetFileName($p))))) { throw 'Config backup mismatch' } }
$target=[IO.Path]::GetFullPath((Join-Path $mods 'slavicmyths-1.7.9.jar'));$staged=$target+'.new'
if ([IO.Path]::GetDirectoryName($target) -ne $mods -or (Test-Path -LiteralPath $target) -or (Test-Path -LiteralPath $staged)) { throw 'Unsafe destination' }
Copy-Item -LiteralPath $source -Destination $staged
if ((Digest $staged) -ne $gate.sha256) { throw 'Staging hash mismatch' }
try {
 Remove-Item -LiteralPath $priorPath
 Move-Item -LiteralPath $staged -Destination $target
 $utf8=[Text.UTF8Encoding]::new($false)
 [IO.File]::WriteAllText($jade,$newJade,$utf8);[IO.File]::WriteAllText($plugins,$newPlugins,$utf8);[IO.File]::WriteAllText($options,$newOptions,$utf8)
 if ((Digest $target) -ne $gate.sha256 -or @(Get-ChildItem -LiteralPath $mods -Filter 'slavicmyths*.jar' -File).Count -ne 1) { throw 'Installed hash mismatch' }
 if (-not (Get-Content -LiteralPath $jade -Raw | ConvertFrom-Json).general.displayTooltip -or (Get-Content -LiteralPath $plugins -Raw | ConvertFrom-Json).minecraft.'entity_health.max_for_render' -ne 0) { throw 'Jade changes missing' }
 foreach ($entry in $before.GetEnumerator()) { if ((Digest $entry.Key) -ne $entry.Value) { throw ('Other file changed: '+$entry.Key) } }
} catch {
 if (Test-Path -LiteralPath $target) { Remove-Item -LiteralPath $target }
 Copy-Item -LiteralPath $saved -Destination $priorPath -Force
 foreach ($p in $configPaths) { Copy-Item -LiteralPath (Join-Path $backupPath ([IO.Path]::GetFileName($p))) -Destination $p -Force }
 if (Test-Path -LiteralPath $staged) { Remove-Item -LiteralPath $staged }
 throw
}
$receipt=[ordered]@{version='1.7.9';status='PASS';instance=$instance;installed_jar=$target;sha256=(Digest $target);backup=$backupPath;previous_sha256=$priorHash;other_files_preserved=$before.Count;intentional_config_changes=@('Jade displayTooltip=true','Jade numeric current/max HP, including fractions','Jade toggle=NumPad6; class ability NumPad1 preserved');client_launches=0;server_launches=0;old_1_16_5='NOT_TOUCHED';worlds='NOT_TOUCHED'}
$receipt | ConvertTo-Json -Depth 4 | Set-Content -LiteralPath (Join-Path $taskRoot 'docs/verification/feedback-1.7.9/polymc-installation.json') -Encoding utf8
$receipt | ConvertTo-Json -Depth 4
