## Результат сборки после публикации QA

Java 21 GitHub Actions [run 39741599767](https://github.com/Deserag/slavic-myths/actions/runs/39741599767) завершён SUCCESS: pure-policy 61489 PASS, Gradle clean build PASS, JAR `slavicmyths-1.9.7.53.jar`, SHA256 `ba866e16acf2c305bafac400bdb8f16b7f33bfedbe13871dd9a2b16baea79d49`. JAR доступен в Actions artifact до 29.10.2026; не установлен и не проверен в игре.

# #193 — единая проверка исходников и критерии ручной приёмки

Дата: 22.10.2026. Ветка: `feat/likho-193-forest-spawn`. В этом пакете не изменяются HP, поведение боевых навыков, модели, ID, spawn-шансы или урон. Добавлены **только воспроизводимые проверки**.

## Java 21 offline policy
`tools/likho193-offline/Likho193Offline.java` тестирует опубликованные неизменённые pure Java-классы (НЕ копии production): `BossKind`, `BossRules`, `LikhoCombatRules`, `LikhoGazeRules`, `MorPresenceRules`, `HigherRules`, `MorExposure`. По 5 seeds и 6144 регионов на seed: вероятности по **гипотетическим однородным биомам** 60/15/10%, сохранение шанса Тугарина (25%) и стабильных кандидатных координат. Не является тестом настоящей карты биомов, посещённых регионов или игрового worldgen.

Запуск локально (Java 21):
```sh
mkdir -p build/likho193-offline
javac --release 21 -encoding UTF-8 -d build/likho193-offline \
  src/main/java/org/slavicmyths/hunt/{BossKind,BossRules,LikhoCombatRules,LikhoGazeRules}.java \
  src/main/java/org/slavicmyths/mor/{MorPresenceRules,HigherRules,MorExposure}.java \
  tools/likho193-offline/Likho193Offline.java
java -cp build/likho193-offline org.slavicmyths.verify.Likho193Offline
```
Java 21 локальный PASS: 61489 checks, 30720 регионов, dark 18413/30720, forest 4707/30720, birch 3174/30720, Tugarin 7616/30720. Проверка `P0-1` берёт чистое решение шанса из source и НЕ моделирует реальное распределение биомов, высоту/землю, chunk load и сохранение мира. P0-2 и P1-3 policy-pass НЕ доказывает успешное нанесение урона/эффекта в Minecraft. Проверка антистака только по интервалам; native immunity/stacking требует сервера.

## Автоматическая сборка
Веточный `.github/workflows/likho193-java21.yml` выполняет проверку policy, затем Java 21 `./gradlew --no-daemon clean build --stacktrace`, проверяет production JAR и публикует build/libs JAR как краткоживущий Actions artifact **только при успешной сборке**. Это компиляция/упаковка, **не игровой запуск**. Если Actions не стартовал или FAIL, считать `BUILD_NOT_VERIFIED`, исправлять source по конкретным логам. Не менять `main` и не устанавливать JAR в PolyMC автоматически.

## Остаток, который нельзя отметить без владельца

- **P0-1 QA**: реальное распределение по dark/regular/birch лесам, новые/старые регионы, сохранение точек после relog, отсутствие повторного спавна, Тугарин, ночь, 7 дней, защита natural vs quest, Моровый вариант.
- **P0-2 QA**: Survival, ground, ровно 5 блоков, старт после 1 секунды и 1 HP/2 секунды, короткий прыжок/длинный полёт, LOS/стена, алмазная броня; два Лиха, две зоны, mor ground+cloud, Creative/Spectator, команды, союзники, peaceful, смерть/выгрузка хозяина, dedicated server, MP/performance.
- **P1-3 QA**: GAZE LOS+взаимный взгляд, 8 секунд Fate, Weakness 6, Blindness 1.25, Nausea 3.5, 240-tick визуальный лимит на игрока при двух Лихо, повторный вход и смерть, амулеты/снятие эффектов, отсутствие GAZE на иных мобах, GRAB не изменён.
- **P1-4**: реальные iron/iron и diamond/netherite × normal/mor, 3 повтора до 5 минут, щит/уклонения/LOS, `-Dslavicmyths.likho.dps=true` и `latest.log`; не менять raw damage до публикации игровой статистики и отдельного решения владельца.
- **P2-5**: Java21 clean build + production JAR SHA и ресурсная сверка, owner-игровой тест на отдельной копии мира, отчёт об установке с backup/no duplicates при добровольной установке владельцем.

**Статусы:** source/policy VERIFIED отдельно от build; все game runtime проверки `NOT_RUN / OWNER_PENDING`. Issue #193 остаётся OPEN; owner-accepted / «Готово» / merge запрещены до приёмки.
