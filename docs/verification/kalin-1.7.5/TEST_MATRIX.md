# Приёмка1.7.5 — bounded server/offline evidence

| № | Требование | Результат / предел доказательства |
|---|---|---|
|1|Печать / подходящий участок|PASS: real item use, occupied chest отказ/сохранён, staged camp. Natural uneven terrain не тестировался клиентом.|
|2|Ровно2 героя / выбор|PASS:0/1/3 отказ; все6 пар real HeroCalls, locks/UUIDs, no replacement.|
|3|Калин начинает верхом|PASS: actual passenger/owned horse,1800HP/4.2 riding height; native offline assets, client визуал NOT_RUN.|
|4|Конный рывок|PASS: real AI selection, >6 travel, collision hit, wall stops/STAGGER45; сложный pathing NOT_RUN.|
|5|Спешивание|PASS: actual65% health branch removes passenger, transition action, mount exit.|
|6|Elite2–3|PASS: actual entities/owned IDs/safe edge placements, summon fixture.|
|7|Common6–10|PASS: actual entities; command scheduling audited, full live cadence NOT_RUN.|
|8|Mixed below20%|PASS: actual phase4/KB1 + mixed real entities; full playthrough NOT_RUN.|
|9|Cap14|PASS: repeated waves never exceed14.|
|10|Camp navigation with2heroes/player/army|PARTIAL:6 pair entrypoints, lanes blueprint and charge/wall checks; prolonged combat/nav/performance NOT_RUN.|
|11|Parry/counter|PASS: one eligible hit cancelled/next not; real14t counter5HP through diamond.|
|12|Surrender/progression|PASS: alive1HP, hostile helpers cleaned, once-only victory/pair/count/eligibility; UI dialogue audit only.|
|13|SaveData persists|PASS: NBT load и real SavedData .dat disk readback сохраняют won/started/count/pair/pending reward; real world restart/player relog NOT_RUN.|
|14|No1.7.6 rewards|PASS: only progression flags; no reward registry/drops/XP.|
|15|Creative boss egg|PASS: registered real SpawnEggItem maps to boss; actual client click NOT_RUN.|
|Extra|Old summons/resource compatibility|PASS: all6 real hero pairs; prior JAR entries/resources retained by release gate; full old boss suites NOT_RUN.|
|Extra|Tank/falcon support|PASS: native Ilya taunt/shield action; falcon intercept only sabre, never whirlwind.|
|Extra|Abort/cleanup/overlap|PASS: owner offline abort; repeated owner start/nearby other owner refuse; actual placed blocks restored, changed diamond block kept; stale retreat flag cleared.|
|Extra|Art/audio/build|Native poses/UV/height/feet and generator/provenance/JAR gates; actual game rendering/audio NOT_RUN.|

Точные counts/logs/SHA — `resource-gates.json`, `polymc-installation.json`, `runtime-final.log`, `offline.log`, `clean-build.log`. Клиентов0. Synthetic players не считаются реальным multiplayer. NBT load не считается реальным перезаходом. MSPT не измерялся.

1.7.5 установлена в PolyMC Slavic-Myths-1.21.1-Testing с backup1.7.4. Clean build,1 focused GameTest/101 assertions, 149000 native checks/48 profiles и50122 production/resource gates PASS. Реальные16 GameTest server starts (включая исправленные итерации),0 клиентов.7426 prior JAR entries byte-identical;2 synthetic switch classes имеют прежние opcodes, изменились debug line tables. История победы дополнительно прочитана из настоящего SavedData .dat на диске. Полный игровой client/real network MP/relog/world restart/MSPT NOT_RUN.
