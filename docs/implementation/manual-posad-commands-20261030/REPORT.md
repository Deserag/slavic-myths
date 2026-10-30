> Исторический checkpoint. Актуальный единый путь `dev generate` и установка2.1.6.2: [новый отчёт](../terrain-and-boss-sync-20261030/REPORT.md).

# Ручные команды — техническое исправление 30.10.2026

## Исправлено
- Искусственная проверка имени `posad-qa-*` удалена. Ручные команды доступны с OP2 в обычном мире и Незере. Дополнительная фраза подтверждения больше не требуется; `create` понимает относительные координаты.
- Для6зданий Посада добавлен `slavicmyths dev posad building <id> [pos]`. Это исходный NBT и лут; весь объём проверяется перед записью, природный грунт внутри комнат удаляется. Повтор и чужие контейнеры отклоняются.
- Для6построек2.1 добавлен `slavicmyths dev nether create <id> [pos]`. Используются те же voxel-методы, что в native pieces, один воспроизводимый вариант, исходный lazy loot. Никакого biome/rarity roll для явной команды. ID капища в прежнем ответе был ошибочен: нужен `forgotten_shrine`, а `sanctuary_unfading_fire` — другая постройка.
- Целый город подготавливает чанки штатным bounded SiteChunks. Операции не блокируются ожиданием futures; проверки и запись идут на серверном потоке, tickets закрываются при завершении, отмене, ошибке, unload и stop. Natural StructureSet не включался. Старые города не достраиваются и не ретрофитятся.

## Тесты и матрица
Финальная сборка и целевые GameTest одним Java21 запуском: `clean build -I tools/posad-qa-runtime.gradle runPosadQa -PposadCommandOnly=true -PposadQaDir=work/command-fix-20261030-v7`; seed20261024; 4/4 GameTest PASS. Это4теста с24положительными случаями отдельных зданий, двумя целыми городами и негативными сценариями, не24отдельных GameTest. Артефакты: gates.txt, resources.json, source.json, production.patch, installation.json.

| REQUIREMENT | REAL_SOURCE | PLAYER_PATH | TEST/EXPECTED | EVIDENCE | RESULT |
|---|---|---|---|---|---|
| Обычное имя мира/OP2 | PosadManualJobs/PosadCommands | create в существующем мире | dispatcher create с~ и world=Test Level | actualCityCommandOrdinaryWorldNamesAndOpTwo, gates.txt | PASS |
| Обычный мир и Незер | те же | явное размещение | готовые durable records и физическая платформа в обеих dimension | POSAD_CITY_COMMAND_PASS | PASS |
| Все6зданий Посада | source NBT/StructureTemplate | building ID [~ ~ ~] | фактические3598/1992/2440/1901/1599/1349блоков ×2dimension |12POSAD_COMMAND_PASS | PASS |
| Все6построек2.1 | ManualNetherBuildings +authored voxels | nether create ID [~ ~ ~] | физические669/192/61/594/644/264блоков ×2dimension |12NETHER_MANUAL_COMMAND_PASS | PASS |
| Исходный лут/повтор | voxel/loot keys/ManualStructureRecords | создать и повторить | по1lazy-loot chest; у моста0; повтор отклоняется | sixNetherManualCommandsBothDimensions | PASS |
| Грунт не остаётся внутри комнат | checked envelope +NBT | поставить зал в природномSTONE | итог ровно3598authored blocks | actualBuildingCommandsBothDimensions | PASS |
| Без прав/чужой сундук/cancel | guards/preflight | отказ безOP; сундук с3алмазами | ноль изменений, cancel до записи убирает job | permissionsForeignChestAndCancelBeforeWrite | PASS |
| Подготовка чанков/границыработ | SiteChunks +phase budget | create→status/cancel | bounded native requests; полный lifecycle/ticket audit | successful whole city +source audit; не все failure cuts | NOT_RUN |
| Natural worldgen не включается | production diff | обычное исследование безOP | прежние biome/spacing/StructureSet | diff меняет только ручной канал | PASS |
| Java21/resources/JAR/install | build +metadata | Testing | exactSHA/backup/no duplicates | resources.json/installation.json | PASS |
| Полный relog/MP/визуал/старый пользовательский мир | owner/runtime | после ручного размещения | сохранение/нет дюпа/два клиента | не выполнялось | NOT_RUN |

Ручной OP-путь не является доказательством survival-доступности. Изменений art, registry IDs, естественной редкости, новых предметов и2.2боссов нет. Приёмка всей2.0/2.1/2.2 не заявляется; Issues не закрывались.

## История проверок
Реальных headless Minecraft процессов7, локальных GUI0. Четыре ранних неуспешных прогона сохранены: v1 — вложенный command result и незакреплённые тестовые чанки; v2 — неочищенная искусственная площадка города; v4/v5 — параллельная negative fixture попадала на уже активное задание другого теста. v3:2/2, v6:4/4, v7:4/4. Ошибки не скрыты и не засчитаны PASS. Были предупреждения старых API и длительные synthetic chunk setup/native survey; измеренного productionMSPT не заявляю. Полная исходная Posad suite в v2 не была успешна из-за этой city fixture; не называю её PASS по новому targeted прогону.

## Установка и исходники
Версия2.1.6-HELL. Установленный SHA256: `ba433a20522ac5771bce708e46edb80268a65a77cc995122586e2fac755f5b8d`.
Точный installed path и backup — installation.json. ОдинmodIdslavicmyths проверен по metadata всехJAR; установленный файл байт-в-байт совпадает сbuild/libs.
База исходников77dfc5dc75d6a816da9a0737bd4a757f2799a5c0 +локальныйproduction.patch (SHA вsource.json). Автоматического commit/push не сделано согласно AGENTS; не выдаётся за чистый GitHub commit. Исходные .eclipse/bin пользователя сохранены.

## Как проверить в Minecraft
Полные рабочие команды и пошаговые ожидаемые/негативные действия: [COMMANDS.md](COMMANDS.md). Запустить существующийTesting профиль, разместить разные здания на разных площадках; отдельно проверить обычный мир и Незер. БезOP/читов команда недоступна. Не принимать ручную выдачу за survival-поиск. После сохранения/перезахода проверить помещения, сундуки и отказ повтора; повторить вдвоём. Эти owner-сценарии NOT_RUN агентом.

## Риски/откат
Ручное размещение освобождает проверенный объём от природных блоков. Распознавание искусственных блоков не является универсальным распознаванием каменных домов или сторонних claims. После начала постройки города cancel может оставить частичный город с резервом; автоповтор наград запрещён. Откат: при закрытой игре вернуть единственный прежнийJAR изbackup; это не отменяет записанные блоки. Пользовательские миры агент не открывал.

## Следующая задача
Ручная проверка владельцем и оставшиеся save/MP/lifecycle gates. Новый milestone/боссы автоматически не запускаются.
