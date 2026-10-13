# Встраивание в текущий код без общего аудита

## 0. Порядок минимального чтения кода
Вместо `find whole project / grep recursively everything` прочитать **только**:
1. root Gradle/dependency config: версию Minecraft/Forge/Java, mappings, mod id, только 2–4 профильных файла;
2. способ регистрации новых entity, item, sound, damage source, potion/effect, particle; при необходимости 1–2 существующих entity как пример;
3. существующие рендереры/модели/анимации/texture resources;
4. один ближайший класс существующего босса/врага с server AI, если есть;
5. rune registry, item tooltips, item slots, capability/naming для двух наградных рун;
6. accessories slot/current UI + skill keybind system;
7. Baba Yaga NPC quest and Cat Bayun NPC quest, player quest progression flags;
8. existing Book of Tales, localization and networks only as directly needed.
Если символ находится рядом с найденными классами, искать по точному имени. **Не анализировать все другие модульные системы и весь исторический git log.**

## 1. Набор реальных инстансов/пакетов
Реальные названия подстроить под существующие conventions, но функциональные слои обязательны:
- boss entity, server AI state controller, ability scheduler, frost aura component/status effect;
- hostile ice double entity, renderer sharing mesh with tint/alpha;
- player ice double entity, renderer snapshot owner skin + armor + weapon, server action-mirror/target coordinator;
- trial summoning artifact and NPC unlock path, trial session persist/participant rewards;
- frost mirror accessory and cooldown storage; two runes and rune integration; staff and projectile skills; heart quest item;
- asset resources: blocky geo/model files, PNG textures, translucent layers, animations, particles, `.ogg` and `sounds.json`, item models, localization, book/advancement.
Avoid one God-class with all model, quest, loot, combat, and networking combined.

## 2. Документация модели как источник истины
- `02_REFERENCES/approved_full` — visual source; `02_REFERENCES/crops` — zoom views.
- В `03_IMPLEMENTATION/02_REFERENCE_MAP.md` для каждого предмета указано конкретное изображение. Программист должен сопоставить **каждый важный элемент** модельной структуры с фрагментом референса.
- Если текущий стек не позволяет объёмные 3D предметы без нестандартного мода — использовать зарегистрированный baked/item model API этой версии и честно указать технический fallback; **не считать плоскую палку с синей текстурой полной реализацией**.

## 3. Инкрементальная компиляция
После реальных блоков изменений запускать compile, resource validation, существующие unit-test задачи, которые НЕ поднимают графический клиент/сервер. Проверить обычную production сборку без PolyMC/import; если сборка требует runClient, **не запускать**. Не создавать фиктивный success-status.

## 4. Deliverable от Codex
- Изменённые целевые исходники/ассеты + локализация + реализуемая наградная цепочка.
- Короткий CHANGELOG 1.9.1, список точных изменённых и новых файлов и команд компиляции (фактический результат).
- Перечень известных ограничений, которые пользователь должен увидеть при ручном тесте, и результаты автоматических проверок.
- Не переносить в release/PolyMC, не создавать артефактов для других версий.

## 5. Конфликты с existing mod
Всё, что уже работает для Серого волка (призыв/союзник), класса/руны и Бабы-яги/Кота Баюна — реиспользовать точечно, если оно действительно подходит, не заменять исходные системы. При отсутствии требуемой функции — реализовать минимальную совместимую поддержку для 1.9.1, не писать «позже», если это обязательное условие приёмки.
