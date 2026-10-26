# #276 L1: разрешённое исполнение 26.10.2026

Baseline `fe3d2752318ed40a00fbfdd79cf050a2b3c99c4b`, ветка hell, дерево чистое. Владелец прямо разрешил реализацию L1 для шести сооружений; старый audit-only scope завершён и не ограничивает это новое поручение. Minecraft/GameTest/server/PolyMC не запускать; установленный 2.0.2-HELL сохраняется.

Игровой путь: книга/обычный поиск существующих структур → зачистка без OP → найти финальную комнату/палатку главаря/дом атамана → открыть существующий главный контейнер → получить ограниченный дополнительный roll, использовать ресурсы/снаряжение/книги/руны через существующие системы. Встречаемость структур и боссов не меняется.

| REQUIREMENT | REAL_SOURCE | PLAYER_PATH | TEST | EXPECTED |
|---|---|---|---|---|
| Одна главная награда | KurganArchitecture finalRoom,index0; CampPiece senior(small)/leader(medium); LargeCampPiece ataman_house | Дойти до существующего главного контейнера | Actual pure KurganLayout 20k seeds/tier; actual NBT markers; routing helper | Ровно одна main table на сооружение, существующее число контейнеров |
| Материалы/руны/снаряжение | Новые namespace loot_table/rewards276/*; реальные item IDs/recipe outputs, vanilla pinned jar | Получить → текущие crafting/equip/anvil/rune handlers | Recursive closure, expected/Pany, 20k samples ×6; item/enchantment/tag schema audit | Без III/IV/story/неработающих новых украшений; craftable I–II приоритет |
| III: scrap15%, enchanted diamond armor3% | Отдельные pools только main kurganIII, rolls1 | Финальный ларец | Exact probabilities, max-count, all containers simulation | Один scrap с15%; одна enchanted diamond armor piece с3% на сооружение |
| III: нет сырьевого железа | Scoped copies семи V2 role tables для III, iron resources→gold | Остальные контейнеры III | Closure всех tables и count profiles | iron_nugget/iron_ingot/iron_block/raw_iron=0; железное снаряжение разрешено |
| Существующий большой лагерь | Сохранённые forge/warehouse/nightingale; cache с craftable replacements legacy rune pools | Соловей/тайник + главный дом | Whole-site baseline25.16%any rune → target35%; не плюс35% | Корректный малый дополнительный roll; монеты неизменны |
| Старые saves | Existing filled/processed flags и hasLootTable/nonempty guards | Старые/частично размещённые структуры | Source guard preservation; runtime NOT_RUN | Никакой миграции/дозаполнения/замены сохранённого inventory/table; только новые назначения |
| Совместимость/сдача | Java21/NeoForge21.1.255, production JAR | Отдельный owner playtest | clean build/resources/package/source gates | OPEN/OWNER_PENDING; main, installed PolyMC и другие структуры без изменений |

Цели предварительного L1: whole rune P: лагеря5/20/35%, курганы8/25/45%; perunite лагеря0/8/25%, курганы0/12/30%. Целые редкие некрафтовые руны исключены; fragments flight/rare_protection допускаются отдельным низким roll после проверки восстановления3→1 и существующих handler limits. Для новых дополнительных материалов/книг/железного снаряжения используются малые конечные rolls только в main, предварительный расчёт до записи production файлов. Netherite за пределами курганаIII=0. Все точные количества, источники и before/after публикуются в profiles/probability reports.
