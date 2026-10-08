# Performance audit — 1.2.5

Статический аудит, не профиль MSPT/FPS. Частоты взяты из production-кода.

| Область | Реальный предел / механизм | Решение |
|---|---|---|
| VillageWork | ACTION_COOLDOWN 200 + UUID offset 0..39; storage search 400; POI radius 32 / до 8 кандидатов; field search локальные 147 + до 768 позиций | Сохранены cadence, приоритеты и vanilla Brain |
| Cached storage/textile | Проверки расстояния, loaded chunk и типа BlockEntity | Старые invalidation сохранены, новых кэшей нет |
| SettlementChest | 64 реальных слота, non-ticking | Фонового поиска нет |
| GuardBehavior | hostile search / path requests 40 ticks, patrol 300, combat cooldown | Баланс/AI не изменены |
| Bandit discovery | 400 ticks со сдвигом entity ID, loaded POI; raid maintenance 200 | Нет полного обхода мира |
| Kitchen / vat | Recipe search при dirty input; pending result, активный прогресс | Recipe cache не добавлен: избегаем stale datapack reload |
| Textile / drying / barrel | Локальные процессы, ограниченные слоты, визуальные переходы | Loaded-tick длительности сохранены |
| Gorodishche / villages | Native structure sets/jigsaw/heightmap/processor, authored NBT | Частота и шаблоны сохранены |
| Showcase / manual generation | По команде; ACTIVE jobs ограничены, tickets очищаются при unload/close | Bounded tick service только для явных заданий |
| Logging | System.out/println в src/main/java не найден | Временного production logging нет |
| Build | Isolated test sourceSets, без default broad runtime fileTree | Xaero local file заменён pinned Maven version |

Runtime-изменение: `VillageWork.validJob` сначала проверяет `level.hasChunkAt(anchor)`, затем вызывает `PoiManager.getType`. Для отсутствующего chunk POI query count меняется с 1 до 0 за вызов; для загруженного остаётся 1. Это вывод из кода, не замер времени. Registry IDs, gameplay и cooldown сохранены.

127 duplicate groups дают только 279,520 потенциальных raw bytes. Byte identity не доказывает ненужность ID; все сохранены. Source-art/archive candidates в runtime resources отсутствуют. Lossy преобразования не выполнялись.

Большие поселения, bosses и varied-terrain worldgen требуют отдельной игровой приёмки. Результаты headless checks в RELEASE_PREPARATION_1.2.5.md не заменяют полный игровой QA.
