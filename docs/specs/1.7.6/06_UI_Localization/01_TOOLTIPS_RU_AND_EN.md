# Full localization / tooltips / key names

All 19 names must be in ru_ru.json and en_us.json. Use keys `item.<actual-modid>.<item-id>`. Helper descriptions use `item.<modid>.<id>.desc`, `.source`, `.cooldown`, `.slot` where useful. Never use transliteration in Russian UI. Use proper Russian spellings: Алёша Попович, Добрыня Никитич, Илья Муромец, Идолище Поганое, Маринка, Ским-зверь, Картаус Рыжий Ус, Калин-царь.

UI illustrations:
- Reward claim view (existing Book of Tales or existing hero dialogue): name 1 line, item 32x32/64x64 icon, role descriptor 1 line, status "Доступно" / "Получено" / "Победите ...", action "Получить награду". Never display a fake clickable button if existing UI cannot commit claim; use current dialogue interaction instead.
- Rune Anvil: name, permitted slot, numerical effect, cooldown, compatibility; invalid slot text "Нельзя установить в этот предмет".
- Equipment tooltip: stat/effect after actual modifiers in one to three lines; no 12-line lore wall.

Damage numbers: use `×1,5` and `×1,75` for RU; `x1.5` and `x1.75` for EN. Health damage measured in HP (2 HP = 1 heart); distinguish HP from hearts.

Accessibility: preserve vanilla captions/subtitles, no uncaptioned important proc. Low-particles preference must not hide warnings; audio plays only through supported positional sounds.
