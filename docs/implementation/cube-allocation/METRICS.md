# Сопоставимые поздние окна

Ages2401–3900:75s sampling after120s scene warmup. Earlier raw data retained; concurrent fixture compilation in the early before window excluded in both runs. One independent JVM per side, no statistical claim from six in-process micro batches.

|Run|Tick avg /p95 /p99 ms|TPS|FPS|Frame p95 /p99 ms|Production allocation B/call median|
|---|---|---:|---:|---|---:|
|before|1.339 /1.877 /2.442|20.00|1992.9|0.880 /2.092|1808.000|
|after|1.317 /1.846 /2.312|20.00|965.7|1.784 /2.416|576.000|
