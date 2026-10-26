# #276: все сооружения обычного мира — поручение 26.10.2026

Источник: файл владельца `attachments/54b38c4c-cb78-4bdb-9426-059e4471fc10/Вставленный текст.txt` и прямое разрешение обновить JAR/версию PolyMC после реализации. Baseline hell `5ffe64764cb10c80385de10cd8eed50836911947`, дерево чистое. Конкретные умеренные веса делегированы владельцем; повторного approval каждого сундука не требуется. Minecraft/GameTest/client/server/PolyMC не запускать. Файловая установка разрешена отдельно и не является запуском.

Scope: все существующие mod Overworld native structures + features + saved/on-demand sites, vanilla village additions и их динамические награды. Vanilla structures/Nether navya_heart не изменяются. Таблица chest сама по себе не структура. #276 остаётся OPEN/OWNER_PENDING.

| REQUIREMENT | REAL_SOURCE | PLAYER_PATH | TEST | EXPECTED |
|---|---|---|---|---|
| Полная инвентаризация |21native definitions, biome/features, все production NBT, source-loot-routes, dynamic rewards | Обычная разведка/книга/NPC/карта → действительное место | Registry/resource/source coverage, template manifest, отдельная строка каждого site/variant | Ни одного скрытого пропуска из-за отсутствия chest table |
| Сохранить L1 | StructureRewards276, rewards276/{kurgan,camp}* | Те же шесть мест | Before/after exact preservation,20k/site | Никакого добавочного нового roll поверх L1 |
| Святыни огня/источника | SanctuaryPiece/Chest/Heart, actual NBT3regular+main,fire+guaranteed | Найти → ritual/guardian → unlockcache | Locked cache source сохранён, ritual prerequisite остаётся снаружи,20k/type | Тематические обычные сундуки, main выше курганаIII и ниже fortress |
| Крепость Горыныча | HillPlan, SerpentHillPiece, HillCommands, ChestMarkers/SeededChests | Реальная крепость → внутренняя сокровищница | Actual pure HillPlan containers incl5barrels, rotation/persistence/source tests,20k | Одна броня Полоза100%,вторая8%,IV3% на целую новую крепость; II>III>IV |
| Прочие места | SwampPiece, ShrineFeature, HomesteadFeature, ChudContents, village/city NBT, DepthsService,YagaHut,Bayun,Wolf | Добыть/победить/исследовать/пройти existing story | Манифест всего места +20k значимых типов | Умеренная тематическая польза; инженерные шахты/богатые сокровищницы KEEP при нормальном baseline |
| Совместимость | Stable registry/source, guards/markers, scripted rewards | Старый мир/частичная генерация/relog/MP | Scope/source/package/native offline; gameplay NOT_RUN | Не переписывать заполненные контейнеры/старые IDs; coins/trades/Tugarin/storykeys прежние |
| Сдача/установка | Java21 cleanbuild, JAR, explicit HELL1.21.1 | Owner playtest | Resources/package +verifiedbackup/install SHA/no duplicate | Точный installed source/version/SHA,0game launches,OPEN |

Тематические commits: inventory/contract; sanctuaries; fortress; swamp/minor/dynamic; QA/docs/version/delivery. При сюжетном риске фиксировать конкретный BLOCKED, продолжая независимое. Не добавлять #284 blocked artifacts, новых предметов/комплектов/структур/тик-сканирования. Gameplay путь и риски каждого места приводятся в общем отчёте.
