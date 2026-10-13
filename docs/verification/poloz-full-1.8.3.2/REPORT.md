# Полный архив Полоза — итог 1.8.3.2

Источник: `Slavic_Myths_1.8.3.2_Poloz_Full_Codex_TZ.zip`; сохранён в [spec-poloz-full-1.8.3.2](../../spec-poloz-full-1.8.3.2/00_overview/00_START_HERE.md). Реализована текущая версия; будущие этапы не добавлены. Ветка main; автоматического commit нет.

Полный архив Полоза 1.8.3.2 реализован поверх предыдущей итерации. Добавлены трофейная голова 3×3×4, гарантированная плеть, сердце с сохранением ID `poloz_core`, 8–12 чешуй и редкая руна IV (5%). Незеритовая броня укрепляется за две чешуи на часть: Shift + ПКМ по столу кузнеца с бронёй в руке; компоненты сохраняются. Бонусы частей суммируются: 55% сопротивления отбрасыванию и 22% снижения урона; комплект даёт ровно +2 HP и защиту от заражённой земли. Плеть: 13 урона, 6 блоков, широкая атака, три временных каменных шипа, 18 с перезарядки. Руна: четвёртое успешное попадание, разлом 6 урона/до четырёх целей/2 блока, 20 с перезарядки.

Clean build **PASS**; 4290 native geometry/policy checks (8 моделей), 120 native block checks, 31 финальный headless GameTest, 8208 resource/production checks **PASS**. Эта итерация: 5 headless JVM/5 GameTestServer миров, 4 успешных прогона; один прогон обнаружил оставленную тестом наград сохранённую перезарядку — изоляция фикстуры исправлена. Первая чистая сборка обнаружила UV-выход нового рога; исправление подтверждено. Клиентских запусков **0**; реальный multiplayer, перезаход и игровая оценка визуала/звука **NOT_RUN**; MSPT/FPS **NOT_MEASURED**. PolyMC Testing обновлён без запуска; 382 прочих файлов сохранены побайтно. [Проверки](resource-gates.json).

## Ключевые изменения

- `src/main/java/org/slavicmyths/poloz/PolozRewards.java`, `PolozRewardEvents.java`, `PolozWhip.java`: награды, улучшение с расходом двух чешуй, суммируемые бонусы, широкий удар, ограниченные проки руны. Первичный удар расходует прочность; secondary attacks не множат проки.
- `PolozTrophy.java`, `PolozStoneSpike.java`: большой цельный трофей и три видимые временные проекции каменных шипов.
- `GreatPoloz.java`, `PolozContent.java`: дропы, группы прислужников 4/6, reach, запись наград в Книге, дополнительный разлом без раздвоения HP.
- `rpg/Runes.java`, `rpg/RuneDefinition.java`: реальное распознавание, совместимость и сохранение руны IV существующей системой.
- `mor/MorBlocks.java`, `MorBlockExposure.java`, `MorGrowth.java`: каменный кирпич/мох/наросты и защита комплекта; старые IDs сохраняются.
- `client/PolozRenderer.java`, `PolozClient.java`, `PolozArmor.java`, `PolozTrophyRenderer.java`, `PolozSpikeRenderer.java`: детализированная форма, поднятые пластины и рога, сегменты, челюсть/клыки, глаза и проекции. Реализация Minecraft адаптирована к blueprint-референсам; игровая визуальная приёмка остаётся открытой.
- `src/main/resources/assets/slavicmyths`: пиксельные спрайты, текстуры, пять геометрических поз плети, blockstates, RU/EN, 17 авторских OGG. `data/slavicmyths`: loot, tales, tags. Сердце сохраняет registry ID `poloz_core`.
- `tools/poloz_full_assets.py`, `verify_poloz_full.py`, `install_poloz_full.ps1`, `poloz-runtime/PolozRewardTests.java`: воспроизводимые ресурсы, проверки готового JAR и установка. Предыдущие отчёты сохранены отдельно.

## Силуэт оружия и референсы

Плеть определена перед введением как отдельное оружие: короткая тяжёлая рукоять, длинная гибкая цепь тёмных шарниров с золотыми пластинами, багровые швы и сплошной шипованный наконечник. Геометрия держит отдельные соединённые сегменты, а смена пяти поз сгибает траекторию. Броня имеет horned helmet, выступающие shoulder plates, центральное красное ядро, отдельные накладки поножей/ботинок. Голова трофея использует ту же объёмную геометрию черепа/челюсти, что и босс, в другом масштабе.

Использованы референсы архива: armor_blueprint_primary, whip_blueprint_primary, rune_iv_blueprint, poloz_overall_blueprint и poloz_model_sheet. Численные игровые эффекты взяты из текста требований. [Спрайты наград](reward-sprites.png).

## Проверки и ограничения

Команды: `gradlew.bat -I tools/poloz-runtime.init.gradle runPolozChecks`; `gradlew.bat clean build -I tools/poloz-offline.gradle -I tools/mor-blocks-offline.gradle polozOfflineChecks morBlocksOfflineChecks`; `python tools/verify_poloz_full.py`; `tools/install_poloz_full.ps1`. JDK 21.0.12/NeoForge 21.1.255.

Native GameTests проверили сохранение всех компонентов брони (зачарования/руны/custom data/имя/повреждение), расход ровно двух чешуй, отказ при одной чешуе, +2 HP/снятие бонуса, заражённую землю, socket admission/NBT roundtrip, настоящий четвёртый hit/прибавку 6 урона/cooldown без рекурсивного счётчика, три настоящие проекции/24t lifetime/сохранность земли, 36 частей трофея/ровно один предмет, гарантированные награды и защиту от повторного дропа. Базовые 23 теста босса/блоков/ритуала/богатырей также прошли.

NBT roundtrip проверяет сериализацию; игровой перезаход не проводился. Аудио проверено ffprobe/декодированием на mono/rate/peak и 9-секундную сумму вызова/ответа; восприятие аллигатороподобного голоса человеком не проверялось. Клиентская геометрия проверена native UV bounds и спрайт-превью; вид в игре, полный бой и реальная сеть двух клиентов не проверялись. В логах остаются стандартные dev warnings Curios/refmap/vanilla commands и startup lag; измерений MSPT/FPS нет.

Краткая ручная проверка после разрешения клиента:

1. Полный бой, все фазы/уязвимые зоны/вода, награды — в отдельном тестовом мире.
2. Внешний вид, пять поз плети, броня, большой трофей и органический характер звука.
3. Два клиента и настоящий перезаход; измеренная нагрузка.

## Production и PolyMC

JAR: `D:\slavic-myths\build\libs\slavicmyths-1.8.3.2.jar`.

SHA256: `91da0b9f3552da1be2059a5f214af0d9c9460f6791ccf017ac518cb684013786`.

Установлен: `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.minecraft\mods\slavicmyths-1.8.3.2.jar`.

Резервная копия: `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.slavicmyths-backups\pre-full-1.8.3.2-20261012-194024`. 382 прочих файла профиля, конфигурации и миры сохранены побайтно; старый профиль 1.16.5 не затронут. Клиент не запускался. [Квитанция установки](POLYMC_INSTALLATION.md), [resource gates](resource-gates.json).
