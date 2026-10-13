# Slavic Myths 1.7.6 — 2026-10-12

Версия собрана и установлена в PolyMC / Slavic-Myths-1.21.1-Testing. Minecraft1.21.1, NeoForge21.1.255, Java21 сохранены. Добавлены ровно19 items; новых summon tools/bosses/worldgen/workbenches/currencies нет. Старые4 предмета призыва богатырей сохранены.

## Как получить

- Откройте Книгу сказаний с книгой в руке и нажмите «Забрать награды». Уже записанные законные победы учитываются без повторного боя. Каждый first bundle: своя руна1 + материалы4/4/3/4/5. Нет истории — нет награды.
- Для дара присядьте и взаимодействуйте со своим Ильёй/Добрыней/Алёшей/Финистом после соответствующей победы и записанного участия/освобождения. Дар отдельный once-only; призывной предмет не расходуется. Потерянный уже выданный дар не выдаётся заново.
- Переполненный inventory: комплект не выдаётся частями и не падает на землю; освобождение места позволяет повторить claim. Repeat loot на новых authenticated callbacks: материал1–2 (Калин2–3), отдельный15% roll своей руны. Login не создаёт repeat loot.
- Новые5 трофеев крафтятся по обычным shaped recipes в существующем crafting/JEI. Эти vanilla recipes не переносят enchantments/component data исходного снаряжения.
- Руны устанавливаются прежней рунной наковальней с прежними socket/XP costs. Boots: Топот; helmet/chest: Рассеяние; melee: Преследование/Подавление; armor: Воинский строй. Новые cloak/mantle имеют1 socket; прежняя leather armor не изменена.

## Реализованные предметы

| Источник | Руна | Дар | Материал | Крафтовый трофей |
|---|---|---|---|---|
| Идолище / Илья | Топота | Щит «Богатырский оплот» | Костяные пластины | Нагрудник Ненасытного |
| Маринка / Добрыня | Рассеяния | Перстень верного удара | Осколки зеркала | Накидка Морока |
| Ским / Алёша | Преследования | Пояс хитреца | Клык | Ожерелье Клыков |
| Картаус / Финист | Подавления | Оберег Ясного взора | Обрывок знамени | Накидка Чёрного знамени |
| Калин | Воинского строя | — | Царское чёрное железо | Сабля Воеводы |

Щит использует accepted block hook: ×1.5 от hypothetical mitigated HP damage, CD700ticks. Перстень: один direct melee ×1.75, окно80ticks, CD240ticks после proc. Отражение и sweep — отдельные guarded damage types, не direct melee. Target-bound suppression CD500ticks; EYE cast interrupt только whitelist/telegraph. Dispel whitelist: конечные Weakness/Blindness/Confusion; physical slowness, hunger, story transformations и бесконечные control effects исключены. Пояс реагирует на поддержанные native FIST_WINDUP/CLAWS/SWORD direct telegraphs, не на arena-wide phases. Взмах сабли ограничен3 additional targets /95°/4.5blocks; Momentum хранит максимум3 unique-death timestamps в rolling600ticks. Equipment removal и UUID ownership проверяются сервером.

## Выполненные gates

- `gradlew.bat clean build`: PASS; production JAR без test/smoke classes, major65 / Java21.
- Последний focused dedicated GameTest:1 case /106 assertions /PASS. Реальные hooks: принятый shield block, reflection, ring/Pursuit/necklace modifiers, suppression, dispel, landing knockback, safe cloak/belt moves, native telegraph warning, chest mitigation/attribute removal, battle line and actual native Kartaus summon→mantle Resistance, saber unique kills→bounded sweep.
- Предыдущий native Kalin regression:1 case /100 assertions /PASS, включая6 реальных server hero-call pairings и old encounter lifecycle. Это отдельная server проверка, не клиентские бои.
- Offline native geometry:118 finite/mesh checks, три разные baked cube counts. Это не игровые screenshots и не проверка walking/swim/ride rendering.
- Production/resource audit:31878 checks /PASS; 7470 прежних JAR entries byte-identical. Изменённые прежние entries перечислены в resource-gates.json; старые RU/EN values и sound definitions сохранены.
- Approved19 PNG64×64 RGBA и22OGG перенесены byte-identical. Все22OGG fully decoded и mono Vorbis. Generator reproduction: zero drift.
- Native PlayerDataStorage.save и фактическое чтение playerdata с диска:5 first +4 gift flags, inventory и timers подтверждены. NBT load также проверен. Это не настоящий клиентский relog или world restart.
- Реальных dedicated server запусков этой итерации:18 (включая неуспешные старты и regression). Client launches:0. Одна попытка runtime остановилась на compilation и не считается запуском сервера.

## Установка и ограничения приёмки

Production SHA256: `301a72abab2940ced97ffcbea4579726bd3b9a14e60d5344f3aa766b40d8da98`. Такой же файл в build/libs, release и выбранном PolyMC mods. Прежний1.7.5 сохранён: `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.slavicmyths-backups\pre-rewards-20261012-091053\slavicmyths-1.7.5.jar`. 263 остальных файлов экземпляра проверены неизменными; игровые миры и старый PolyMC1.16.5 не перезаписаны. Commit не создавался.

NOT_RUN: first/third-person visual poses, GUI scales, audible cues/subtitles in real client, real reconnect mid-claim, world restart, two real clients, measured MSPT. Single server synthetic players подтверждают owner-specific guards/claims; это не multiplayer client acceptance. Ручной минимум: книга/полный inventory; shift-interact каждого героя; shield raise and counter; снять каждую rune/accessory/chest; crouch/swim/ride geometry; реальные relog/two-client checks на отдельном тестовом мире.
