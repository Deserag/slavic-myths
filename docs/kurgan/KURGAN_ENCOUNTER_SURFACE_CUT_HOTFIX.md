# Курганы и Порез — hotfix 2026-10-06

Реализован поверх текущего рабочего дерева. Изменения 0.9.10/0.9.9 сохранены; registry IDs существующих предметов и существ сохраняются. Геометрия курганов не менялась.

## Враги

| Курган | Упырь | Навь | Дружинник | Выделенные встречи |
|---|---|---|---|---|
| Малый | 2–4 | 0–1 | 25%: один | Нет боссов |
| Воинский | 3–5 | 2–3 | 2–4 | Один Воевода, 150 HP |
| Великий | 5–8 | 3–5 | 4–6 | Воевода 150 HP, Погребённый волхв 150 HP, Неупокоенный князь 350 HP |

Seed определяет состав всего кургана, затем враги распределяются по подходящим комнатам, с охраной сокровищ и ограничением числа существ. Ловушки не получают толпы. Воевода получает отдельную воинскую комнату; если текущий великий план не содержит WARRIOR, используется существующая большая погребальная камера. Волхв находится в ритуальной комнате; legacy-планы используют OFFERING. Князь — в финальном зале за существующей печатью. Геометрия ради назначения комнат не переделывается.

Метаданные регистрируются при обычном размещении структуры и при команде. Вход игрока, включая creative, активирует встречу; creative не назначается целью AI. Система не зависит от естественного спавна. Сохранённые slots содержат тип, UUID, последнее положение, defeated/restored; состояния UNTRIGGERED/ACTIVE/CLEARED сохраняются. Смерть очищает слот, выгрузка не считается смертью. Проверка отсутствующего UUID разрешена только после загрузки entities соответствующего чанка. Утраченный слот восстанавливается один раз, старый UUID запрещается; убитые и очищенные комнаты не восстанавливаются. Уже активные legacy-встречи без slots сохраняют исходный UUID-состав.

## Поверхность

Детерминированные пространственные patches около четырёх блоков, а не независимый random каждого блока. Основные блоки: grass_block, coarse_dirt, rooted_dirt, dirt, gravel, moss_block, stone, cobblestone, mossy_cobblestone. В лесу допускается podzol; редкая растительность short_grass/fern и снег зависят от условий. Зоны вершины, склона, подножия и входа имеют разные веса; вход получает каменное усиление. Обычная насыпь и поднятый на Superflat грунтовый опорный объём используют одну палитру.

## Порез

Один native MobEffect CUT: amplifier+1 = 1–5 стаков. Каждый успешный удар существующим nightingale_dagger увеличивает общий стак до 5 и заменяет единственный effect instance с duration=100 ticks. Удар при пяти стаках тоже обновляет все 100 ticks. Через 100 ticks без попадания исчезают все стаки вместе; скрытых effect instances и отдельных сроков нет. Существующий порыв кинжала пока сохранён до решения по 0.9.10.

Отдельный pulse каждые 20 ticks наносит 0.25 HP за стак и не сбрасывается обновлением effect duration. Последний атакующий записывается в persistent NBT. Periodic DamageSource имеет causing entity, но не direct entity: урон не повторяет бонусы удара оружием; оберег охотника явно исключает cut. При отсутствии online-атакующего урон остаётся unattributed. PvP и friendly-fire команд проверяются; bleed_immune включает undead и все шесть курганных существ. Native effect и persistent NBT сохраняются штатно.

## Изменённые файлы hotfix

- kurgan: KurganRoster, KurganInstance, BurialRecords, KurganEncounterState, KurganEncounters, KurganSurface, KurganDungeonPiece, KurganEarthwork.
- worldgen/ManualStructureJobs.java: палитра поднятого грунта.
- combat/CutEffect.java, combat/MythDamageSources.java, item/SilverCombat.java, registry/ModEffects.java.
- damage_type/cut.json, tags/entity_type/bleed_immune.json, mob_effect/cut.png, ru_ru.json/en_us.json.
- build.gradle: отдельные opt-in hotfix GameTests; тесты и fixtures только tools/kurgan-tests и tools/kurgan-test-resources.
- tools/cut_resources_hotfix.py, обновлённый KurganHeadless.java, KurganHotfixGameTests.java; эти документы и три главных документа проекта.

## Ручная проверка

Снять внешние скриншоты малого, воинского и великого кургана, включая поднятый великий на Superflat: связность patches, земляной силуэт и переход входа. Проверить полноценные бои/AI, читаемость эффекта и реальный multiplayer PvP/перезаход. Headless chunk unload/reload и NBT roundtrip не заменяют реальный перезаход игрока. Естественная частота курганов и MSPT не измерялись в этом hotfix. Minecraft-клиент не запускался; установка JAR и commit не выполнялись.

## Итог проверок

Hotfix GameTests: 5/5 core и companions до последнего промпта. Всего 8 реальных headless server запусков hotfix/regression, клиент 0. Общая регрессия 26/27: обнаружена расчистка каменной кладки; камень удалён из approachSoil, нанесение палитры исключено из трёхблочной дорожки. Повторный runtime после исправления не выполнялся по последнему запрету auto-launch. CPU/plan/NBT/codec checks и clean build RC1 PASS, статические ссылки 2685/0 errors; production JAR проверен и включён в RC1. Логи: work/kurgan-hotfix-final-core.log, work/kurgan-hotfix-companions.log, work/kurgan-hotfix-regressions.log, work/playtest-rc1-clean-build.log. Никакой полной визуальной приёмки не заявлено.
