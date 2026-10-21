Историческая ограниченная итерация43. Текущая полная цепочка #88/#122–129 и source46: [REPORT](../armor88-full/REPORT.md). Приведённые ниже результаты относятся только к сохранённому baseline43.

# #88 — льняной комплект, source 1.9.7.43

Статус IN_PROGRESS / OWNER_PENDING. Выбран один дефектный комплект: linen_shirt + linen_ports. Прочие 40 предметов из области аудита #88 не перерабатывались. Issue остаётся OPEN. Автоматических запусков Minecraft/PolyMC/сервера: 0.

Дефект подтверждён нативным bake: рубаха и верх портов имели вынос 0.12 при внешнем слое скина 0.25; низ портов заканчивался на Z=1.92 внутри самой ноги Z=2.0. Теперь рубаха имеет вынос 0.32, рукава 0.34; пояс портов 0.44, верх 0.38, низ 0.32. Низ использует полную основу ноги 4×4 вместо 3.6×3.6. Разные выносы разделяют поверхности швов. Точки крепления копируются из реального PlayerModel, включая Alex.

Текстуры, иконки, registry ID, характеристики, рецепты, common/server код, сохранения и рунический слой не изменены. Это исправление посадки, а не новый дизайн всей брони.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Один реальный дефектный комплект | before-native.json: пересечение с базовой ногой и внешним слоем скина | PASS |
| Реальные ModelPart/UV/PNG до и после | before/ исходник, before-native.json, after-native.json; tools/armor88-offline | PASS |
| Steve/Alex, front/right/back/3quarter, idle/walk | SOURCE-BEFORE-AFTER.png: 16 пар кадров, одинаковая камера и масштаб | PASS |
| Поднятая рука | Ещё 8 пар source-поз weapon_hold; геометрия оружия не показана, реальная матрица хвата сохранена в JSON | PASS для source-позы; runtime NOT_RUN |
| Нативные UV в пределах атласа, точки крепления | exportArmor88; verify_armor88_linen.py | PASS |
| Остальные шесть предметов одежды | 12 нативных контрольных мешей до/после идентичны | PASS |
| ID/иконки/баланс/сохранения/рунический код не изменены | baseline-source-sha256.json: изменены только TextileClothingLayer.java и версия | PASS для source; runtime NOT_RUN |
| Java 21 clean build + регрессия rune104 | BUILD.log | PASS |
| Production JAR, ресурсы, PNG, Java21, отсутствие offline fixture | jar-check.json | PASS |
| Игровые clipping/z-fighting/анимация/сочетание с предметом/рунами | Нужна ручная проверка владельцем на Steve и Alex, включая внешний слой скина | NOT_RUN |
| Ручная приёмка владельцем | Ожидается | NOT_RUN |
| Установка PolyMC | Не запрошена в этой итерации; установленная 1.9.7.42 сохранена | NOT_RUN |

## Воспроизведение

Java21: gradlew.bat clean build -I tools/armor88-offline.gradle exportArmor88 -I tools/rune104-offline.gradle checkRune104Offline

Затем python tools/armor88_preview.py и python tools/verify_armor88_linen.py. Для baseline использовалась неизменённая 1.9.7.42, затем экспортирована исправленная модель. Preview — offline depth-buffer rasterization нативных мешей с настоящими текстурами, без запуска игры; не доказательство runtime. Состояния idle/walk/weapon_hold задаются в fixture и не изображают фактический игровой запуск.

Коммит не создавался. 1.9.7.43 в PolyMC не установлена. Сборка не объявляется релизом или принятой задачей.
