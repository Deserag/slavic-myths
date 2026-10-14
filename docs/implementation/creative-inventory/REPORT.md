# Творческий инвентарь — итоговый отчёт, 2026-10-14

**COMPLETE_WITH_LIMITATIONS**. 10 вкладок, 849 предметных registry ID по независимому
разбору текущих регистраций; 835 видимых ID / 837 стеков, 14 намеренно скрытых.
Чистая сборка и offline/resource/JAR gates PASS. Minecraft не запускался:
**0 клиентов, 0 серверов, 0 GameTest**. Визуальная и загруженная registry-проверка NOT_RUN.

## Git и границы изменений

Начальная ветка `runs`, HEAD `b76160800223267abc04e7c6d46812dfba8976dc`. Итоговая ветка `pred-relise`, тот же HEAD.
Предыдущие staged/unstaged/untracked изменения сохранены. Commit, push и merge не выполнялись.
Registry ID предметов и блоков, рецепты, loot, теги, характеристики, руны и механики
**этой задачей не менялись**. Девять прежних tab ID сохранены, добавлен только `weapons`.
Старые числовые категории 0–8 сохранены для существующих потребителей API; weapons = 9.
Порядок интерфейса задаётся отдельно: 0,1,2,3,9,4,5,6,7,8. Ванильный UI не изменён;
пагинацию/размещение выполняет установленный NeoForge.

Версия первоначального кандидата была 1.9.7.1. Параллельная задача оружия подняла
общую версию до **1.9.7.2**, её правки сохранены и включены в окончательный JAR.
Это не изменение оружейных механик в рамках инвентаря: принадлежность параллельных
изменений и 34 сторонних переводов зафиксирована в [PARALLEL_CHANGES.json](PARALLEL_CHANGES.json).
Прямой совместный clean удалял результаты сборки; финальный clean build выполнен
с отдельным build directory через `tools/creative-build.init.gradle`, затем проверенный
production JAR экспортирован в `build/libs`. Все 7735 production inputs оставались
побайтно неизменными на протяжении финальной сборки и проверок.

## Вкладки

| Registry path | RU название | ID | Стеки | Иконка (slavicmyths) |
|---|---|---:|---:|---|
| slavicmyths | Основное | 3 | 3 | `lore_book` |
| resources | Материалы и трофеи | 114 | 114 | `silver_ingot` |
| farming | Растения и земледелие | 48 | 48 | `rye_seeds` |
| food | Еда и напитки | 94 | 94 | `karavai` |
| weapons | Оружие | 68 | 68 | `perunite_sword` |
| tools | Инструменты | 19 | 19 | `sickle` |
| armor | Броня и снаряжение | 42 | 42 | `perunite_chestplate` |
| decor | Строительство и декор | 216 | 216 | `apple_planks` |
| magic | Магия и ритуалы | 166 | 168 | `thunder_stone` |
| mobs | Существа | 65 | 65 | `domovoy_spawn_egg` |

EN: Main; Materials & Trophies; Plants & Farming; Food & Drinks; Weapons; Tools;
Armor & Equipment; Building & Decoration; Magic & Rituals; Creatures.
Две дополнительные строки — сохранённые варианты `blank_rune_1` (iron/gold)
и `blank_rune_2` (diamond/perunite). `blank_rune_3` имеет один perunite-вариант.
Обычные стеки этих ID не добавляются повторно, варианты вставлены у своего уровня.

## Инвентаризация и устройство

[INVENTORY.json](INVENTORY.json) и [INVENTORY.csv](INVENTORY.csv) содержат полный
`registry ID → категория → порядок → группа → текущая исходная регистрация`.
`tools/creative_source_inventory.py` независимо раскрывает literal-регистрации,
действующие helper-вызовы, циклы, enum и составные семейства текущего Java-кода;
полученные 849 ID точно совпали с TSV. Историческая документация не используется
как источник списка в финальном проверочном скрипте. Это статическая сверка,
не загрузка реестров NeoForge.

`CreativeCatalog` — неизменяемые данные, категории и стабильный порядок, без
Minecraft/client зависимостей. `ModItemGroup` собирает фактический namespace
`slavicmyths` из BuiltInRegistries.ITEM при построении вкладок: предметы из
внешних модулей не теряются из-за неполной карты ModItems. Неизвестный ID получает
типовой fallback, ставится после известных, однократно пишет предупреждение.
Алфавит используется только для неизвестного хвоста. Известные ID сортируются
по явному каталогу. Скрытые предметы описаны отдельно; повреждённый каталог и
дубли ID отклоняются с ошибкой. Нет world/player tick polling и новых client imports.

## Исправленная классификация и порядок

- Оружие выделено из инструментов: мечи/кинжалы, булавы/чеканы, боевые топоры,
  цепы, копья, двуручное оружие/косы, метательные семейства, луки/арбалеты, посохи.
  Учтены `noon_sickle`, `karachun_scythe`, `poloz_whip`, `morozko_staff`,
  `serpent_crossbow`, `three_breath_crossbow`, все текущие трофейные варианты.
- Сохранены семейства stone → gold → iron → silver → diamond → perunite → netherite
  и канонические исключения `spear`, `silver_spear`, `flail`. Проверены реальные
  MATERIALS/Tier, SpearItem/FlailItem, скорости/урон, рецепты и добыча. Это порядок
  доступности и применения, а не утверждение монотонного DPS: gold имеет малую
  прочность/высокую зачаровываемость; perunite даёт 3 sockets при меньшем базовом
  уроне, чем diamond. Древние находки и их восстановленные формы оставлены рядом.
- Рабочий `perunite_axe`, `butchering_knife`, серп/мотыга/лейка и `rune_chisel`
  находятся в инструментах. Боевые battle/thunder axes — в оружии.
- Perunite/dragon/poloz: целые комплекты helmet → chestplate → leggings → boots.
  Одежда раньше брони; уникальные части, пояса, щиты и защитные аксессуары после.
- Мука, крупы, тесто и brewing-ингредиенты перенесены в еду; hearth/kitchen_table,
  textile/brewing/industry станки — в функциональное строительство.
- Материалы оружейных частей сгруппированы с обработанными компонентами;
  обычные трофеи предшествуют элитным и легендарным.
- Ягоды/овощи показаны однократно в растениях, рядом с формами посадки/сбора;
  редкие травы и fern_flower расположены перед удобрением.
- Apple/linden/rowan/willow/pine/darkened и два kurgan stone family не разорваны;
  мебель сгруппирована по древесине, бытовые контейнеры по назначению.
- Числовые руны I/II/III проверены по RuneBalance, MiningRunes и typed recipes;
  IV — после них. Legacy/rare/story рунам без явного числового tier не приписан
  выдуманный уровень. Пять актуальных новых рун присутствуют; система рун не менялась.
- Мирные/нейтральные яйца предшествуют hostile/elite/miniboss/boss. Жар-птица
  возвращена к нейтральным существам вместо первого места перед домашними животными.

## Намеренно скрыто

`bear_cub_spawn_egg`, `doe_spawn_egg` — сохранённое правило семейных яиц.
`filled_pitcher` — служебный напиточный стек.
`light_beer_wort`, `dark_beer_wort`, `hopped_beer_wort`, `honey_beer_wort`,
`forest_beer_wort`, `thunder_beer_wort`, `veles_dark_wort`, `witch_berry_wort`,
`kvass_wort`, `mead_must`, `cider_must` — сохранённые служебные brewing batches.
Из реестров ничего не удалялось. Held/icon/pulling/charged/pose/bottled модели —
варианты отображения, не новые предметные регистрации. Отсутствующие яйца Кузи,
Морозко, Карачуна и Полоза не создавались по документации.

## Спорные случаи и ограничения

Щиты отнесены к защитному снаряжению, а не оружию. Ритуальный нож — магия;
butchering_knife — рабочий инструмент, хотя технически SwordItem. Карта глубин —
основной информационный раздел. Напиточная тара — еда/напитки; лечебные и ритуальные
эликсиры и бутылки дыхания Горыныча — магия. Защитные goggles/belts/quiver —
снаряжение; кольца/амулеты с магическими эффектами — магия. Капища и ритуальные
станции — магия, декоративная каменная резьба и двери — строительство.
Это однозначные решения каталога без межвкладочных дублей; баланс не оценивался
игровыми измерениями. Будущие незнакомые custom-классы требуют ручной проверки fallback.

## Проверки и установка

`gradlew.bat -I tools/creative-build.init.gradle clean build`: **PASS**, 48s,
Java21 / NeoForge21.1.255 / Minecraft1.21.1. Ранняя компиляция общего checkout
сообщала 100 warnings; они не исправлялись в рамках организации инвентаря.
Финальная compileJava использовала валидированный Gradle cache.
Java-парсер: **1710 checks PASS**, только JDK и CreativeCatalog, без Minecraft bootstrap.
Независимый source/resource/JAR аудит: **24145 checks PASS**. Проверены все
локальные model parents/textures/overrides, зарегистрированные item models,
JSON/PNG, переводные ключи, упаковка всех production resources/classes, CRC,
метаданные и исключение tools/smoke/verify/GameTest fixtures.
`git diff --check` по собственным изменениям PASS.

Production: `build/libs/slavicmyths-1.9.7.2.jar`.
SHA256: `b24727af8d8a016a4f12e258e0b31373eec09688bc4d3fa47a78d9c7a1bf1b3d`. [GATES.json](GATES.json), [BUILD.log](BUILD.log), [OFFLINE.log](OFFLINE.log).

PolyMC: **PASS**, установлен тот же JAR с проверкой SHA256 и отсутствия дубликатов.
Имя: `Slavic Myths 1.9.7.2 - NeoForge 1.21.1 TEST — Патч оружия — Исправления существ — Творческий инвентарь — Исправления оружия`. Старый JAR и instance.cfg сохранены в
`C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.slavicmyths-backups\pre-creative-inventory-20261014-141633`. Остальные **457 файлов профиля** сохранены
побайтно. Имя и номер версии обновлены при первой установке; последняя установка
изменила 0 конфигурационных файлов. Миры и старый
1.16.5 не затронуты. [POLYMC_INSTALLATION.json](POLYMC_INSTALLATION.json).
Исходная установка 1.9.7 и её прежний instance.cfg также сохранены в
`C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.slavicmyths-backups\pre-creative-inventory-20261014-140758`; SHA256 исходного JAR проверен отдельно.
Установщик не запускает игру. Все игровые/визуальные проверки NOT_RUN по запрету пользователя.

## Файлы этой задачи

- `src/main/java/org/slavicmyths/registry/ModItemGroup.java`
- `src/main/java/org/slavicmyths/registry/CreativeCatalog.java`
- `src/main/resources/assets/slavicmyths/creative_catalog.tsv`
- `src/main/resources/assets/slavicmyths/lang/ru_ru.json`
- `src/main/resources/assets/slavicmyths/lang/en_us.json`
- `gradle.properties`
- `tools/gardens_resources.py`
- `tools/verify_gardens.py`
- `tools/creative_tab_names.py`
- `tools/creative_source_inventory.py`
- `tools/creative_checkpoint.py`
- `tools/verify_creative_inventory.py`
- `tools/install_creative_inventory.ps1`
- `tools/creative-build.init.gradle`
- `tools/creative-tests/CreativeCatalogChecks.java`
- `docs/PROJECT_STATUS.md`
- `docs/ROADMAP.md`
- `docs/ARCHITECTURE.md`

- `docs/implementation/creative-inventory/` — отчёт, полные JSON/CSV ведомости,
  результаты gates/установки, логи и перечень сохранённых параллельных правок.

## Краткая ручная проверка (не выполнялась)

1. RU/EN: десять названий/иконок, порядок и навигация вкладок NeoForge.
2. Оружейные/броневые/деревянные семейства и порядок I–IV в реальном GUI.
3. Все 835 видимых ID, пять typed blank rune stacks; отсутствие случайных дублей и fallback warnings.
4. 14 скрытых ID отсутствуют во вкладках, остальной поиск/доступность работают.
