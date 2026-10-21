# Руническое сияние и здоровье полных комплектов · 1.9.7.48 · 21.10.2026

**IN_PROGRESS / OWNER_PENDING. #104 OPEN; #105 NOT_STARTED.** Тестовая48 установлена по действующему поручению владельца. Это не финальный релиз и не художественная приёмка владельцем. Commit/push/закрытия Issue не выполнялись.

Исправлен провал видимости45: низкая альфа дополнительно ослаблялась квадратом плотности, а обычное alpha blending могло затемнять материал. В47/48 плотность линейная, alpha64/100/140, палитра ярче; SRC_ALPHA/ONE из установленного NeoForge LIGHTNING_TRANSPARENCY даёт световой вклад, не затемнение. Мелкий shared128px PNG и медленный UV ход12/7 сохранены, крупных ремней нет в снятых ракурсах. EQUAL depth, armor view offset, fixed buffers, zero-rune bypass, независимость стеков и vanilla glint сохранены. Rune glow определяется установленными рунами, не обычным enchantment. Изменения моделей/UV/текстур брони другой задачи сохранены.

Подтверждённые владельцем значения за все4 части одного комплекта: Перунит +5 ХП (2,5 сердца), Полоз +14 ХП (7 сердец; заменяет прежние +2), драконий +20 ХП (10 сердец). Неполные и смешанные комплекты бонуса не дают. ArmorSetHealth вызывается существующими equipment/join/login/respawn/dimension events PolozRewardEvents, без нового tick polling. Стабильные transient attribute IDs, no stacking; увеличение максимума не лечит, при снятии избыток текущего HP обрезается новым максимумом. Старый poloz_set_health ID сохранён. RU/EN tooltip и генератор Полоза актуализированы. Одиночные предметы/одежда не превращены в новые сеты; прочие защитные эффекты сохранены.

Java21 clean build + plain JVM rune regression PASS (build.log, 1m5s). Ресурсы/production JAR:7442 ресурсов,1339 PNG,1743 классов Java21; ZIP CRC, version, source parity, отсутствие RuneHealthProbe и других test classes PASS (jar-check.json). Точный dirty source fingerprint и все файлы — source-before.json. Shader standalone compile/link — ../rune-glow-visible-47/shader-compile.json; совпадает с shader в48.

Реальные запуски: **5 dev clients,4 входа в отдельную копию тестового мира**,0 dedicated servers/GameTest,0 запусков установленного PolyMC JAR. Пользователь позже явно разрешил Minecraft для доработки; пользовательские миры PolyMC не изменялись. Подробные исходы и ограничения — runtime.json. Первый health fixture ошибочно учитывал естественную регенерацию: она выключена только в тестовой копии. Второй запуск прерван при загрузке ресурсов, shutdown exception сохранён. Третий и четвёртый полный scripted pass, пятый целевой survival inventory/back/side pass. Gradle BUILD SUCCESSFUL сам по себе не принят за runtime: проверены явные HEALTH48_PASS и RUNTIME_COMPLETE markers.

**GAME evidence, не OFFLINE:** game-frames/poloz-day.png, iron-day.png, perunite-day.png, dragon-day.png, linen-day.png; слева направо **0/1/2/3 руны**. В четвёртом прогоне убраны загородившие исходные кадры животные тестового мира. poloz-night/iron-night, poloz-back/poloz-side; *_late пары через20 клиентских ticks показывают меняющийся свет (frame-motion.json; метрика не является FPS/художественной приёмкой). vanilla-enchantment-reference.png — реальный vanilla enchanted iron без рун; poloz-enchant-and-runes.png — совместное наложение. poloz-inventory.png — настоящий survival InventoryScreen с надетым игроком Полоз3 без enchant. Initial creative inventory capture был непригоден для проверки персонажа и заменён целевым survival запуском. Исходные владельческие GAME-owner-45-*.png сохранены; их камера/освещение отличаются, это не контролируемое 1:1 A/B.

| REQUIREMENT | ITEM ID | rune count | viewing context | SOURCE BEFORE | SOURCE AFTER | GAME OWNER CAPTURE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|---|---|---|---|---|
| Видимое мягкое сияние, шкала0–3 | minecraft:iron armor; slavicmyths:poloz/perunite/dragon armor |0–3|GAME native armor stands, день|rejected45 source в ../rune-glow-visible-47/before|48 PNG/GLSL/style; game-frames/*-day.png|old45 provided; new48 pending|PASS scripted; OWNER_PENDING |
| Одежда | slavicmyths:linen_shirt |0–3|GAME armor stands|45 source|game-frames/linen-day.png|pending|PASS scripted |
| Ночь, спина, бок, движение света | iron/poloz |0–3|GAME same fixture, two timestamps|old owner back45|*-night, poloz-back, poloz-side, *-late.png|pending|PASS sampled; OWNER_PENDING |
| Надетый игрок в инвентаре | poloz set |3|GAME survival InventoryScreen|owner45 inventory|game-frames/poloz-inventory.png|pending|PASS sampled |
| Vanilla glint и совместное наложение | iron enchanted; poloz enchanted+runes |0–3|GAME|standard vanilla pipeline|vanilla-enchantment-reference.png / poloz-enchant-and-runes.png|pending|PASS sampled |
| Три set health бонуса | perunite/poloz/dragon |0|GAME integrated server ServerPlayer|Полоz+2, других бонусов не было|11 HEALTH48_PASS markers в client-attempt4.log|n/a|PASS |
| Partial/mixed/no heal/clamp/no stacking | все3сета |0|GAME integrated server|existing equipment events|client-attempt4.log|n/a|PASS |
| Clean build, source/resources/JAR parity | all |all|Java21/offline packaging|source-before.json|build.log, jar-check.json|n/a|PASS |
| Установка/backup/одинJAR/6прочихмодов | all |all|PolyMC filesystem|installed45|installed.json|pending|PASS |
| Dedicated server, MP, actual relog/dimension/death | все3сета |all|runtime|no evidence|events reviewed, scenarios not executed|absent|NOT_RUN |
| GUI item icons/1P/ground/frame/Fabulous/весь полный набор #104 | all |0–3|GAME|no controlled capture|не заявлено проверенным|absent|NOT_RUN |
| Производительность и длительный gameplay | all |all|runtime|no measurements|не измерены|absent|NOT_RUN |
| Визуальная приёмка владельцем | all |0–3|owner game|45 rejected|48 ждёт проверки|absent|NOT_RUN |

Точная установленная версия/путь/SHA256/source base commit/backup — [installed.json](installed.json). Source69a2db12 + незакоммиченные изменения. Backup45 вне mods, один активный Slavic JAR,6 прочих JAR сохранены по SHA256. После dev runtime установленный PolyMC JAR не запускался. #104 остаётся OPEN; #105 ждёт явной приёмки #104.
