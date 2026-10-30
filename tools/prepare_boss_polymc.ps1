param(
 [Parameter(Mandatory=$true)][string]$Jar,
 [Parameter(Mandatory=$true)][string]$ExpectedSha256,
 [Parameter(Mandatory=$true)][string]$SourceSha,
 [Parameter(Mandatory=$true)][string]$CiRun,
 [Parameter(Mandatory=$true)][string]$OutputReceipt
)
$ErrorActionPreference='Stop'
$artifact=(Resolve-Path -LiteralPath $Jar).Path
if((Get-FileHash -LiteralPath $artifact -Algorithm SHA256).Hash.ToLowerInvariant() -ne $ExpectedSha256.ToLowerInvariant()){throw 'Tested artifact hash mismatch'}
$roots=@((Join-Path ([Environment]::GetFolderPath('ApplicationData')) 'PolyMC/instances'),(Join-Path ([Environment]::GetFolderPath('LocalApplicationData')) 'PolyMC/instances'))
$exe=Get-Command PolyMC.exe -ErrorAction SilentlyContinue
if($exe){$roots+=Join-Path (Split-Path -Parent $exe.Source) 'instances'}
$candidates=@(foreach($root in ($roots | Select-Object -Unique)){
 if(!(Test-Path -LiteralPath $root)){continue}
 foreach($profile in Get-ChildItem -LiteralPath $root -Directory){
  $pack=Join-Path $profile.FullName 'mmc-pack.json';$mods=Join-Path $profile.FullName '.minecraft/mods'
  if(!(Test-Path -LiteralPath $pack) -or !(Test-Path -LiteralPath $mods)){continue}
  $meta=Get-Content -LiteralPath $pack -Raw | ConvertFrom-Json
  $mc=@($meta.components | Where-Object {$_.uid -eq 'net.minecraft' -and $_.version -eq '1.21.1'})
  $neo=@($meta.components | Where-Object {$_.uid -eq 'net.neoforged.neoforge' -and $_.version -eq '21.1.255'})
  $slavic=@(Get-ChildItem -LiteralPath $mods -File -Filter 'slavicmyths-*.jar')
  if($mc.Count -eq 1 -and $neo.Count -eq 1 -and $slavic.Count -eq 1 -and $profile.Name -notlike '*Bosses*'){
   [pscustomobject]@{Profile=$profile;Mod=$slavic[0]}
  }
 }
})
if($candidates.Count -ne 1){throw "Expected one existing Slavic Myths 1.21.1 profile, found $($candidates.Count); no profile changed"}
$source=$candidates[0].Profile.FullName;$previous=$candidates[0].Mod
$previousHash=(Get-FileHash -LiteralPath $previous.FullName -Algorithm SHA256).Hash
$instances=Split-Path -Parent $source
$destination=Join-Path $instances 'Slavic-Myths-Bosses-2.1.6.1-Testing'
$resolvedTarget=[IO.Path]::GetFullPath($destination);$resolvedRoot=[IO.Path]::GetFullPath($instances)+[IO.Path]::DirectorySeparatorChar
if(!$resolvedTarget.StartsWith($resolvedRoot,[StringComparison]::OrdinalIgnoreCase) -or $resolvedTarget -eq $source){throw 'Unsafe profile target'}
if(Test-Path -LiteralPath $destination){throw 'New profile already exists; refusing to overwrite its settings or worlds'}
New-Item -ItemType Directory -Path $destination | Out-Null
foreach($name in @('mmc-pack.json','instance.cfg','patches','neoforge-metadata-original.json')){
 $file=Join-Path $source $name;if(Test-Path -LiteralPath $file){Copy-Item -LiteralPath $file -Destination $destination -Recurse}
}
$game=Join-Path $destination '.minecraft';New-Item -ItemType Directory -Path $game | Out-Null
foreach($name in @('config','defaultconfigs','resourcepacks','shaderpacks','options.txt','icon.png')){
 $file=Join-Path (Join-Path $source '.minecraft') $name;if(Test-Path -LiteralPath $file){Copy-Item -LiteralPath $file -Destination $game -Recurse}
}
$targetMods=Join-Path $game 'mods';New-Item -ItemType Directory -Path $targetMods | Out-Null
Get-ChildItem -LiteralPath (Join-Path $source '.minecraft/mods') -File | Where-Object Name -NotLike 'slavicmyths-*.jar' | ForEach-Object {Copy-Item -LiteralPath $_.FullName -Destination $targetMods}
$backup=Join-Path $destination 'mod-backups/previous-working';New-Item -ItemType Directory -Path $backup -Force | Out-Null
Copy-Item -LiteralPath $previous.FullName -Destination $backup
Copy-Item -LiteralPath $artifact -Destination $targetMods
$cfgPath=Join-Path $destination 'instance.cfg';$cfg=Get-Content -LiteralPath $cfgPath -Raw
$cfg=[regex]::Replace($cfg,'(?m)^name=.*$','name=Slavic Myths Bosses 2.1.6.1 TEST - owner acceptance pending')
[IO.File]::WriteAllText($cfgPath,$cfg,[Text.UTF8Encoding]::new($false))
$installed=Join-Path $targetMods (Split-Path -Leaf $artifact)
if((Get-FileHash -LiteralPath $installed -Algorithm SHA256).Hash.ToLowerInvariant() -ne $ExpectedSha256.ToLowerInvariant()){throw 'Installed artifact hash mismatch'}
if((Get-FileHash -LiteralPath $previous.FullName -Algorithm SHA256).Hash -ne $previousHash){throw 'Original working mod changed during installation'}
if(@(Get-ChildItem -LiteralPath $targetMods -Filter 'slavicmyths-*.jar').Count -ne 1){throw 'Duplicate installed Slavic Myths JAR'}
$receipt=[ordered]@{version='2.1.6.1-BOSSES-TEST';source_sha=$SourceSha;ci_run=$CiRun;jar_sha256=$ExpectedSha256.ToLowerInvariant();profile=$destination;source_profile=$source;previous_jar=$previous.Name;previous_sha256=$previousHash.ToLowerInvariant();old_profile_written=$false;worlds_copied=$false;worlds_overwritten=$false;local_gui_launches=0;runtime_evidence='Actual isolated GitHub-hosted packaged-mod clients; local PolyMC profile launch NOT_RUN to preserve desktop isolation';owner_acceptance='PENDING';audio='BLOCKED_AUDIO original recordings unavailable';created_utc=[DateTime]::UtcNow.ToString('o')}
$json=$receipt | ConvertTo-Json -Depth 5
[IO.File]::WriteAllText((Join-Path $destination 'BOSSES_TEST_INSTALL.json'),$json,[Text.UTF8Encoding]::new($false))
[IO.File]::WriteAllText([IO.Path]::GetFullPath($OutputReceipt),$json,[Text.UTF8Encoding]::new($false))
Write-Output $json
