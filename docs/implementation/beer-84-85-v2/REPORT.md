# Единая задача #84/#85 — ревизия1.9.7.25

IN_PROGRESS / OWNER_PENDING. Результат1.9.7.24 отклонён. Текущая ревизия не принята владельцем.
База: актуальный origin/main f07225da6f4c24efcda38730995e08bd7b2c70ba,
получен fast-forward без сброса/стеша/перезаписи предыдущих локальных правок.
Предыдущее опубликованное состояние меняло только docs/design; production base совпадал.

## Изменения

`tools/beer_models_125.py` теперь генерирует для8 сортов одну геометрию:
исходные стенки, обручи, дно и связанная ручка без изменения вершин/пропорций.
Два внутренних cuboid содержимого имеют одинаковый размер для всех сортов:
liquid до y9.65; плоская поверхность y9.65–9.72 при борте y10.
Никакой пены снаружи, сколов, наростов, задних/боковых табличек или бутылок рядом.
Плоская поверхность использует полный UV0–16, чтобы цвет и пена были видны сверху.
Декор не используется. Рисунок пены общий, различия сосредоточены в двух цветах.
Остальные модели ёмкостей сохранены, включая бутылки, кувшины и nonalcoholic drinks.

| Registry ID (slavicmyths:) | Напиток | Пена |
|---|---|---|
| light_beer_mug | #e7bc48 золотистый | #fff0bf |
| dark_beer_mug | #794928 коричневый | #d4b68b |
| hopped_beer_mug | #b2ac39 золотисто-зелёный | #e5e2a5 |
| honey_beer_mug | #d68b24 янтарный | #ffdaa0 |
| forest_beer_mug | #56844a травянистый | #c0d3aa |
| thunder_beer_mug | #62b7d2 голубоватый | #dcf5fa |
| veles_dark_beer_mug | #312437 насыщенный тёмный | #93829e |
| witch_berry_beer_mug | #a83c67 ягодный | #edabc6 |

Все PNG16×16 с alpha0/255.8 mug JSON,8 liquid PNG,8 flat-surface PNG и16 совместимых
mug/bottled sprites. Старые rejected mark PNG удалены только из текущих ресурсов;
их исходники сохранены в before/. Бутылочные JSON возвращены к исходной модели
путём исправления существующего генератора, без отката проекта.
`BeverageItem.filled` по-прежнему выбирает бутылку custom_model_data1;
пивные кувшины не поддерживались и не добавлялись. Registry/recipes/food/NBT не менялись.
Генератор по-прежнему вызывается из общей brewing цепочки; его отдельный повтор
проверяет совпадение ВСЕХ production ресурсов, а не только изменённых PNG.

## Фактическая логика опьянения

Существующий attachment `drink_overload`, CompoundTag `Points`/`LastDrink`.
Никакой второй системы или клиентского счётчика.

| Обычные дозы без спада | Отрицательные эффекты |
|---|---|
| 0–3 | Нет алкогольных штрафов |
| 4 | Путаница10с |
| 5 | Путаница20с + слабость30с |
| 6 | Путаница30с + слабость45с + замедление30с |
| 7+ | Прежняя опасная ступень: путаница40сII, слабость60сII, замедление45с, яд8с |

Обычные сорта/медовуха/сидр дают1 дозу; thunder/veles/witch дают2 и сохраняют прежние
магические множители/усилители.2 особые кружки или1 особая+2 обычные достигают4 доз.
Каждое завершённое питьё32тика учитывается; saturating add предотвращает overflow.
Decay: минус1 за каждые1200 игровых тиков с LastDrink, считается перед новой
алкогольной порцией. Новая порция обновляет LastDrink; эффекты конечны и без питья
не refresh-ятся. Квас/морс/сок нагрузку и LastDrink не меняют. Качество влияет на
прежние бонусы; испорченный напиток даёт пищевое отравление независимо от4 доз.
RU/EN tooltip и action bar после питья показывают порог/серверную нагрузку.
Молоко не обнуляет скрытую нагрузку. Остановленное игровое время не равно wall clock.

## Сохранение, смерть, перезаход

Проверяется actual CompoundTag.CODEC и NbtIo binary roundtrip, затем actual
BrewRules.decay/addLoad над загруженным состоянием. Это офлайн-проверка формата,
НЕ перезаход реального игрока. Неизменный Brewing.LOAD сериализуется и не включает
copyOnDeath; дополнительно проверяется default false в установленной NeoForge21.1.255.
Смерть сбрасывает attachment по исходному clone/death контракту NeoForge.
Actual onPlayerClone передаёт event.isWasDeath() в copyAttachmentsFrom, который
при смерти копирует только типы с copyOnDeath=true. Реальные death/relog
в мире не заявляются проверенными.

## Доказательства

- `eight-beers-slots.png` — ЕДИНОЕ итоговое превью8 кружек: native16px в18px слотах,
  тот же ряд GUIscale3 nearest-neighbor и actual16px top textures для проверки цвета.
- `source-before-after.png` — одинаковая source-камера для rejected24/исправленной25.
- `before/assets/slavicmyths/` — фактические модели/текстуры rejected24.
- `tools/preview_beer_84.py` — CPU-проекция production JSON с их textures/UV/display.
  Это не Minecraft ItemRenderer, не игровая рука и не скриншот запущенной игры.
- `checks.json`, `build.log`, `jvm-tests.txt` — результаты gates.
- `build-source-sha256.json` — полный manifest production source/resource/Gradle файлов
  фактически собранного рабочего дерева. JAR включает прежние локальные #73–75;
  отдельный scoped commit пива не выдаётся за весь исходник этих несохранённых правок.

## Матрица

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Актуальный main, сохранение пользовательских изменений | FF f07225da, scoped diff/commit | PASS |
| Одинаковая целая форма всех8 кружек | Structural vertices/display exact match upstream, verify_beer_84_85.py | PASS |
| Различимые поверхности без символов/потёков/наростов |8 top textures + native slot preview + UV/element checks | PASS |
| Ёмкости/ID/NBT/override/recipes сохранены | Source methods/unchanged bottles/other containers, gate | PASS |
| Повторяемый генератор, JSON/UV/PNG/JAR parity | checks.json | PASS |
| Порог4, усиление5–6, strong/mixed/repeat/decay | actual production JVM arithmetic | PASS |
| Формат сохранения и decay после загрузки | actual CompoundTag.CODEC/NbtIo + production arithmetic | PASS |
| Смерть: установленная copyOnDeath политика | неизменный production builder + NeoForge default-field check | PASS |
| Java21 clean build, resource/JAR/class gates | build.log, checks.json | PASS |
| Реальные смерть/перезаход/MP/клиентское отображение | Запуск Minecraft запрещён | NOT_RUN |
| Ручная приёмка владельцем | Нет явного решения | NOT_RUN |

Minecraft/PolyMC/GameTest/server запусков0. POLYMC_NOT_INSTALLED. Issues OPEN.
Коммит только пивного пакета по прямому запросу; push не запрошен.

## Результаты gates

Java21 clean build + verifyBeerOffline: PASS44с; compileJava FROM-CACHE.
7406 ресурсов и1707 Java21 классов совпали с production JAR.
SHA2561.9.7.25: `c210f054e5077d33aa85c54922c1a3c81fbbf6c16d8ea7566d16fb8443f5e397`.
Генератор повторён: byte-identical; JSON/PNG/refs/UV/override/структура PASS.
Первый запуск дополнительного native-теста обнаружил неоднозначную method reference
CompoundTag::new; тест исправлен на точную production lambda, затем полный clean
build и все gates повторены успешно. Production-код из-за этого не менялся.
