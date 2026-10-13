# Регистры, имена, пути — сверить с текущим modid

Подставить текущий `modid` из проекта, в roadmap исторически `slavicmyths`; НЕ создавать параллельный modid и отдельный namespace.

## Требуемые item/entity logical IDs
- `morozko` boss.
- `morozko_ice_double` вражеский 50 HP double.
- `player_ice_double` зеркальная 15 HP копия игрока.
- `morozko_trial_totem` артефакт ритуала.
- `frost_mirror` украшение.
- `frost_resistance_rune` защитная руна.
- `frost_rune` атакующая руна.
- `morozko_staff` оружие.
- `heart_of_eternal_cold` сюжетный материал.
- `morozko_ice_bolt` и `morozko_icicle` при необходимости entity-projectiles, не предметы для крафта.

## Ожидаемые assets — логические роли, не придумывать пути при наличии соглашений
`assets/<modid>/textures/entity/morozko*.png`; `.../textures/item/{morozko_staff,frost_mirror,frost_resistance_rune,frost_rune,heart_of_eternal_cold,morozko_trial_totem}.png`; `.../models/item/...` либо актуальный 3D item renderer; `.../textures/particle/...`; `.../sounds/morozko/*.ogg`; `.../sounds.json`; `.../lang/ru_ru.json`, `en_us.json`; `.../animations/morozko...` если слой существует; book/advancement по текущему проекту.

## Обязательные проверки
- Ни одного `missing texture`, `missing sound`, перекрёстного registry namespace или untranslated item key.
- Подсказки не обещают получение в ванильном крафте, если на деле only boss reward.
- Зимняя модель скина/вооружения не использует network access для собственного копирования инвентаря.
- Проверить существующие `registry ID` и сохранения при добавлении новых, никогда не переименовывать старые без миграции.
