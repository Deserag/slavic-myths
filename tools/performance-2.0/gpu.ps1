param([Parameter(Mandatory=$true)][int]$TargetPid,[Parameter(Mandatory=$true)][string]$Output)
$ErrorActionPreference='Stop'
'epoch_ms,engine,utilization_percent,query_ms' | Set-Content -LiteralPath $Output
'epoch_ms,working_set,private_bytes,process_cpu_seconds' | Set-Content -LiteralPath ($Output+'.process.csv')
$started=(Get-Process -Id $TargetPid).StartTime
while(Get-Process -Id $TargetPid -ErrorAction SilentlyContinue){
 $gameProcess=Get-Process -Id $TargetPid -ErrorAction SilentlyContinue
 if(!$gameProcess -or $gameProcess.StartTime -ne $started){break}
 if($gameProcess){"$([DateTimeOffset]::Now.ToUnixTimeMilliseconds()),$($gameProcess.WorkingSet64),$($gameProcess.PrivateMemorySize64),$($gameProcess.CPU.ToString('R',[Globalization.CultureInfo]::InvariantCulture))" | Add-Content -LiteralPath ($Output+'.process.csv')}
 $timer=[Diagnostics.Stopwatch]::StartNew()
 try{$samples=Get-CimInstance Win32_PerfFormattedData_GPUPerformanceCounters_GPUEngine -Filter "Name LIKE 'pid_${TargetPid}_%engtype_3D%'"; $elapsed=$timer.Elapsed.TotalMilliseconds;foreach($sample in $samples){"$([DateTimeOffset]::Now.ToUnixTimeMilliseconds()),$($sample.Name),$($sample.UtilizationPercentage),$($elapsed.ToString('R',[Globalization.CultureInfo]::InvariantCulture))" | Add-Content -LiteralPath $Output}}
 catch{"# GPU_COUNTER_UNAVAILABLE $($_.Exception.GetType().Name)" | Add-Content -LiteralPath $Output;break}
 Start-Sleep -Seconds 2
}
