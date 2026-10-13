# Русский язык

Новые строки UTF-8 JSON parse, RU/EN key parity, no U+FFFD, CP1251→UTF8 mojibake check: PASS. Новых ключей: 1315. Проверка production JAR, не только source. «Книга сказаний», «Кот Баюн», восемь названий разделов и четыре уровня знаний используют native Minecraft font; текст не вшит в PNG. Заголовки и описания из actual Components, перенос через font metrics; scissor/scroll для длинной страницы. EN — краткое механическое описание каждого нового ключа.

Четыре ранее повреждённых ключа исправлены ровно обратным CP1251→UTF8 преобразованием: `entity.slavicmyths.podzemnik`, `podzemnik_miner`, `kladovik`, `large_podzemnik`. Другие прежние locale значения побайтно/по ключам сохраняются. Кузя остаётся «Домовёнок Кузя», его пять оригинальных мультфильмовых файлов не изменены. Русские названия vanilla loot взяты из локального установленного asset index17; минимальный used-name snapshot в `docs/bayun/vanilla-ru-1.21.json`.

58 индивидуальных RU living profiles: density PASS (`content-density.json`). Body — игровая интерпретация; сведения об API/loot/HP взяты из actual classes/registry/recipe manager. Technical actors описаны у владельца и не названы новой мифологией.

**Реальные скриншоты русский GUI 1366×768 scale2/3/4: NOT_RUN.** Клиент автоматически не запускался по прямому отказу пользователя. Папка для ручных captures: `docs/verification/bayun-book-final/screenshots/` (пока пустая). Русский текст в коде проверен, его реальный рендер и читаемость на этих размерах не приняты. Поэтому status кандидата не объявляется окончательным release complete.
