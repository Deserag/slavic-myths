# ID, локализация и логика интеграции (имена предложены, проверять существующие)

**Структуры:** `sanctuary_unfading_fire`, `abode_pure_spring` (не регистрировать новые registry ID, если в проекте уже есть функциональный эквивалент). Block/BE/structures брать из фактического API 1.16.5, не из код-примеров 1.20+.

**Предлагаемые item ID:** `sacred_ember`, `purifying_ash`, `flame_vial`, `fading_flame_charm`, `ancient_rune_fragment`, `pure_spring_drop`, `purified_water_bottle`, `spring_tear`, `purification_stone`.

**Предлагаемые state/interaction ID:** `unfading_brazier` `seal_fire_door` `seal_spring_door` `tainted_spring_node`. Все должны иметь настоящие соответствия в registry/model/blockstates/lang/drop и правильный creative visibility по назначению.

**Русские строки:** Святилище незатухающего огня; Обитель чистого источника; Священный уголь; Пепел очищения; Огненная склянка; Оберег угасающего пламени; Древние обломки рун; Капля чистого источника; Очищенная вода; Слеза источника; Камень очищения; «Огонь едва теплится. Ему нужна священная искра»; «Древние печати ещё не позволяют войти»; «Воды источника вновь чисты».

**English:** translate labels meaningfully, not machine-generated placeholder strings. Sound/loot/advancement IDs should be consistent.

Не вводить новые инвентарные слоты, переработку рунических уровней или новую global capability без нужды; использовать существующие слоты и регистрационные процессы.
