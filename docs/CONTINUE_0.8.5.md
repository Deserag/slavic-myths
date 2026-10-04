# CONTINUE 0.8.5

## DONE
Три лабиринта, 17 блоков/предметов, 64px ресурсы, сохранение плана/экземпляра,
нарушение покоя, два режима проклятия, запечатанная гробница/три ниши, команды,
loot и README. Сборка и установка завершены.

## IN PROGRESS
Нет незавершённых шагов сборки/установки.

## TODO
Ручной QA: MANUAL_TEST_0.8.5.md. Minecraft запусков 0.

## BLOCKED
Нет блокировок реализации. Автоматическая проверка отклонила команду с очисткой
временных файлов (blocked by policy); work/kurgan085_reference, PlanCheck.java,
docs085.py оставлены на месте. В production JAR они не входят.

## FILES
src/main/java/org/slavicmyths/kurgan/: KurganPlan, KurganPlanNbt,
KurganDungeonPiece, KurganBlocks, KurganInstance, BurialRecords, KurganCurse,
KurganDisturbance, KurganDebug, KurganCommands, KurganStructure,
KurganStructures, BurialCoffinTile. SlavicMyths.java; build.gradle;
tools/kurgan_085.py, verify_kurgan_085.py, verify_resources.py, kurgan-tests;
новые resources; README и docs.

## ARCHITECTURE
См. верх ARCHITECTURE.md. Старые Structure ID и kurgan_shell сохранены.
Version=1 содержит фактический план, не новый RNG при загрузке.
Южный порт root-комнат зарезервирован под вход/лестницу.
Persistent curse хранится по player UUID и source UUID в Overworld BurialRecords.

## BUILD
PASS: clean build, финальный build, verifyKurgan (1200 планов/NBT),
verify_kurgan_085.py, verify_resources.py; Java8/reobf, без test classes в JAR.
build/libs/slavicmyths-0.8.5.jar. verification/headless-0.8.5.json.

## POLYMC
Slavic-Myths-Testing/.minecraft/mods/slavicmyths-0.8.5.jar установлен.
Один Slavic JAR; предыдущий сохранён в mod-backups; Curios/JEI не изменены.
SHA256/абсолютные пути: verification/install-0.8.5.json.

## MANUAL QA
TODO: визуал, реальный worldgen, проходы/лестницы, multiplayer колоды,
смерть/перезаход/молоко/clear, естественный поиск. Не заменять это headless QA.

## CONTINUE FROM HERE
Получить seed, tier, координаты, версию JAR и latest.log для реальных дефектов.
Не начинать врагов 0.8.6 или босса/лечение 0.8.7 без задания.
Не перезаписывать пользовательские battle_axe/carved_staff/club/retainer_shield
модели и 256px текстуры глобальным старым create_resources.py.
Генератор kurgan_085.py их не затрагивает.
