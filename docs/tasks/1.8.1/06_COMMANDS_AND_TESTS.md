# Управление, тестирование и автоматические проверки

## Гарантированная процедура получения предмета
Поддержать `/give @s slavicmyths:mor_flask 1` (действительный стабильный новый item ID), Creative tab и tooltip. Дополнительной OP-команды `/sm ...` НЕ вводить ради единственного зелья, если уже есть debug command pattern — допустимо аккуратно добавить alias к имеющемуся тестовому набору, но **никакой второй способ превращать моба не требуется для acceptance**. В этом пакете вводится только один новый видимый предмет.

## Manual QA сценарии (для пользователя, НЕ для запуска Codex)
1. Запустить отдельно созданный TEST world вручную, установить Creative, получить Склянку мора через Creative/`/give`.
2. Вызвать существующие обычные сущности имеющимися яйцами/командами (не создавать новые mor eggs), бросить Flask в одного; проверить 2,5s transition, честную модель/звуки/particle.
3. Для каждого поставить один обычный и один mor рядом, посмотреть фронт/бок/зад, сравнить здоровье и cooldown ударов. НЕТ обязательного вступления в квест.
4. Леший: стоять в 9blocks, увидеть 3 предупреждающих «трещины», уклониться или получить один раз корневой урон; подойти на5block для крика, проверка wall.
5. Полудница: проверить слепоту от красной вспышки, two-swing dash и бонус игрового полудня; побег из радиуса avoids hit.
6. Кикимора: наблюдать ровно 3 прыжка, финал с отдельной паузой, нити не проходят сквозь стену.
7. Огненный змей: обычная форма vs mor mini-boss; при≤50% HP два неотличимых змея по30HP; bossbar скрыт; попасть в clone и original, проверить лут только у original.
8. Сохранить/перезайти (manual) при mor transformation и во время 3-serpent fight, проверить отсутствие повторной генерации.
9. Проверить survival natural spawn после day7 и 160blocks от точки спавна, обычные варианты не исчезают, редкость условная (без требуемого измерения фактической частоты за один короткий тест).
10. Пользователь смотрит реальные анимации и звуки; до этого Codex маркирует visual/audio **REQUIRES_MANUAL_QA**, даже если модели и звуковые ресурсы подтверждены static/gametest.

## Автоматические тесты CodeX, только адресные
- Unit/state test for 3-state lifecycle, HP fractions and modifiers, NBT roundtrip, clone summon-once and mixed loaded/unloaded saves.
- Spawn gating parameterized: day6/7 boundary, only Overworld, distance160 boundary, whitelist, 4% RNG deterministic seeded, existing spawn reason, AABB mor-cap, flask unaffected by day.
- Combat: attack timing/cooldowns, LOS, cover, radius, no crowd damage in same cast, no friendly fire, no block changes, projectile lifetime.
- Existing source regression: normal entity classes do not receive mor attributes/animations/skin; no existing IDs removed; correct Book discovery once, ru_ru/en_us, potion no recipe.
- Resource verify: all JSON syntax, all textures/media paths, all `.ogg` references exist if declared, 4 mor native models baked, no pink missing texture, production archive contains needed assets, no generated test fixtures shipped.
- Performance guard static: no O(worldEntities) per-tick, no forced chunk load, no config polling/dynamic asset reload for one entity.

## Отчёт о тестах
Сохранить `docs/verification/mor-1.8.1/REPORT.md`, включить фактические команды и выходы, количество headless/server/client attempts, pass/fail и незапущенную manual visual/audio/rejoin проверку. Если Java API отличается от указанных в ТЗ имён — использовать **реальные** API версии NeoForge и записать замену; не оставлять заглушку.
