# #46 — точная интеграция V2

Архив владельца: SHA256 `98a01d34281371c40edfb5928a37f2538fbae78e140fa97bd148b348a9ae457b`.
12 entries:11 независимых RGB16×16 PNG и embedded manifest. ZIP CRC, каждый
embedded SHA256 и внешний canonical manifest согласованы. В production/JAR
установлены точные PNG-байты без декодирования/перекодирования и художественных
изменений. Canonical art/kurgan-16-approved/approved.zip теперь V2; отдельные
двусмысленные копии V1 не добавлялись, V1 остаётся в истории Git.

Изменены только эти11 ресурсов в src/main/resources/assets/slavicmyths/textures/block/:

- `bog_green_kurgan_stone.png`
- `bog_green_kurgan_cobblestone.png`
- `cracked_bog_green_kurgan_cobblestone.png`
- `mossy_bog_green_kurgan_cobblestone.png`
- `pale_blue_kurgan_stone.png`
- `pale_blue_kurgan_stone_variant.png`
- `pale_blue_kurgan_cobblestone.png`
- `cracked_pale_blue_kurgan_cobblestone.png`
- `mossy_pale_blue_kurgan_cobblestone.png`
- `burial_stone_top.png`
- `carved_burial_stone.png`

Оригинальные art/kurgan-0.9.6/source/provenance и runtime-256-backup сохранены
побайтно. Registry ID, рецепты, source Java, модели/blockstates/item models,
механика, баланс и структуры относительно main722eabc2 не менялись.

## Creative

Офлайн-проверка actual catalog/registration source/ModItemGroup/моделей/переводов:
повторных IDs в creative_catalog.tsv нет, RU/EN отображаемые названия всей
голубой группы уникальны. ModItemGroup перечисляет entries BuiltInRegistries.ITEM
и добавляет один default stack для этих блоков. Зарегистрированы разные предметы:

| Item ID (namespace slavicmyths) | Боковая текстура |
|---|---|
| pale_blue_kurgan_cobblestone | pale_blue_kurgan_cobblestone |
| cracked_pale_blue_kurgan_cobblestone | cracked_pale_blue_kurgan_cobblestone |
| mossy_pale_blue_kurgan_cobblestone | mossy_pale_blue_kurgan_cobblestone |

Все три сохраняют общую burial_stone_top на верхних/нижних гранях, что могло
усиливать визуальное сходство V1. Боковые формы V2 различны; все11 pixel buffers
различны. pale_blue_kurgan_stone_variant — weighted model камня, отдельного
block/item ID нет. Без исходного скриншота с tooltip/IDs нельзя утверждать,
какие именно два stacks видел владелец. Нужные блоки не удалялись и не скрывались.
Это не runtime-проверка Creative. Подробные ссылки/имена: [CREATIVE.json](CREATIVE.json).

## Генераторы и сборка

- install_kurgan_16.py требует конкретный V2 ZIP SHA, revision, точный набор12
  entries, embedded manifest и11 per-file hashes. V1, даже переименованный в V2,
  отклоняется ДО записи ресурсов.
- kurgan_texture_import_096.py делегирует V2 installer без legacy fallback.
- kurgan_085.py сначала восстанавливает V2 и пропускает старую генерацию approved
  core textures/models/states. Остальные исторические ветки не переписывались.
- Прямой и legacy entrypoint идемпотентны; все resources/оригиналы после них
  побайтно прежние. Core loop085 проверен отдельно с перехватом записей:
  для approved материалов процедурной генерации нет. Полный исторический
  генератор остальных систем не запускался. [GENERATOR.json](GENERATOR.json).

Java21 `gradlew.bat clean build` PASS; compileJava FROM-CACHE.
`python tools/verify_kurgan_16.py`:22289 PASS;92 models, parent/texture variables,
UV0..16, верх/низ, blockstate/item references,6804 resource parity, JAR CRC,
production-only Java21 classes,11 точных16×16 PNG. [BUILD.json](BUILD.json),
[GATES.json](GATES.json), [атлас фактических V2 PNG](installed-atlas.png).
Ступени/плиты сохраняют side/top/bottom bindings, стены — собственную wall
текстуру. Новых технических несовместимостей ссылок/UV не выявлено.
Офлайн-проверка не является игровой приёмкой швов/освещения/восприятия материалов.

Версия1.9.7.8 не менялась. PolyMC, другие моды и пользовательские миры не трогались;
кандидат V2 собран только в build/libs и отправлен в main. Minecraft/GameTest/
PolyMC/server launches **0**. Установленный прежде JAR V1 намеренно не обновлялся.
Issue46 остаётся открытой до решения владельца по V2. Временные regression
fixtures удалены; новых веток/worktree/постоянных рабочих копий нет.
