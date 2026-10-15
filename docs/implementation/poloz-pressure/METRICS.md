# Native A/B/C/D observations

One JVM, sequential scenes, one occurrence per scene. Rates: cumulative counter differences over observed endpoints after300 warmup ticks. Engine peaks below include warmup. Tick/frame windows: age>300; native scripted damage does not equal Survival/DPS acceptance.

|Scene|Tick avg /p95 /p99 ms|TPS|FPS|Frame p95 /p99 ms|Tracked particles peak|Channels peak|Handle failures in rate window|
|---|---|---:|---:|---|---:|---:|---:|
|A_idle|1.48 /2.14 /2.52|20.00|1965.5|0.93 /1.98|9|1|0|
|A_32zombies|2.53 /3.73 /4.60|20.00|1623.9|1.06 /1.88|9|75|0|
|B_128pillagers|5.95 /8.37 /9.93|20.00|194.6|8.09 /10.58|10|342|30575|
|C_poloz|4.11 /6.98 /15.58|20.00|659.5|3.28 /5.27|14790|342|259|
|D_poloz_128pillagers|9.71 /13.89 /17.72|20.00|190.7|9.00 /12.21|11818|352|17293|
|E_expiry|1.41 /2.16 /2.88|20.00|1413.7|1.51 /2.71|9|259|0|

Top particle/sound types, calls/s, residence, native channel removal and active phase counts are in metrics.json. Queued/tracked sounds differ from physically playing OpenAL sources; particles tracked differ from rendered surfaces.
