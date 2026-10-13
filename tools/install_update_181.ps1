$ErrorActionPreference='Stop'
$taskRoot=(Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$instance=(Resolve-Path -LiteralPath 'C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing').Path
$mods=(Resolve-Path -LiteralPath (Join-Path $instance '.minecraft/mods')).Path
function Digest([string]$path) { (Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash.ToLowerInvariant() }
$gate=Get-Content -LiteralPath (Join-Path $taskRoot 'docs/verification/update-1.8.1/resource-gates.json') -Raw | ConvertFrom-Json
$source=Join-Path $taskRoot 'build/libs/slavicmyths-1.8.1.jar'
if ($gate.status -ne 'PASS' -or (Digest $source) -ne $gate.sha256) { throw 'Production gate mismatch' }
if ((Get-Content -LiteralPath (Join-Path $taskRoot 'work/update-1.8.1-offline.log') -Raw) -notmatch 'BUILD SUCCESSFUL') { throw 'Offline checks missing' }
$running=@(Get-CimInstance Win32_Process -Filter "Name='java.exe' OR Name='javaw.exe'" | Where-Object { $_.CommandLine -match 'net.minecraft.client.main.Main|--gameDir|org.multimc|org.prismlauncher|cpw.mods.bootstraplauncher.BootstrapLauncher' })
if ($running.Count) { throw 'Minecraft is running' }
$pack=Get-Content -LiteralPath (Join-Path $instance 'mmc-pack.json') -Raw | ConvertFrom-Json
if (($pack.components | Where-Object uid -eq 'net.minecraft').version -ne '1.21.1' -or ($pack.components | Where-Object uid -eq 'net.neoforged.neoforge').version -ne '21.1.255') { throw 'Wrong platform' }
$prior=@(Get-ChildItem -LiteralPath $mods -Filter 'slavicmyths*.jar' -File)
if ($prior.Count -ne 1 -or $prior[0].Name -ne 'slavicmyths-1.7.9.jar') { throw 'Expected one installed 1.7.9' }
$priorPath=$prior[0].FullName
$before=@{}
Get-ChildItem -LiteralPath $instance -Recurse -File | Where-Object FullName -ne $priorPath | ForEach-Object { $before[$_.FullName]=Digest $_.FullName }
$backup=[IO.Path]::GetFullPath((Join-Path $instance ('.slavicmyths-backups/pre-update-181-'+(Get-Date -Format 'yyyyMMdd-HHmmss'))))
$target=[IO.Path]::GetFullPath((Join-Path $mods 'slavicmyths-1.8.1.jar'))
foreach ($path in @($backup,$target,$priorPath)) { if (-not $path.StartsWith($instance+[IO.Path]::DirectorySeparatorChar,[StringComparison]::OrdinalIgnoreCase)) { throw 'Unsafe target' } }
if ((Test-Path -LiteralPath $target) -or (Test-Path -LiteralPath ($target+'.new'))) { throw 'Destination exists' }
$null=New-Item -ItemType Directory -Path $backup
$saved=Join-Path $backup $prior[0].Name
Copy-Item -LiteralPath $priorPath -Destination $saved
if ((Digest $saved) -ne (Digest $priorPath)) { throw 'Backup mismatch' }
Copy-Item -LiteralPath $source -Destination ($target+'.new')
if ((Digest ($target+'.new')) -ne $gate.sha256) { throw 'Staging mismatch' }
try {
 Remove-Item -LiteralPath $priorPath
 Move-Item -LiteralPath ($target+'.new') -Destination $target
 if ((Digest $target) -ne $gate.sha256 -or @(Get-ChildItem -LiteralPath $mods -Filter 'slavicmyths*.jar' -File).Count -ne 1) { throw 'Installed mismatch' }
 foreach ($entry in $before.GetEnumerator()) { if ((Digest $entry.Key) -ne $entry.Value) { throw ('Other file changed: '+$entry.Key) } }
} catch {
 if (Test-Path -LiteralPath $target) { Remove-Item -LiteralPath $target }
 Copy-Item -LiteralPath $saved -Destination $priorPath
 throw
}
Copy-Item -LiteralPath $source -Destination (Join-Path $taskRoot 'release/slavicmyths-1.8.1.jar')
$receipt=[ordered]@{version='1.8.1';status='PASS';instance=$instance;installed_jar=$target;sha256=(Digest $target);backup=$backup;other_files_preserved=$before.Count;client_launches=0;server_launches=0;worlds='NOT_TOUCHED';old_1_16_5='NOT_TOUCHED';minecraft='1.21.1';neoforge='21.1.255'}
$receipt | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $taskRoot 'docs/verification/update-1.8.1/polymc-installation.json') -Encoding utf8
$receipt | ConvertTo-Json
