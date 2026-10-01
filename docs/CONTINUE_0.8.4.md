# CONTINUE 0.8.4

## DONE

Колода, пять слотов, крышка/звуки/четыре вида останков; natural burial opened data;
оружие/реставрация/аксессуары/находки; девять darkened blocks и smoke aging;
три exterior shells с существующими деревьями/входами/метками/paths;
locate/next/dev teleport, loot, RU/EN, генератор и проверки.

## IN PROGRESS

Нет незавершённых технических шагов; ожидается ручной QA.

## TODO

Minecraft manual QA из MANUAL_TEST_0.8.4.md, включая фактический terrain, визуал,
звук, поиск Great, двух игроков и сохранение. Реальных запусков не было.

## BLOCKED

Нет известных внешних блокировок. Runtime-поведение пока не подтверждено игрой.

## FILES

src/main/java/org/slavicmyths/kurgan/*;
client/BurialCoffinRenderer.java, BurialCoffinScreen.java, ClientSetup;
registries ModBlocks/Items/Tiles/Sounds; SlavicMyths.java;
tools/burial_084.py, verify_burial_084.py, tests/KurganShapeTest.java,
verify_resources.py, create_resources.py; data loot/recipes/tags/models/textures/audio.

## ARCHITECTURE

См. верх ARCHITECTURE.md. Сохранить двухблочный FOOT tile, existing Curios/Armorer,
WorldSavedData с одним burial-opened bit/UUID, event-discovered smoke queue,
StructurePiece/KurganShape и существующую TreeShape. Не заменять это новым framework.
Нет dungeon и новых entity. Шаблоны дерева/формула кургана не требуют NBT assets.

## BUILD

PASS: clean build и финальный build; verify_burial_084.py; verify_resources.py; KurganShapeTest (100000 кандидатов).
Проверены 310 item registrations, 132 block registrations, 275 PNG, 200 recipes, 76 advancements, Java8/reobf и mono Vorbis.
Production: build/libs/slavicmyths-0.8.4.jar, Java8/reobf.

## POLYMC

Установлен slavicmyths-0.8.4.jar в Slavic-Myths-Testing. Один Slavic JAR, SHA256 совпадает с production, Curios/JEI не изменены.
Предыдущий JAR сохранён в mod-backups. Отчёт: verification/install-0.8.4.json. Minecraft не запускался.

## MANUAL QA

MANUAL_TEST_0.8.4.md. Это TODO, не считать build доказательством игрового поведения.

## CONTINUE FROM HERE

После технической установки получить пользовательский результат:
сначала обе половины колоды (содержимое/ломание/двое игроков/перезаход), затем
smoke aging (pause/resume/reload) и три `/slavicmyths dev tp_kurgan ...`.
Если Great слишком редок, проверять KurganStructure terrain/exclusion checks
и KurganCommands search bounds; не обходить terrain validation принудительным
размещением. Исправлять конкретные результаты QA в соответствующих классах,
затем build + verify_burial_084.py + verify_resources.py + обновление PolyMC.
0.8.5 не начинать без отдельного задания.
