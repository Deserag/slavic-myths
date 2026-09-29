# Проверка 0.2.0

- BUILD: PASS — `gradlew.bat build --console=plain`, JDK 8u504, BUILD SUCCESSFUL in 6s,
  reobfJar выполнен. `build/libs/slavicmyths-0.2.0.jar` — production, не sources/dev.
- SHA-256: `AD393DCF1BD07624524BFD16C6F7F26A8A11C9A24D7E543D26C81C38B8CFA90F`.
- RESOURCES/JSON/REGISTRATIONS: PASS — `python tools/verify_resources.py`:
  10 предметов, 3 блока, 10 PNG, 5 рецептов, 10 видимых advancements, 5 открытий рецептов;
  связи, переводы, отсутствие циклов, PNG CRC, содержимое JAR, metadata, Java 8,
  наличие переименованного `func_78016_d` в production-классе вкладки.
- CLIENT: PASS (development client). Мир «Новый мир» реально загрузился;
  в логе есть вход Dev и выдача новых достижений. Сохранение содержит выполненные
  достижения новых ресурсов. Способ получения предметов этим не подтверждается.
- WORLDGEN: PASS для наличия ресурсов в сохранённом мире. Прочитаны 780 чанков:
  247 льна, 133 полыни, 97 перунитовой руды (Y=1..13) и 2531 алмазной руды.
  Выборка могла содержать изменения игрока; это не масштабный тест распределения.
- PERFORMANCE AUDIT: PASS по коду. Регистрация однократно, bounded vanilla worldgen,
  нет тиковых обработчиков и сканирования мира. GLM быстро отсекает не-fern loot tables.
  Бенчмарки MSPT, многопользовательская нагрузка и статистика многих seed не проводились.

## Исправлено при разработке

Первичная компиляция выявила несовпадение перегрузки BiomeGenerationSettings.Builder
addFeature с Supplier в API 1.16.5. Использован фактический Forge API
getFeatures(stage).add(supplier); последующая полная сборка успешна.
Генератор и проверяющий скрипт обновлены под 0.2.0; старые рецепты не восстанавливаются.

## Ограничения и реальные сообщения

Первый dev-run сохранил мир и завершил Minecraft, после чего Gradle сообщил
`Could not receive a message from the daemon`. Причина не установлена; это не скрыто
за BUILD PASS (отдельная production-сборка завершилась с кодом 0).
Повторный dev-run снова загрузил атласы и клиент, но при закрытии повторилась потеря
связи с daemon. Журнал daemon заканчивается сообщением о завершении JVM
(normal exit or user interrupt), без crash report; причина не установлена.
Realms отклонил dev-session
(DONT_CRASH); это ожидаемо для среды разработки и не ошибка ресурсов мода.
Computer Use смог найти окно, но снимок окна не получен из-за таймаута;
CLIENT PASS основан на фактической загрузке мира/логах, не на вымышленном скриншоте.

Полный крафт в survival, инструменты и enchants, частота редкого drop,
выделенный сервер и production-запуск через обычный launcher **НЕ ПРОВЕРЕНЫ**.

## PolyMC

Обнаружен установленный PolyMC 7.1. JRE 8u503 по настроенному пути существует.
Исходный профиль `1.16.5` использует Forge 36.2.34 (моду нужен 36.2.42),
содержал только instance.cfg и mmc-pack.json. В логе ошибка загрузки метаданных.
Сервисы метаданных Minecraft 1.16.5 и Forge 36.2.42 доступны по HTTP 200.

Подготовлен отдельный профиль, исходный не изменялся:
`C:/Users/user/AppData/Roaming/PolyMC/instances/Slavic-Myths-0.2.0`.
В нём Minecraft 1.16.5, Forge 36.2.42, Java 8, 512–3072 MB RAM и
`.minecraft/mods/slavicmyths-0.2.0.jar`. Профиль виден в UI PolyMC.
Загрузка игры/зависимостей и запуск этого профиля пока не проверены.

Протоколы: `verification/gradle-build-0.2.log`, `resources-check-0.2.log`,
`dev-client-0.2.log`, `dev-client-0.2-retry.log`, `world-sample-0.2.json`.
