# CONTINUE 0.8.2

## DONE

Реализованы encounter Соловья, самостоятельные geometry/texture/animations,
три свиста, укрытия, whitelist, серверная полоса/сохранение, death sequence,
награды и три используемых предмета, personal chest, lore/advancement/RU/EN,
интеграция двора и две DEV-команды. Полный список — CHECKLIST_0.8.2.md.

Финальные clean build / reobf / Java8 / resources / геометрия / воспроизводимость: PASS.
PolyMC Slavic-Myths-Testing обновлён, см. verification/install-0.8.2.json.

## PARTIAL

Игровая оценка внешнего вида/синтезированного звука/баланса: ресурсов и подключений
достаточно для теста, но нужны реальный просмотр, прослушивание и бой пользователя.
Навигация с помоста и multiplayer реализованы, runtime-подтверждения нет.

## TODO

Ручные сценарии из CHECKLIST_0.8.2.md. Автоматического ретрофита полностью готовых
дворов 0.8.1 нет по принятому безопасному ограничению; нужны новые чанки.

## FILES

- bandit/NightingaleEntity.java, WindAttack.java, WindGeometry.java
- bandit/NightingaleWhistle.java, NightingaleDagger.java
- client/NightingaleModel.java, NightingaleRenderer.java
- bandit/LargeCampPiece.java, StrongholdRecords.java, CampCommands.java
- registries, ClientSetup, Knowledge, LoreScreen
- tools/nightingale_082.py, stronghold_081.py, verify_nightingale_082.py
- tools/tests/WindGeometryTest.java

Все Java-пути относительно src/main/java/org/slavicmyths.

## ARCHITECTURE

Подробно — ARCHITECTURE.md, верхний раздел 0.8.2. Не заменять state machine и
StrongholdRecords; registry IDs сохранять. Bit 27 книги, два независимых boss-флага,
один маркер/deterministic UUID лагеря. Команды используют старую locate infrastructure.
Геометрия и звуки генерируются воспроизводимо; править генераторы вместе с ресурсами.

## MANUAL QA

Minecraft не запускался. Список из десяти сценариев в CHECKLIST_0.8.2.md включает
модель/звук/атаки/укрытия/сохранения/предметы/двух игроков.

## CONTINUE FROM HERE

Сначала получить результат пользовательской проверки нового двора через
`/slavicmyths dev tp_nightingale`: наблюдение, физический спуск, три свиста,
смерть и перезаход. При проблеме спуска править только platform-descent ветку
NightingaleEntity.aiStep; при визуальной проблеме — NightingaleModel и текстуры
в nightingale_082.py. После точечной правки выполнить JDK8 build, три verify scripts,
WindGeometryTest и заменить единственный Slavic JAR в Slavic-Myths-Testing.
Не начинать следующую арку вместо исправления конкретных результатов QA.
