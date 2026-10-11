# Slavic Myths 1.4.3 — проверенный релиз, 2026-10-09

Работа возобновлена по запросу пользователя после ночной паузы. Уточнение о двух разных мобах выполнено: прежний Волколак сохранён, новый **Проклятый Волколак** добавлен отдельно.

| Результат | Статус |
|---|---|
| Variant B model, две позы/переходы/анимации | READY; клиентская приёмка открыта |
| 400 HP, три фазы, физические атаки/хватка/Дурная кровь | READY |
| Четвёртое поручение, могила/череп/передача Волку | READY |
| Отдельное яйцо нового моба | READY |
| Новые OGG | 28 файлов / 23 события; источники/лицензии записаны и включены в JAR |
| Существующие Finist Bow + Quiver, прежние системы | READY; production/baseline/regressions PASS |
| Clean build | PASS в отдельном output directory |
| Production resources/JAR | 15945 проверок PASS; 60 protected baseline файлов сохранены |
| PolyMC | PASS; один активный mod JAR, 381 прочих файлов сохранены byte-for-byte |

Production: `D:\slavic-myths\build\libs\slavicmyths-1.4.3.jar`. Установлен: `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.minecraft\mods\slavicmyths-1.4.3.jar`.

SHA-256: `e00192ac339433dfeaf6e7dfd40310f59bb5e0c50b85e833bea018ba688e8e63`.

## Фактическая проверка

Финальные наборы: **25 Volkolak +25 WolfStory +20 GreyWolf +63 Combat +23 Unique =156 native GameTests PASS**. Регрессии выполнены в `final-runtime.log`; после последних изменений моба/могилы финальные 25 Volkolak повторены в `isolated-runtime-pass.log`. Native model bake: **1660 checks**, 68 cuboids. JSON/PNG/resources/OGG/production bytes, Java21 и отсутствие fixtures проверены отдельным текущим gate.

Реальных запусков headless Java loader в этой итерации: **16**, клиентов: **0**. Включены промежуточные неудачные проверки, исправленные до релиза. Один запуск остановился ещё при загрузке mixins из-за изменявшегося общего output; это не выдаётся за успешный игровой тест. Изначальные ошибки хватки/щитового test fixture, сравнительного travel fixture и блокировка обычного clean сохранены в исходных логах. Два compile-only fixture сбоя не считаются игровыми запусками.

Обычный `gradlew.bat clean build` не смог удалить занятые другой задачей immutable NeoForge artifacts. Итоговый `gradlew.bat --init-script work/volkolak-143/isolated-build.gradle clean build` полностью очистил/собрал отдельную `work/volkolak-143/release-build`, не прерывая чужие процессы. Проверенный production JAR скопирован в обязательный `build/libs`. Старые/параллельные изменения workspace сохранены; Chud gameplay здесь не проверяется.

Native ground travel при одинаковых 10 шагах без AI/pathfinding: new=7.327029762811621 GREY_WOLF=5.427429803513746; controller-free native travel fixture. Это сравнительное физическое измерение fixture, не прохождение лесного маршрута. Collision 1.35×1.85 quad /1.35×2.75 upright. Визуальная высота выше земли: quad 1.841, upright 2.768 blocks. Длина корпуса quad без хвоста 2.947 blocks; полный с хвостом 3.521. Нижняя граница в нейтральной offline позе пересекает плоскость земли до 0.052 блока; это меньше одного texel, динамическая походка требует клиентской проверки.

## Что остаётся проверить пользователю

Исторический `tools/verify_resources.py` также выполнен: он перенаправляет проверку на строгую parity исходной версии 0.9.3 и выдаёт **140 прежних несовпадений** (версия, переименованные datapack пути, поздние gameplay/data изменения). Такой же FAIL уже зафиксирован в checkpoint 1.4.2; baseline не переписан ради PASS. Это не текущий release gate 1.4.3: отдельный `verify_volkolak_143.py` проверяет все актуальные JSON/PNG и production resources, собственные assets/audio/локализации и protected baseline. Лог исторической проверки сохранён рядом.

GUI/render/texture/animation/sound perception, survival естественная генерация и полный сюжет, настоящий multiplayer/relog/server restart, Journal/Xaero и MSPT не запускались/не измерялись. Native NBT roundtrip не является реальным перезаходом. Проекции — offline native geometry, не игровые скриншоты. Код/ресурсы готовы, игровая приёмка остаётся открытой по условиям архива «user does gameplay QA».

[Реализация требований и безопасная адаптация могилы](../../IMPLEMENTATION_MAP_1_4_3.md), [краткая ручная проверка](MANUAL_QA.md), [звуки и лицензии](SOUND_CREDITS.md), [production gate](resource_gates.json), [установка](polymc-installation.json).
