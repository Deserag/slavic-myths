# Уникальное снаряжение: дополнение текущей версии — 2026-10-08

Версия до/после: **1.3.4.5.3 → 1.3.4.5.3**. Имя 1.3.3.2 в архиве — историческое имя задания; `gradle.properties` этим заданием не правился. Minecraft 1.21.1 / NeoForge 21.1.255 / Java 21. Реализованы все шесть предметов, включая механику, модели, текстуры, локализацию, рецепты/источники и integration. Это игровая интерпретация.

## Предметы и рецепты

Все пять улучшений используют существующий оружейный верстак, `slavicmyths:armorer_shapeless`, `copy_components: true`; каждый ингредиент занимает отдельную ячейку сетки 4×4. Нового станка нет.

| Предмет / registry ID | База ×1 | Дополнительные предметы (реальные IDs) |
|---|---|---|
| Сапоги ветра / `slavicmyths:wind_boots` | `slavicmyths:seven_league_boots` | `slavicmyths:mysterious_black_feather` ×4; `slavicmyths:goat_hide` ×2 |
| Нагрудник богатыря / `slavicmyths:bogatyr_chestplate` | `minecraft:iron_chestplate` | `slavicmyths:hardened_iron` ×4; `slavicmyths:princely_seal` ×2 |
| Шлем ясного взора / `slavicmyths:clear_sight_helmet` | `minecraft:iron_helmet` | `slavicmyths:oko_likha` ×1; `slavicmyths:hardened_iron` ×2; `slavicmyths:ancient_sign` ×2 |
| Поножи неукротимости / `slavicmyths:indomitability_leggings` | `minecraft:iron_leggings` | `slavicmyths:hardened_iron` ×2; `slavicmyths:tugarinova_kozha` ×2; `slavicmyths:klyk_volkolaka` ×1 |
| Щит-оберег / `slavicmyths:ward_shield` | `minecraft:shield` | `slavicmyths:hardened_iron` ×2; `slavicmyths:warding_charm` ×1; `slavicmyths:silver_ingot` ×1 |
| Закалённое железо / `slavicmyths:hardened_iron` | Нет крафта | Воевода ×1, волхв ×1, князь ×2 — гарантированные дополнительные loot pools |

Отдельного пера-трофея Соловья в фактическом репозитории нет. Пользователь явно разрешил использовать существующее «Загадочное чёрное перо» из стана Соловья. Новый drop и правки Соловья для рецепта не добавлялись. Трофей нагрудника — существующая печать князя, высокий tier кургана. Закалённое железо — только материал, без руды/worldgen/полного armor set.

## Точные характеристики и механика

| Предмет | Armor | Toughness | Базовое KB resistance | Прочность | Максимум рун |
|---|---:|---:|---:|---:|---:|
| Сапоги | 2 | 0 | 0 | 260 | 3 |
| Нагрудник | 8 | 3 | 0.20 | 640 | 3 |
| Шлем | 3 | 1 | 0 | 330 | 3 |
| Поножи | 6 | 2 | 0.10 | 600 | 3 |
| Щит | — | — | — | 672 | 3 |

- Сапоги: постоянная скорость ×1.40; сила обычного прыжка ×1.20; горизонтальный momentum при прыжке с разбега ×1.10. В воздухе остаётся штатная физика, дополнительного обнуления momentum нет. Чистый расчёт обычной vanilla траектории без зелий: около 1.732 блока против 1.252, +38.3% высоты; это расчёт, не клиентский замер. Скорость выбрана +40%, чтобы превосходить нынешний peak Скороходов +30%; их прежний разгон не распространяется на новые сапоги. Fall damage multiplier ×0.60, без отмены падения или включения flight.
- Нагрудник: при живом HP ≤35% max HP конечный входящий урон ×0.85, KB +0.15 (в сумме 0.35). Здоровье и смена экипировки обновляют один transient modifier; повышение здоровья немедленно снимает его. Kill/void с bypasses_invulnerability сохраняет стандартное поведение.
- Шлем: **Alt + действующая клавиша общей активной руны** (по умолчанию Alt+R). Обычные R и Shift+R сохраняют прежние действия. Сервер проверяет шлем/живого игрока/КД; один локальный loaded-radius запрос 24 блока, до 512 целей. Контуры 100 тиков; КД 800 тиков. Показаны Mob и разрешённые PvP сервером игроки; исключены armor stands, spectator/dead, прочие типы entities и `slavicmyths:clear_sight_excluded`. Снимок целей привязан к UUID, ID и измерению; местная отрисовка Minecraft, без vanilla Glowing. Снятие шлема, смерть/выход/смена ClientLevel очищают отображение, не КД.
- Поножи: длительность эффектов из `slavicmyths:equipment_control` — ceil(duration×0.65), минимум 1 тик. Сейчас tag содержит slowness, class_root, disorientation; расширяется datapack без отдельной системы эффектов. После accepted сильного knockback с эффективной силой ≥0.40 добавляется KB +0.35 на 100 тиков; КД 300 тиков. Повторные толчки не продлевают окно. Снятие/lifecycle очищает временный бонус, сохраняя КД.
- Щит: после итогового accepted block с положительным поглощением шанс 25%, урон владельцу источника blockedDamage×1.25. Для снаряда — его living owner; для ближнего удара — attacker. Без владельца/самоповреждение/неудачный или отменённый блок/нулевое поглощение не отражаются. Registry damage `ward_reflection` запрещает повторное отражение; direct entity отсутствует, авторство сохраняется и оружейные on-hit эффекты не запускаются; применяются обычные правила защиты атакующего. Chance/multiplier централизованы в `UniqueEquipmentRules`.

Все пять используют существующий RuneState и наковальню: **ёмкость 3**, ранее открытые ячейки и установленные руны переносятся. Закрытые ячейки не открываются бесплатно при upgrade. Нового rune component/UI нет.

`transmuteCopy` переносит patch компонентов базы: enchantments, имя, trim, RuneState, custom data/components, repair cost, custom max durability и прочие vanilla компоненты. Износ переводится в ту же долю новой прочности с округлением вверх: `ceil(oldDamage × newMax / oldMax)`. Это предотвращает бесплатный ремонт; custom MAX_DAMAGE, если задан на базе, сохраняется. Стандартная починка работает: кожа для сапог, закалённое железо для прочих вещей.

## Визуал и integration

Направление — единственный approved PNG из архива, извлечённый в `work/unique-armor/approved.png`. Новые концепты не создавались. Нативные 32px inventory sprites и 64×32 worn textures; надетая броня использует четыре собственных humanoid meshes с vanilla bones/trim pipeline. Сапоги — высокие тёмные кожаные с небольшими холодными перьевыми элементами; нагрудник — широкая красная ламеллярная грудь и умеренные плечи; открытый металлический шлем с короткими боковыми элементами; отдельные кожаные ноги с бедренными и коленными пластинами. Щит — деревянный заострённый овал 10×14×2 с железным ободом и геометрическим бронзовым мотивом, отдельные held/blocking transforms. Материал — холодный металл со светлыми гранями без emission. [Фактические спрайты](media/unique-armor/production-sprites.png).

Creative/Search: все предметы включены в существующие категории; русское имя «Сапоги ветра» реально присутствует. Native tests проверяют membership, validator — RU/EN и ресурсы. Реальный ввод поиска остаётся ручным. JEI использует существующую Armorer category для всех пяти upgrades; optional `JeiUniqueEquipment` добавляет источник железа. JEI runtime UI не проверялся клиентом.

## Файлы production

Новые Java: `equipment/UniqueEquipment.java`, `UniqueEquipmentRules.java`, `UniqueEquipmentEvents.java`, `ClearSightNetwork.java`, `equipment/mixin/{EquipmentHealthMixin,WardBlockMixin,ClearSightMixin}.java`, `client/{ClearSightClient,UniqueArmorModels}.java`, `compat/JeiUniqueEquipment.java`.

Изменённые точки integration: `SlavicMyths.java`, `armorer/ArmorerRecipe.java`, `rpg/Runes.java`, `registry/ModItemGroup.java`, `client/ClientSetup.java`, `client/RareRuneClient.java`, `META-INF/neoforge.mods.toml`. Новые ресурсы: шесть models/sprites, shield blocking model, восемь armor textures, пять upgrade recipes, два tags, damage type, mixin config; RU/EN дополнены, в трёх курганных loot tables добавлены отдельные guaranteed pools. Полные списки файлов — в [машинном отчёте](verification/unique-equipment.json).

Generator `tools/unique_armor_resources.py` воспроизводит только этот слой. Старые rare weapon/rune и общий integration generators вызывают его в конце, предотвращая откат новых recipes/loot/texts. `build.gradle` содержит opt-in отдельный test source set, версия не менялась.

## Проверки и ограничения

`gradlew.bat clean build` финального production — PASS. Предыдущий combined clean build с `-ParmorOfflineCheck -PruneReworkOfflineCheck verifyArmorRunes verifyRuneRework` — PASS. Предыдущие 147 armor +355 rune offline checks — PASS. Новый resource/JAR validator — **19 336 проверок PASS**; 5 554 ранее существовавших файлов побайтно сохранены на момент проверки.

**8 реальных headless server запусков / 0 клиентов.** Первый: 4/11 (constructor health hook обращался к ещё не созданному player inventory); второй: 10/11 (неподготовленный blocking fixture); третий: 10/11 (clamped event blocked damage после вычитания равнялся нулю). Причины исправлены. Четвёртый: 14/14; пятый и шестой: **16/16**, седьмой и восьмой: **17/17** (добавлено исключение explosion owner и финальная атрибуция отражения). Шестой и последующие новым процессом читает реальные `.dat` и проверяет пять equipment stacks/два КД. Это не реальный клиентский relog.

Native GameTests: все пять баз/неверные базы/недостающие ингредиенты, компоненты и trim, износ, actual workstation consumption без повторного крафта, repair, item codec, Creative membership/ёмкость рун; native attributes/jump/fall/no-flight; HP boundary/KB idempotence/actual damage; native control; реальный 101-tick KB expiry; helmet intent/КД/categories; melee/projectile accepted/cancelled/ownerless reflection и антициклы; guaranteed native loot; native death, два dimension transitions и clone/respawn events; новый серверный процесс player storage. Mock network listener только в test fixture: реальная доставка outline packets/отрисовка/два клиента остаются ручными. MSPT/FPS не измерялись.

Production: `build/libs/slavicmyths-1.3.4.5.3.jar`, SHA-256 `c30b24178e089ba62537de7fc36a077da81040937d27ea606bb31b0846b5fb0d`. Тестовых классов/структур нет в JAR. Клиент и PolyMC не запускались, установка не выполнялась. Commit/push не выполнялись; пользовательские миры не использовались.

Во время работы общая папка параллельно изменялась извне: `bandit/NightingaleEntity.java`, `bandit/WindAttack.java` и инструменты boss verification. Этот patch их не редактировал и не откатывал. Они отдельно перечислены в machine audit; итоговый JAR содержит актуальное состояние общей папки, поэтому это не изолированный JAR только с бронёй. Версия на протяжении данного задания неизменна.

**REQUIRES_MANUAL_CLIENT_CHECK** — [краткая приёмка](MANUAL_VISUAL_CHECK.md). Compilation/server tests не выдаются за клиентскую визуальную приёмку.
