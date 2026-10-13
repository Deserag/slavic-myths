# Russian and old-world compatibility release gates

## Prior Russian breakage — MUST explicitly prevent
The prompt language is English, but the mod's complete new UI/text MUST have a high-quality Russian base. Do not replace text with auto-transliterated Latin, pseudo-Cyrillic, gibberish, EN-only, missing keys, question-mark glyphs or `\u003F` for letters. Code/translations saved UTF-8 (no mojibake). `ru_ru.json` actual content should include all dialog choices, fatal death message, lore stages, page headings, subtitles, achievements. `en_us.json` keys must match with accurate English meaning, but if imperfect EN text never break RU.

### Check scripts / gates to add
- Parse both json languages with actual UTF-8. Validate key uniqueness using duplicate-aware JSON parser before Java `JsonObject` swallow duplicates.
- Collect ALL new usage keys: `Component.translatable`, advancement display translatables, narrator and subtitles, any `item.slavicmyths.*` new paths; compare ru_ru and en_us sets, fail on missing keys.
- Test glyphs Cyrillic `А-я Ёё`, Russian «ёлочки», em dash, punctuation. Actual Minecraft font render with 16:9, 1366×768, GUI scales 2,3,4; inspect screenshots.
- Warn on any player-visible hardcoded English string, untranslated test `???` absent purpose or gibberish. Check no text invalid UTF-8/`�` U+FFFD. UI tooltip must wrap.
- Take backup/diff of existing RU keys and compare before/after; existing strings should not disappear except deliberate renamed paths with all references migrated.
- At least one full integration test on old test save with already earned SlavicMyths advancement and Baba Yaga quest, book both present and absent. Book new first-grant flag on legacy saves needs careful migration: if already previously issued a book, MUST NOT give second. If cannot prove previous grant from saved signals, choose safe non-duplication/migration policy documented for old players rather than grant unlimited.

## Naming and punctuation (exact)
«Книга сказаний», «Кот Баюн», «Баба-яга», «Соловей-разбойник», «Лихо Одноглазое», «Область поиска», «Предания», «Существа и духи», «Люди и поселения», «Места и тайны», «Ремёсла и еда», «Звери и растения», «Поручения и поиски», «Деяния и достижения», «Услышано», «Встречено», «Изучено», «Проверено». Do not write `KataboYuna`, `BabeGIS` in in-game strings: those were voice-to-text artifacts, not character names.

## Acceptance samples
`Кот Баюн предупреждающе рычит.` / `Подношение принято. Кот Баюн благосклонно прикрыл глаза.` / `Слишком далеко, чтобы погладить Кота Баюна.` / `Откройте Книгу сказаний, чтобы прочесть новое предание.` / `Возможное местонахождение: хвойный лес к северо-востоку.` / `Кот Баюн начал свою чарующую песнь!` / death `Игрок был растерзан Котом Баюном.` where placeholder resolved by TranslationComponent.

## Save migration and respect user worlds
Do not overwrite player advancements files in existing world. Do not reset `baba_yaga` quest schema. On versioned data deserialize unknown books, recognize old book items via actual registry ID not item localized name. Test item placed in shulker/chest / dropped doesn't cause infinite auto grant. Survivor who later loses book continues acquiring stats; cannot get a new free book at every join.
