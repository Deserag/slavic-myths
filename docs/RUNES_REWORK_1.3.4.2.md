# Руны I–II: 1.3.4.2

Реализовано по архиву Slavic_Myths_1.3.4.2_Runes_Rework_Codex.zip. Minecraft 1.21.1, NeoForge 21.1.255, Java 21, mod ID `slavicmyths`. Клиент и сервер запрещены пользователем: реальных запусков **0/0**. Полная игровая приёмка остаётся открытой. Classes GUI/HUD отдельно ждут утверждения макетов; PolyMC остаётся 1.3.3.

## IDs и миграция

Существующий installed component `slavicmyths:runes`, registry IDs, ёмкости, зачарования и native special effects не заменены. `rune_heat` переиспользован как Огонь I, `rune_wind` — Ветер II. Добавлены только `rune_strength`, `rune_speed`, `rune_resilience`, `rune_crushing`, `rune_blood`, `rune_fortitude` в существующую Creative Tab.

Старые `thunder`, `forest`, `midday`, `shadow`, `protection`, `life` сохраняют IDs/эффекты; повторная установка этих старых видов запрещена, на броне их прежнее хранение без эффекта сохранено. Все восемь старых rune sprites переведены на физические камни.

Для старых установленных heat/wind добавлен persistent/network component `slavicmyths:legacy_rune_specials`. Отсутствие компонента означает старую установку: прежний Огонь сохраняет поджог 2 с с прежним cooldown, Ветер — +8% скорости стрелы и прежние специальные отбрасывания. Пустой компонент означает новую установку с параметрами ниже. При добавлении копий старые особенности закреплены за первой копией; снятие первой убирает соответствующий legacy flag. Новые копии дают только новые эффекты. Repair запрещает объединять предметы с разным происхождением этих эффектов. Прежние компоненты и CustomData читаются; schema installed RuneState сохранена и допускает новые повторения.

## Эффекты одной новой копии

| Руна / ID | База | Эффект и совместимость |
|---|---|---|
| Сила I / strength | iron | Ближний бой: +6% урона |
| Быстрота I / speed | gold | Ближний бой: +6% скорости атаки; ботинки: +4% движения |
| Стойкость I / resilience | iron | Броня: +0,5 toughness; щит: −5% расхода прочности при блоке |
| Огонь I / heat | gold | Ближний бой: 15% поджечь на 3 с; броня: −8% огненного/лавового урона |
| Сокрушение II / crushing | diamond | Ближний бой: +18% урона, −10% скорости атаки |
| Ветер II / wind | perunite | Ботинки: +8% движения, +15% силы прыжка, +3% горизонтального импульса прыжка; лук/арбалет: +15% начальной скорости стрелы |
| Кровь II / blood | perunite | Ближний бой: 20% кровотечения, до 2 стаков, по 0,5 HP каждые 2 с, длительность 6 с |
| Крепость II / fortitude | diamond | Броня: +1 toughness, +0,03 knockback resistance, −1,5% движения; щит: −5% расхода прочности |

Кровотечение скорректировано по прямому ответу пользователя: оставить 0,5 HP и native максимум 3 стака. Руническое ограничено двумя, общее — `max(native, rune)`, максимум три, а не сумма пять. Используется существующий RareBleedEffect, отдельные сроки/владельцы источников и общий интервал 40 ticks; повторное применение не удваивает periodic hit. Иммунитеты/PvP permissions сохранены. Тугарин сохраняет свою отдельную механику и остальные native эффекты.

Огонь применяется после успешного прямого melee damage, без рекурсивного damage; один итоговый поджог, более длинный существующий огонь не укорачивается. Огненная защита работает через IS_FIRE на final damage. Ветер не меняет baseDamage стрелы; скорость может естественно повлиять на vanilla impact damage. SlavicBowChecked предотвращает повторное ускорение при загрузке entity. Прыжок сохраняет gravity и обычную траекторию; полёт не добавлен.

## Повторения и пределы

Положительные коэффициенты копий: 1 / 0,70 / 0,45; отрицательные: 1 / 0,60 / 0,35. Для четвёртой и последующих копий по всей броне используется третий коэффициент. Максимум три sockets на предмет, прежние material capacities сохранены. Итог каждого вида считается по совместимым надетым предметам, затем ограничивается; разные виды складывают свои вклады.

Caps: melee damage +60%; attack bonus +35%, penalty −50%, отрицательная руна не снижает attack speed ниже 0,10 (не повышает уже более низкое внешнее значение); movement bonus +35%, penalty −15%; toughness +4; knockback +0,20; fire reduction 40%; proc chance 50%; jump +35%; projectile speed +35%; shield efficiency 25%; horizontal jump momentum +8%. Пример: три Силы +12,9%, три Сокрушения +38,7% damage/−19,5% attack speed; три Быстроты и три Сокрушения дают множитель attack speed 0,934.

Атрибуты transient, стабильные IDs `slavicmyths:rune2_damage/attack/movement/toughness/knockback/jump`; обновление на equipment change, entity join, login, respawn и смену измерения. Повторное refresh удаляет наш прежний modifier перед добавлением. Нет нового глобального tick polling/world scans; native item modifiers и чужие modifier IDs не удаляются.

## Рецепты, наковальня, JEI

Recipe IDs: `slavicmyths:rune_rework_strength`, `_speed`, `_resilience`, `_heat`, `_crushing`, `_wind`, `_blood`, `_fortitude`. Общий существующий type `slavicmyths:rune_crafting` расширен optional input base component. Все рецепты выдают одну руну, без расхода резца; используют три counted inputs:

| Руна | Заготовка ×1 с точным component | Остальные inputs |
|---|---|---|
| strength | I iron | iron_dust ×2, redstone ×1 |
| speed | I gold | gold_dust ×2, feather ×1 |
| resilience | I iron | iron_dust ×2, lapis_dust ×1 |
| heat | I gold | coal_dust ×2, flint ×1 |
| crushing | II diamond | diamond_dust ×2, iron_dust ×2 |
| wind | II perunite | perunite_dust ×2, feather ×2 |
| blood | II perunite | perunite_dust ×2, soul_fragment ×2 |
| fortitude | II diamond | diamond_dust ×2, lapis_dust ×2 |

Ingredient совпадения недостаточно: сервер проверяет tier/material component. JEI display stacks и меню используют ту же Counted definition. В existing RuneAnvilScreen добавлены эффекты/база/совместимость в recipe tooltip, иконки установленных рун и tooltip результата; actual equipped armor totals показываются с caps. Старый authoritative output transaction сохранён. JEI остаётся необязательным и изолированным в compat.

Ближний бой — `slavicmyths:rune_melee_weapons`, включает vanilla swords/axes/trident/mace и реальные melee families мода через существующие rune_dagger/spear/heavy/staff tags и item entries. Броня, boots, shields, BowItem/CrossbowItem определяются типами. Несовместимая установка отклоняется с причиной; старые установленные запрещённые теперь сочетания сохраняются. Rare weapons и native RareCombat byte-for-byte сохранены, кроме общего RareBleedEffect. Названия уникального снаряжения из ТЗ не считаются зарегистрированными автоматически: используются фактические items. SevenLeagueBoots имеет прежнюю leather capacity 0, эта граница не увеличена.

## Визуал и границы

14 разных RGBA 32×32 sprites: камень, врезанный символ, прозрачный фон; I грубый, II обработанный, близкий физический размер. Символы: X, перо с тремя ответвлениями, щит, пламя, шестилучевая трещина, завихрение, капля, крепость. [Контактный лист](media/runes-rework-1342/runes.png). Генератор воспроизводит актуальные recipes/sprites/models/locales/tag без изменения baseline loot/worldgen.

Стандарт будущего III: тот же физический размер и семейство камня; более глубокая обработка/инкрустация и аккуратное внутреннее свечение. Новых III предметов/эффектов, каркасов систем, боссов, structures, измерений, accessories и class synergies нет.

## Проверки и файлы

`gradlew.bat clean build -PruneReworkOfflineCheck -PruneFoundationOfflineCheck -ParmorOfflineCheck verifyRuneRework verifyRuneFoundation verifyArmorRunes`: BUILD SUCCESSFUL. Rune rework — 355 formula/JSON/NBT/network/provenance checks; armor regression — 147; foundation regression — 75064, 3000 bounded plans. Resource/preservation gates — 22838, сохранены 4898 прежних resources; все JSON production JAR читаются, тестовые классы исключены, common rune classes не ссылаются на client/JEI. Generator reproduction PASS.

Попытка unit-проверки native AttributeInstance без игры потребовала Minecraft registry bootstrap; заменять её обходом не стали. Equip/unequip native attributes, repair/menu transactions, JEI rendering, реальная смерть/перезаход, bleed/fire combat, client GUI и multiplayer/dedicated-server не проверены игровым запуском. Формулы и codec roundtrips не выдаются за эти проверки; MSPT не измерялся.

Production: `build/libs/slavicmyths-1.3.4.2.jar`, 9690815 bytes, SHA-256 `fcf695172460a7023942305d9fcda70fe6cf095c1753f39f2e2eb2361de09cfe`. [Машинный отчёт](verification/runes-rework-1.3.4.2.json).

Изменённые файлы этой итерации: `gradle.properties`, `build.gradle`; `SlavicMyths.java`, `item/ItemState.java`, `combat/RareBleedEffect.java`; rpg `RuneBalance.java`/`RuneRework.java`/`RuneRuntime.java` (новые), `Runes.java`, `RuneDefinition.java`, `RuneEffects.java`, `RuneRepair.java`, `RuneCraftRecipe.java`, `RpgMenu.java`, `RpgEvents.java`; client `RpgClient.java`, `RuneAnvilScreen.java`; compat `JeiRunes.java`; 14 rune models/textures, 8 recipe JSON, melee tag, ru_ru/en_us; `tools/rune_rework_resources_1342.py`, `tools/verify_rune_rework_1342.py`, `tools/rune-rework-tests/.../RuneReworkOffline.java`, существующий `ArmorRunesOffline.java`; этот отчёт, verification JSON/media и PROJECT_STATUS/ROADMAP/ARCHITECTURE. Прочие имеющиеся пользовательские изменения не сброшены; commit не создан.

Ручная приёмка после разрешения запуска: installation/remove/repair каждого вида и wrong base; 1/2/3 copies и armor caps; swap/death/relog без накопления attributes; fire/lava/bleed/native Tugarin; bow/crossbow velocity, jump; JEI/GUI tooltips; отдельный dedicated server/PvP. Только отдельный тестовый мир.
