# Книга сказаний — контент, структура, полнота охвата

## Иерархия данных (строго)
`partId -> chapterId -> sectionId -> entryId -> stages[]`. ID с namespace `slavicmyths:` и стабильные при переименовании. Категории/записи в JSON/codec/data-driven registry, локализуемые; не захардкодить в GUI сотни switch-case.

Части (RU точное название и примеры подразделов):
- `tales` **Предания** → `beginnings` Старые сказания / `rumors` Слухи и молва / `great_deeds` Славные деяния. Не навязывать глобальный сюжет.
- `creatures` **Существа и духи** → `house` Домашние духи / `forest` Лесные духи / `field` Полевые духи / `water` Водные обитатели / `cursed` Проклятые и нечисть / `foes` Разбойники и противники / `ancient` Древние создания / `special` Особые персонажи.
- `people` **Люди и поселения** → `villages` Деревни / `gorodishcha` Городища / `bandits` Разбойничьи стоянки / `chud` Чудь и подземный город / `travelers` Особые жители.
- `places` **Места и тайны** → `shrines` Капища / `mounds` Курганы / `underground` Подземелья / `swamps` Болота / `rare` Редкие места. Различать доступные обычному worldgen и создаваемые командой/картой; не обещать обнаружение недоступных структур.
- `crafts` **Ремёсла и еда** → `kitchen` Кухня / `food` Блюда / `alchemy_ritual` Ритуалы / `runes` Руны и кузнечное дело / `gear` Снаряжение / `farming` Хозяйство.
- `nature` **Звери и растения** → `wildlife` Животные / `fish` Рыба и рыболовство / `berries` Ягоды и травы / `crops` Посадки.
- `quests` **Поручения и поиски** → `active` Текущие / `rumors` Слухи / `locations` Области поиска / `completed` Завершённые. Отображать только реально существующие задачи, а не фиктивные патчи.
- `deeds` **Деяния и достижения** → `bandits` Разбойники / `spirits` Духи / `mounds` Курганы / `chud` Чудь / `settlements` Поселения / `crafts` Ремёсла / `nature` Природа / `bosses` Сильные противники. Читаем из настоящих advancement IDs.

## Записи — формат
`{id, part, chapter, section, entryType, registryLink?, image?, icon, initialVisibility, unlockCriteria[], rumorKey, discoveredKey, studiedKey, verifiedKey, linkedAdvancements[], linkedEntries[], hintProvider?}`. Separate dynamic per-player `{stage, seenAt, verifiedAt, locationHintId?, unread}`.
Stage order: `UNKNOWN -> RUMOR -> DISCOVERED -> STUDIED -> VERIFIED`. `DEFEATED` is separate boss-completion flag; `STUDIED` allowed for neutral non-killable NPC. Promotion monotonic; rereads cannot auto-promote. Gaps handled by showing most-recent relevant text without silently auto-unlocking former stages.

## ОБЯЗАТЕЛЬНАЯ полнота (требование к аудиту)
Codex extracts the REAL registries of **all** custom entities, blocks, items, foods, stations, worldgen placed features, structures, recipes, bosses and important interaction systems. Create `docs/verification/bayun-book-final/CONTENT_COVERAGE.csv` with columns `id,kind,display_name_ru,book_entry_id,trigger,stage,verified` generated/audited against actual 1.6 checkout. Every meaningful entity and user-facing mechanic must have at least one encyclopedia page. Group repetitive item families in a single page when individual page would be redundant; but explicitly reference full set in metadata. Never omit old creatures just because their textures are outdated.

Named examples whose EXISTENCE/ID must be verified against repo: Домовой, Леший, Кикимора, Полевик, Полудница, Банник, Овинник, Игоша, Водяной, Русалка, Болотник, Баба-яга, Кот Баюн (new), Серый Волк, Жар-птица, Соловей-разбойник, Тугарин, Лихо, Оборотень и существующий Волколак (если оба имеются), Чудь, Земляной дух, Сивка-Бурка, хищники/животные, кусты/ягоды, курганы, лагеря, городища, кулинария, кузница, руны, капища, алтарь, классы/снаряжение и подтверждённые структуры. For a name not found, do NOT create fictitious entry with guaranteed presence; record absent in audit and exclude from game until existing. Do not confuse project roadmap plans with finished content.

## Индивидуальная запись — плотность
- initial rumor: 45–95 Russian words, folkloric but accurate hints (no misleading promise that structures or items are in game).
- discovered: 45–100 words, tell where found/rough behavior; do not give precise counterplay yet.
- studied: 60–130 words about real behavior, interactions/conditions and notes.
- verified: 80–160 words plus structured blocks for actual attacks/loot/limitations where applicable, with live source of balance facts rather than hand-written stale numbers.
- recipes link to real recipe display; when gameplay has no crafting GUI show discoverable instructions not fake buttons.
- use Unicode Cyrillic; no generic cloned sentence across 30 entities, no empty pages or text “TBD”. Entries should be concise but genuinely useful.

## Narrative example (localized actual text in RU_TEXTS.md)
Sоловей: initial rumor exaggerates destruction of cottages by whistle; verified shows actual whistle wave and real loot after winning. Баюн: rumor warns of song and milk/fish; discovered confirms gifts and 15-block danger radius; friend grants conversational context but no invented bogatyr coordinates before 1.7.

## Migration and external book copies
Treat item as GUI access token. Two legitimate copies (creative/manual reproduction) show identical progression. Never store per-player quest flags solely in item NBT. At minimum opening book calls server sync, contents UI are safe client snapshots, updates sync only on change.
