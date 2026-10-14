$ErrorActionPreference='Stop'
$taskRoot=(Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$instance=(Resolve-Path -LiteralPath 'C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing').Path
$mods=(Resolve-Path -LiteralPath (Join-Path $instance '.minecraft/mods')).Path
function Digest([string]$path){(Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash.ToLowerInvariant()}
$gate=Get-Content -LiteralPath (Join-Path $taskRoot 'docs/integration-197/gates.json') -Raw | ConvertFrom-Json
$source=Join-Path $taskRoot 'build/libs/slavicmyths-1.9.7.jar'
if($gate.status -ne 'PASS' -or (Digest $source) -ne $gate.sha256){throw 'Production candidate differs from passed gates'}
$head=(& git -C $taskRoot rev-parse HEAD).Trim()
if($head -ne $gate.source_commit){throw 'Candidate source commit differs from current HEAD'}
& git -C $taskRoot diff --quiet HEAD -- src build.gradle gradle.properties tools
if($LASTEXITCODE -ne 0){throw 'Uncommitted implementation differs from candidate'}
foreach($name in @('clean-build.log','integrated-gametest.log','integrated-native-client.log')){
 $log=Get-Content -LiteralPath (Join-Path $taskRoot ('.tools/'+$name)) -Raw
 if($log -notmatch 'BUILD SUCCESSFUL'){throw ('Passed run missing: '+$name)}
}
$pack=Get-Content -LiteralPath (Join-Path $instance 'mmc-pack.json') -Raw | ConvertFrom-Json
if(($pack.components | Where-Object uid -eq 'net.minecraft').version -ne '1.21.1' -or ($pack.components | Where-Object uid -eq 'net.neoforged.neoforge').version -ne '21.1.255'){throw 'Wrong target platform'}
$minecraft=@(Get-CimInstance Win32_Process | Where-Object {$_.Name -in @('java.exe','javaw.exe') -and $_.CommandLine -match 'net.minecraft.client.main.Main|--gameDir|cpw.mods.bootstraplauncher.BootstrapLauncher|org.multimc|org.polymc'})
if($minecraft.Count -ne 0){throw 'Minecraft still running: close before installing'}
$prior=@(Get-ChildItem -LiteralPath $mods -Filter 'slavicmyths-*.jar' -File)
if($prior.Count -ne 1 -or (Digest $prior[0].FullName) -ne $gate.baseline_sha256){throw 'Installed baseline differs: revalidate before replacing'}
$target=[IO.Path]::GetFullPath((Join-Path $mods 'slavicmyths-1.9.7.jar'));$staged=$target+'.new'
foreach($path in @($target,$staged,$prior[0].FullName)){if([IO.Path]::GetDirectoryName([IO.Path]::GetFullPath($path)) -ne $mods){throw 'Unsafe target outside mods'}}
if(Test-Path -LiteralPath $staged){throw 'Existing staging file requires inspection'}
$before=@{};Get-ChildItem -LiteralPath $instance -Recurse -File | Where-Object {$_.FullName -ne $prior[0].FullName} | ForEach-Object {$before[$_.FullName]=Digest $_.FullName}
$stamp=[TimeZoneInfo]::ConvertTimeBySystemTimeZoneId([DateTime]::UtcNow,'Russian Standard Time').ToString('yyyyMMdd-HHmmss')
$backupPath=[IO.Path]::GetFullPath((Join-Path $instance ('.slavicmyths-backups/pre-integrated-197-'+$stamp)))
if(-not $backupPath.StartsWith($instance+[IO.Path]::DirectorySeparatorChar,[StringComparison]::OrdinalIgnoreCase)){throw 'Unsafe backup directory'}
New-Item -ItemType Directory -Path $backupPath | Out-Null
$saved=Join-Path $backupPath $prior[0].Name;Copy-Item -LiteralPath $prior[0].FullName -Destination $saved
if((Digest $saved) -ne $gate.baseline_sha256){throw 'Backup checksum mismatch'}
Copy-Item -LiteralPath $source -Destination $staged
if((Digest $staged) -ne $gate.sha256){throw 'Staging checksum mismatch'}
try{
 Move-Item -LiteralPath $staged -Destination $target -Force
 if((Digest $target) -ne $gate.sha256 -or @(Get-ChildItem -LiteralPath $mods -Filter 'slavicmyths-*.jar' -File).Count -ne 1){throw 'Installed checksum/duplicate check failed'}
 foreach($entry in $before.GetEnumerator()){if((Digest $entry.Key) -ne $entry.Value){throw ('Unrelated file changed: '+$entry.Key)}}
}catch{Copy-Item -LiteralPath $saved -Destination $target -Force;throw}
$receipt=[ordered]@{status='PASS';label='1.9.7 + патч оружия — коса, броня, руны, здоровье существ (этап1)';version='1.9.7';source_commit=$gate.source_commit;instance=$instance;installed_jar=$target;sha256=(Digest $target);previous_sha256=$gate.baseline_sha256;backup=$saved;preserved_profile_files=$before.Count;client_launch='PENDING';rollback='Close Minecraft; copy verified backup over installed_jar; verify previous_sha256';components=$gate.components}
$receipt | ConvertTo-Json -Depth 8 | Set-Content -LiteralPath (Join-Path $taskRoot 'docs/integration-197/polymc-installation.json') -Encoding utf8
$receipt | ConvertTo-Json -Depth 8
