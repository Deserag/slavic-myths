# #140 и #141 — техническая сдача, игровая приёмка ожидается

21.10.2026. Source base `32d1733457e7e90360ce16c23860304ece664c64` + незакоммиченный snapshot `source-manifest.json`; принятый декор #108 сохранён. Новых коммитов/push нет. Тестовая версия 1.9.7.51. Minecraft/PolyMC/сервер/GameTest: **0 запусков**.

## #140: головы

Шесть новых ID: `tugarin_head`, `skim_head`, `cursed_volkolak_head`, `likho_head`, `mor_likho_head`, `large_podzemnik_head`. На настоящей смерти соответствующего моба LivingDropsEvent добавляет ровно одну голову без RNG/Looting. Человеческая фаза Тугарина исключена. Полоз, Горыныч и Серый волк не изменены. Остальные предметы лута сохранены. Отменённые события не обходятся.

В мире и руках используются отдельные экземпляры настоящих ModelPart с родными UV/PNG; у волколака сохранён renderer depth .68, у Скима сохранена грива. В GUI — прозрачные 16×16 PNG из тех же фактических граней. Лихо и Моровое Лихо занимают 2 клетки, Подземник — 2; остальные — 1. Ским с гривой целиком помещается в одной клетке: вторая создавала бы пустой невидимый блок. Контуры совпадают с рассчитанными границами рендера, вращение — четыре стороны. У двухблочной головы единственный нижний BE/loot anchor; удаление любой половины удаляет пару. Требуется игровая проверка разрушения/сохранения.

Реальные исходные части и полученные трофеи в одинаковом масштабе/ракурсе: [A/B](native-heads-source-before-after.png), [общий результат](six-heads-result.png), [вершины и UV](native-heads.json). «До» — исходная часть моба, а не ранее существовавшая голова: этих шести предметов раньше не было. Геометрия/UV/PNG живых мобов не переделаны; два accessor-метода возвращают часть независимого экземпляра модели.

Production: `trophy/TrophyHeads.java`, `TrophyHeadBlock.java`, `TrophyHeadDrops.java`, `client/TrophyHeadsClient.java`, init в SlavicMyths, accessor в WorldBossModel/CursedVolkolakModel; 6 комплектов blockstates/block/item/held/PNG/loot, RU/EN и Creative catalog. Каноническое воспроизведение: `tools/trophy140_resources.py` + pinned `trophy140_icons.zip`, финальный overlay `create_resources.py`. Иконки пересчитываются из фактических частей через native offline exporter и `trophy140_preview.py`.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Шесть реальных частей, UV, границы и коллизии | verifyHeads140Offline, 11449 checks; native-heads.json | PASS |
| Правила выбора головы/исключения форм | production pure drop rule и offline form matrix | PASS |
| Событие реальной смерти → предмет в игре | исходник LivingDropsEvent; игровой сценарий ниже | NOT_RUN / OWNER_PENDING |
| Ресурсы, локализация, Creative, воспроизводимость, JAR | verification.json | PASS |
| Размещение, разрушение, повороты, перезаход, руки/GUI | production block/BER/BEWLR, native source views; игровой сценарий | NOT_RUN / OWNER_PENDING |
| Существующие несвязанные source/resources сохранены | baseline.json → changed-existing.json | PASS |

## #141: единая генерация

Все генераторы получили `/slavicmyths dev generate <строение> [существующие параметры]`; обработчики, permissions, player/console context, config gates и алгоритмы генерации прежние. Размер следует после имени там, где он существует. Совместимые старые команды остаются алиасами. Телепорт к болоту не выдаётся за генерацию. Отдельный `courtyard_debug` сохраняет особый debug gate. Всего 41 исполняемый вариант нового дерева; 90 записей совместимости в generation-aliases.json.

Полный список и примеры: [GENERATION_COMMANDS.md](../../GENERATION_COMMANDS.md). Production-изменение ограничено `commands/CommandTree.java`; существующие builders остаются источником дерева. Документация и миграция воспроизводятся `tools/command_reference.py --write/--check`.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Общий путь, completions, допустимые размеры | настоящий Brigadier tree, verifyCommandsOffline: 9592 checks / 676 nodes | PASS |
| Прежние обработчики, аргументы, gates и алиасы | production tree fixtures, callback spies без запуска генерации, aliases.json | PASS |
| Справка и документация соответствуют production builders | command_reference.py --check | PASS |
| Реальная генерация/размещение в мире | сценарии ниже; game launch запрещён | NOT_RUN / OWNER_PENDING |

## Сборка и установка

Java 21 `gradlew.bat clean build`: PASS; журнал `build-heads140-generation141.log`. Отдельные offline задачи: `-I tools/commands-offline.init.gradle -I tools/heads140-offline.gradle verifyCommandsOffline verifyHeads140Offline`: PASS, журнал `build-heads140-generation141-offline.log`. Все production resource bytes сверены с JAR, JSON/PNG декодированы: `verification.json`. Точные installed path/SHA256/backup/source и отсутствие дублей — `installed.json` после установки. Это тестовый candidate, не релиз и не приёмка.

## Проверка владельцем

1. На копии мира убить каждого из шести боссов, отдельно обычное/Моровое Лихо и настоящую вторую фазу Тугарина: ровно одна соответствующая голова. Проверить без Looting и с ним; человеческая фаза не даёт голову.
2. Каждую голову посмотреть в GUI, руках, на земле; поставить во всех 4 направлениях. Для двухблочных проверить отказ под потолком, разрушение обеих половин в survival/creative, взрыв, выгрузку/перезаход: без остатка и двойного лута.
3. `/slavicmyths dev generate` + Tab, затем `bandit_camp small`, `kurgan warrior`, `abandoned great`, `city attached`, `yaga`; проверить появление и размеры в отдельном мире. Сравнить со старым алиасом; проверить отказ без разрешений/debug config. Самостоятельно игру не запускали.

Рекомендуемый Status обеих задач: **Тестирование**. Технического доступа к изменению поля Project нет; поле не изменено. Issues остаются OPEN, owner-accepted не добавлялся.
