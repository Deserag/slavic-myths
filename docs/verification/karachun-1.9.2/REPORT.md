# Карачун 1.9.2 — фактическая реализация

Minecraft 1.21.1, NeoForge 21.1.255, Java 21.0.12, official mappings, mod ID slavicmyths, ветка main. Commit не создавался. Архив [ТЗ](../../specs/1.9.2/00_START_HERE/00_READ_ME_FIRST.md) проверен: 55 SHA256. Пользователь прямо разрешил реализовать 1.9.2 и обновить известный PolyMC Testing; архивные запреты запуска соблюдены. Указания пользователя о работе готовых предметов призыва имеют приоритет над дополнительными reputation gates.

Финальная чистая сборка и пять offline наборов — PASS; production/resource gates 21918 — PASS. SHA256 `75f1cec8b4af7fe2f566765328ec2a0108617a85921a169e6b84f709a511ea70`.

PolyMC Testing обновлён после gates; 297 остальных файлов сохранены побайтно. Предыдущий JAR сохранён в `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.slavicmyths-backups\pre-karachun-1.9.2-20261013-113133`.

| Блок | Реализация и внеигровое подтверждение | Игровая приёмка |
|---|---|---|
| Босс / 3 фазы / 5% | Karachun + NightRules; final-damage clamp и setHealth floor; yield идемпотентен | A02, A13–A15: NOT_RUN |
| Геометрия / анимации | KarachunMesh / KarachunModel, 26 bones / 293 boxes, отдельные волосы/мех/подвески/плащ/руки; native UV проверены | Front/side/back и соответствие референсу NOT_RUN |
| Вороны | NightRaven: круг/18tick предупреждение/фиксированный dive/контакт/отлёт; 4 враждебных, 2 союзных; собственные rig, texture, flap/glide/dive poses | A03–A04, A27: NOT_RUN |
| Угасание / offhand | NightStatus: 15% slow, периодический camera wobble, активный offhand и shield блокируются; предмет/тотем не изымаются; на мобах attack damage reduction, big-boss cap II | A05–A07: NOT_RUN |
| Страх / ночь / фантомы | NightNetwork и NightClient; отдельные личные 0–3 zombie/skeleton meshes, ray hit-test без fake packets, приоритет настоящего entity target; local fog, F8 reduced toggle с сохранением | A08–A10: NOT_RUN |
| Атаки | Голод IV 240t через физический снаряд; Frost cone, Wither II ближний удар, sweep; фиксированный круг Last Hour с 120t default предупреждением, без block damage | A11–A13: NOT_RUN |
| Коса / двуручное комбо | NightCombat + NightClient/Reaper/RenderHand: 5/5/7t windup, cone3.5/70°, LOS, по разу на цель, 3 distinct poses; ванильные skin hands/armor | A19–A20: NOT_RUN |
| Коса / пассивы | UUID конкретного оружия и цели, третий успешный hit→Wither I; life heal≤actual HP, 5s owner CD; undead +25% и 3HP за прямое подтверждённое убийство | A21–A22, A25: NOT_RUN |
| Коса / активы | Last Breath: 10t delay, radius4 LOS; Black Harvest: 3 lanes ±15°, no homing, block collision, общий cast target ledger | A23–A24: NOT_RUN |
| Руна / аксессуар / осколок | Единственная waning rune в текущих sockets, 20% +25 и per-target ICD; Curios necklace, 2 физических allied birds 400t / 3000t; гарантированный shard без будущего рецепта | A16–A18, A26–A27: NOT_RUN |
| NPC / сохранения | Yaga action702 и Bayun choice5 с существующими token/дистанцией/доверием; 4 obsidian+blue ice — выбранное проектное подношение. Готовый reusable тотем без reputation gate | A01, A28–A29: NOT_RUN |
| Награды / MP | Отдельный slavicmyths_night192 SavedData; подготовка всех независимых rewards→save→inventory receipt save→ack queue; reload cancels incomplete, complete/first-claim/pending сохраняются | Реальный crash/relog и 2–4 client MP NOT_RUN |
| Звук / RU | 24 оригинально синтезированных sound events, 32 OGG, spatial cue до dive; RU/EN, subtitles, bossbar phase, HUD, lore/advancement | A30: NOT_RUN |

## Проверки

`gradlew.bat -I tools/karachun-offline.gradle -I tools/morozko-offline.gradle -I tools/quest185-offline.gradle -I tools/poloz-offline.gradle -I tools/mor-blocks-offline.gradle clean build nightOfflineChecks frostOfflineChecks quest185OfflineChecks polozOfflineChecks morBlocksOfflineChecks`.

Night: 40 assertions, 10000 seeded reward comparisons, 8352 native UV vertices. Морозко 6946 / quest185 182 / Полоз 4290 / Mor blocks 120. Число UV vertices не выдаётся за игровые сценарии. NBT load не выдаётся за реальный перезаход. Resource gate проверяет JAR bytecode/resources, координаты native/JSON models, генератор, звуки и переводы, отсутствие test классов, 1.9.1 baseline и исправление закрытого буфера Полоза. Dedicated server не запускался: разделение client/common проверено статически.

Результаты: [resource-gates.json](resource-gates.json), [reproducibility.json](reproducibility.json), [установка](POLYMC_INSTALLATION.md), [offline UV preview](offline-preview.png). Offline preview — ортографическая проекция кубоидов, не игровой скриншот и не доказательство правильной позы в руках.

## Честные ограничения и ручная приёмка

Настоящих запусков агента — 0. В первую очередь проверить NPC и призыв; вид boss/raven/scythe и двуручное комбо от первого/третьего лица; блокировку щита при Угасании III вместе с тотемом бессмертия; distinction реального ворона/фантома; обход фиксированного Last Hour; награды двух игроков и полный инвентарь; повторный вход после завершения/прерывания. Полная матрица A01–A32 находится в архиве.

Модели вручную воссозданы из ступенчатых кубоидов; cinematic poster lighting/bloom не воспроизводится пиксель в пиксель. Используются native pose functions, не GeckoLib клипы. Ванильные zombie/skeleton meshes фантомов окрашены и слегка дрожат, без собственного AI. Головокружение реализовано периодической камерой, подавляется F8; постоянное vanilla Nausea намеренно не навязывается. Угасание на мобах снижает скорость движения/урон, без полного выключения AI. Лечение undead kill сейчас учитывает прямой серверный урон косой, не независимый поздний DOT. Утраченный first-clear амулет бесплатно не восстанавливается; тотем восстанавливается через те же NPC. Перезапуск отменяет незавершённое испытание без лута. MSPT/FPS не измерены.
