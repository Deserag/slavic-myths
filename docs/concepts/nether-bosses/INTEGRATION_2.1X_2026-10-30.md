# 2.1.x — расширение призывных боссов, решение владельца30.10.2026

Это новое расширение #353/#354. Исторические запреты на новые предметы в отдельных архитектурных2.1.x не переписываются. Новые компоненты добавляются только в штатные loot tables ещё не раскрытых сундуков; без ретрогенерации/пополнения открытых контейнеров.

| Структура | Два гарантированных разных компонента, по1 |
|---|---|
| Дозорный пост/nether_watchpost | broken_watch_horn, charred_signal_ribbon |
| Стоянка странника/wayfarer_camp | morova_thread, darkened_waymark |
| Забытое капище/forgotten_shrine | faded_rite_seal, nameless_ashes |
| Кумирня предков/ancestor_shrine | ancestor_bone_charm, mourning_circlet |
| Тризное кострище/trizna_pyre | nameless_ashes, charred_signal_ribbon |

Каждый компонент — отдельный guaranteed pool с rolls1. Прежние pools сохранены. Broken_nether_bridge/2.1.2 не затронут;2.1.8/2.1.9 отсутствуют. Все8компонентов покрыты; два повторяются в тематически подходящих местах.

## Рецепты — инженерная конкретизация, разрешённая30.10.2026
Оба shaped3x3 с pattern `1A2 / B C / 3D4`, центр пуст. Это НЕ восстановленная раскладка из художественного постера.

| Ключ | amulet_of_the_faded | last_watch_horn |
|---|---|---|
|1|morova_thread|broken_watch_horn|
|2|faded_rite_seal|darkened_waymark|
|3|ancestor_bone_charm|mourning_circlet|
|4|nameless_ashes|charred_signal_ribbon|
|A|minecraft:spider_eye|minecraft:blaze_rod|
|B|minecraft:ghast_tear|minecraft:ghast_tear|
|C|minecraft:soul_sand|minecraft:netherite_scrap|
|D|minecraft:bone|minecraft:bone|

Каждый ингредиент ровно1раз. Last_watch_horn расходуется только ритуалом, fallen_druzhina_horn — отдельный многоразовый трофей.

Ритуальная конкретизация: дева — soul_sand, свечи над четырьмя опорными блоками на расстоянии2 по осям; князь — campfire,8камней в кольце3x3 (blackstone/polished_blackstone/basalt/netherrack).10сек, Nether-only,96блоков без другого ритуала/босса, проверка свободного bbox перед расходом. Прерванный RESERVED можно отменить без расхода; неопределённый SPAWNING после сбоя не повторяется автоматически. Полная crash-boundary QA ещё обязательна.

Иконки — новые авторские детерминированные pixel assets, генератор tools/netherboss-assets/generate.py. Не являются исходным постером босса. Все признаки BLOCKED_ART_REFERENCE/BLOCKED_AUDIO сохраняются до объективной сверки.

## Runtime VFX и звуковые события

У обоих боссов — 11 отдельных registry sound events с ru/en subtitles: ambient, windup, impact, zone, wave, summon, phase, hurt, death, ritual, trophy. Сервер выдаёт windup до кадра урона (30 тиков, для раскола 36), impact в contact frame; отдельные зоны/волны/призывы и 40-тактовый визуальный переход фаз. Ритуал и аксессуар также имеют cue. Мор — зелёно-серый dust, красный конус — telegraph, пламя — vanilla flame; никаких временных разрушающих блоков.

На этом этапе используются легальные vanilla foley через sounds.json type:event, normal pitch: soul_escape/шерсть/душевная почва для Девы, цепи/netherite/basalt/fire/horn для Князя. Это намеренно выбранные технические cues, не оригинальные записи голоса. Голоса ведьмы/визер-скелета WIP удалены. Шёпот и оригинальные голосовые/художественные SFX остаются BLOCKED_AUDIO; совпадение/согласование аудио не заявлено. Ресурсный генератор воспроизводит определения.
