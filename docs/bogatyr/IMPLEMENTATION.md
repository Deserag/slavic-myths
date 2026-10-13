# Илья и Идолище 1.7.1

Исходное ТЗ хранится целиком в `docs/specs/1.7.1`; это спецификация реализуемого пакета, а не разрешение на сторонние действия. Согласованный пользовательский объём: реализация и установка мода в PolyMC 1.21.1-Testing. Чужие пользовательские системы сохранены.

## Архитектура

`bogatyr/BogatyrContent` регистрирует две сущности и 33 звука в существующих registries. `CombatEntity` синхронизирует state/start/duration; `IlyaEntity` и `IdolishcheEntity` выполняют собственный серверный автомат. Нет общего перебора мира/игроков каждый тик и принудительной загрузки чанков. Поиск целей ограничен местной областью и cooldown; minion/taunt goals устанавливаются событиями. `BogatyrMotion` связывает native pivots рук и серверные PartEntity. `BogatyrEvents` контролирует mitigation/однократный redirect, исключение AoE, landing damage и журналы.

`BogatyrLedger` — Overworld SavedData по UUID: единственный герой и история побед. При недоступном сохранённом герое второй экземпляр не создаётся. На загрузке незавершённый бой босса прекращается с очисткой своих minions; восстановление героя продолжает сохранённое elapsed, а не даёт бесплатный reset. Debug-прогресс исключён. Направления/цели/расходы/таймеры решает сервер.

`client/BogatyrClient`, `IlyaMesh`, `IdolishcheMesh` изолированы от common. Vanilla native renderer без сторонней animation библиотеки; HUD и screen только client; packet обработчик валидирует игрока/действие/частоту. `BogatyrArena` держит только активные очереди явного строительства, проверяя air/fluid/loaded заново перед записью. Остановка сервера может оставить частично построенный двор; блоки мира не стираются.

## Силуэт и ресурсы

Илья: 40 model pixels (2.5 блока), широкие металлические/меховые плечи, красный сужающийся щит 16×26 в левой руке; палица справа — деревянная рукоять 3×19 и железная шипованная головка 10×9. Это самостоятельные формы щита и палицы, не recolor другого оружия. Идолище: 144 pixels (9 блоков), грушевидный живот, длинные асимметричные руки/четыре пальца и большой палец, подвижная челюсть/клыки, деревянный спинной каркас/трофеи. Атласы 256², индивидуальные UV и padding; 107/131 cuboids, 19/39 bones. Фольклорные персонажи и оригинальная игровая интерпретация, не историческая реконструкция.

`python tools/bogatyr_assets.py` воспроизводит только собственные native rigs, текстуры, добавления sounds/lang и damage types. OGG копируются из предоставленного собственного аудиопакета побайтно; голоса из чужих произведений не используются. `tools/bogatyr_preview.py` строит offline previews из экспортированных baked native mesh.

## Пользовательские команды

- L — меню призыва, R — освободиться из захвата; клавиши переназначаются.
- `/slavicmyths hero call` — призыв до боя, без прав оператора.
- `/slavicmyths encounter courtyard` — оператор; требует пустой загруженный диск диаметром61 и высотой14 перед игроком, строит центр в36 блоках по взгляду.
- `/slavicmyths encounter idolishche` — оператор; настоящая встреча с записью прогресса, босс примерно12 блоков впереди на безопасном полу. На готовом дворе призвать героя заранее.
- `/slavicmyths debug_171 spawn_ilya|spawn_idolishche|courtyard|test_hero_retreat|status` — оператор и debug config.
- `/slavicmyths debug_171 attack fist|stomp|grab|summon|breath|charge` — только debug босс.
- `/slavicmyths debug_171 boss_phase 2`; `progression get|clear_test` — тестовые инструменты оператора; `clear_test` явно очищает запись вызывающего игрока.

## Проверки

`gradlew.bat -I tools/bogatyr-offline.gradle bogatyrOfflineChecks`

`gradlew.bat -I tools/bogatyr-runtime.init.gradle -PbogatyrRun=<отдельное-имя> runBogatyrChecks`

Перезапуск использует одинаковое bogatyrRun с `-PbogatyrNamespace=slavicmyths_bogatyr_restart -PbogatyrRestartPhase=write`, затем `read`. Мир только `work/bogatyr`, игровые пользовательские saves не применяются. Тесты не входят в production JAR.

`gradlew.bat clean build`, `python tools/verify_bogatyr.py`, затем `python tools/install_bogatyr.py` — gates и установка с backup. Последний verifier фиксирует конкретный принятый runtime-08, его нельзя считать автоматическим выполнением нового игрового теста. Итоги: [отчёт](../verification/bogatyr-1.7.1/REPORT.md).
