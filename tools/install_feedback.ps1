$ErrorActionPreference='Stop'
$taskRoot=(Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$instance=(Resolve-Path -LiteralPath 'C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing').Path
$mods=(Resolve-Path -LiteralPath (Join-Path $instance '.minecraft/mods')).Path
if (-not $mods.StartsWith($instance+[IO.Path]::DirectorySeparatorChar,[StringComparison]::OrdinalIgnoreCase)) { throw 'Mods path outside selected instance' }
$gate=Get-Content -LiteralPath (Join-Path $taskRoot 'docs/verification/feedback-1.7.7/resource-gates.json') -Raw | ConvertFrom-Json
$source=Join-Path $taskRoot 'build/libs/slavicmyths-1.7.7.jar'
function Digest([string]$path) { (Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash.ToLowerInvariant() }
if ($gate.status -ne 'PASS' -or (Digest $source) -ne $gate.sha256) { throw 'Candidate does not match passed production gates' }
$buildLog=Get-Content -LiteralPath (Join-Path $taskRoot 'work/feedback-clean-build-final.log') -Raw
if ($buildLog -notmatch 'BUILD SUCCESSFUL') { throw 'Final clean build not passed' }
$running=@(Get-CimInstance Win32_Process -Filter "Name='java.exe' OR Name='javaw.exe'" | Where-Object { $_.CommandLine -match 'net.minecraft.client.main.Main|--gameDir|org.multimc|org.prismlauncher|cpw.mods.bootstraplauncher.BootstrapLauncher' })
if ($running.Count -ne 0) { throw 'Minecraft is running; do not replace its loaded JAR' }
$pack=Get-Content -LiteralPath (Join-Path $instance 'mmc-pack.json') -Raw | ConvertFrom-Json
if (($pack.components | Where-Object uid -eq 'net.minecraft').version -ne '1.21.1' -or ($pack.components | Where-Object uid -eq 'net.neoforged.neoforge').version -ne '21.1.255') { throw 'Wrong profile platform' }
$prior=@(Get-ChildItem -LiteralPath $mods -Filter 'slavicmyths*.jar' -File)
if ($prior.Count -ne 1) { throw 'Expected exactly one previous mod JAR' }
$priorPath=$prior[0].FullName
$priorHash=Digest $priorPath
if ($priorHash -ne (Digest (Join-Path $taskRoot 'release/slavicmyths-1.7.6.jar'))) { throw 'Previous installed JAR differs from accepted 1.7.6' }
$before=@{}
Get-ChildItem -LiteralPath $instance -Recurse -File | Where-Object FullName -ne $priorPath | ForEach-Object { $before[$_.FullName]=Digest $_.FullName }
$backupPath=[IO.Path]::GetFullPath((Join-Path $instance ('.slavicmyths-backups/pre-feedback-'+(Get-Date -Format 'yyyyMMdd-HHmmss'))))
if (-not $backupPath.StartsWith($instance+[IO.Path]::DirectorySeparatorChar,[StringComparison]::OrdinalIgnoreCase)) { throw 'Backup outside selected instance' }
$null=New-Item -ItemType Directory -Path $backupPath
$saved=Join-Path $backupPath $prior[0].Name
Copy-Item -LiteralPath $priorPath -Destination $saved
if ((Digest $saved) -ne $priorHash) { throw 'Backup verification failed' }
$target=[IO.Path]::GetFullPath((Join-Path $mods 'slavicmyths-1.7.7.jar'))
$staged=$target+'.new'
if ([IO.Path]::GetDirectoryName($target) -ne $mods -or (Test-Path -LiteralPath $target) -or (Test-Path -LiteralPath $staged)) { throw 'Unsafe or existing destination' }
Copy-Item -LiteralPath $source -Destination $staged
if ((Digest $staged) -ne $gate.sha256 -or (Digest $priorPath) -ne $priorHash) { throw 'Staging verification failed' }
try {
 Remove-Item -LiteralPath $priorPath
 Move-Item -LiteralPath $staged -Destination $target
} catch {
 if (-not (Test-Path -LiteralPath $priorPath)) { Copy-Item -LiteralPath $saved -Destination $priorPath }
 if (Test-Path -LiteralPath $staged) { Remove-Item -LiteralPath $staged }
 throw
}
if ((Digest $target) -ne $gate.sha256 -or @(Get-ChildItem -LiteralPath $mods -Filter 'slavicmyths*.jar' -File).Count -ne 1) { throw 'Installed JAR verification failed' }
foreach ($entry in $before.GetEnumerator()) { if ((Digest $entry.Key) -ne $entry.Value) { throw ('Other instance file changed: '+$entry.Key) } }
$receipt=[ordered]@{version='1.7.7';status='PASS';instance=$instance;installed_jar=$target;sha256=(Digest $target);previous_jar=$saved;previous_sha256=$priorHash;other_files_preserved=$before.Count;client_launches=0;old_1_16_5_instance='NOT_TOUCHED';acceptance='Client/gameplay acceptance NOT_RUN'}
$receipt | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $taskRoot 'docs/verification/feedback-1.7.7/polymc-installation.json') -Encoding utf8
$receipt | ConvertTo-Json
