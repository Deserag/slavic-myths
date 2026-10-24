# Оптимизация 24.10.2026 — IN_PROGRESS

Поручение: интегрировать накопленные изменения в main, обновить тестовый PolyMC, затем начать оптимизацию в отдельной ветке. Main и установка выполнены до изменения EarthCamera: [квитанция](../polymc-19762/installed.json). Оптимизация находится в рабочей копии codex/performance-20261024 без нового коммита, main/origin/main = 1bed9897. Полная оптимизация кодовой базы не объявлена завершённой.

## Изменение

На каждом вызове ComputeCameraAngles раньше создавались два списка результатов, type-test объекты и два одинаковых AABB. Теперь списки и type-test используются повторно, AABB один; indexed loops не создают итераторы. finally очищает ссылки на сущности после каждого вызова, включая исключение. Результаты запросов не кэшируются между кадрами: движение, появление/исчезновение и spectator-фильтр по-прежнему учитываются на каждом кадре. Стандартный публичный Level.getEntities(type,bounds,predicate,list) проверен по установленным NeoForge sources и компилятором. Число spatial queries осталось прежним — два на кадр.

Неиспользуемые временные результаты удалены из пути кадра. Массового удаления файлов не было: отсутствие текстовых обращений недостаточно для удаления event subscribers, registry/mixin/reflection code. Изменены только EarthCamera и сопровождающие offline проверки/документация. Art, баланс, IDs и ресурсы не менялись.

## Матрица

REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN
--- | --- | ---
Все накопленные локальные изменения сохранены и объединены с origin/main | commits97e78e40 + merge + a0917008 + receipt1bed9897, origin/main verified | PASS
Тестовый PolyMC main1.9.7.62 установлен с backup и SHA256 | ../polymc-19762/installed.json; installed hash повторно проверен | PASS
Отдельная ветка после установки | codex/performance-20261024, base1bed9897 | PASS
Повторное использование результатов query, без удержания entities | EarthCamera.java; finally; native-query.json exception/empty/moving cases | PASS source/offline
Формулы/тайминги тряски сохранены | before/EarthCamera.java; source-checks.json, normalized source comparison | PASS static
Порядок, типы и фильтры native query | 820 offline checks; EntitySectionStorage/LevelEntityGetterAdapter fixture, раздельные Actor/Boss types, вне радиуса и spectators | PASS offline
Java21 clean build | build-offline.log: BUILD SUCCESSFUL59s | PASS
Production JAR/resources/harness isolation | resources.log; artifact.json; source-checks.json,7688 resources/1840 classes | PASS
Measured caller allocation | native-query.json; Java21 ThreadMXBean,10000warmup +3×20000frames, minimum allocated bytes | PASS fixture only
Реальное влияние на FPS/MSPT/плавность | Игровые процессы не запускались, разрешения нет | NOT_RUN
Визуальная игровая регрессия и owner acceptance | 0 запусков Minecraft/PolyMC/GameTest/server | NOT_RUN
Полная оптимизация всех подсистем | Начат один ограниченный пакет; причин всех тормозов ещё не установлено | NOT_RUN

## Границы измерений

Это native section-query fixture с лёгкими EntityAccess объектами, не настоящий ClientLevel, не full camera event и не игровые измерения. Проверяет повторное использование списков на штатном entity storage; формулы проверены отдельно по actual source. Тестовая выборка содержит оба типа, spectators и сущности вне bounds. Нет обещания нулевых аллокаций всего обработчика: сам native query и AABB продолжают выделять память. Цифры JVM зависят от JIT; проценты не экстраполируются на FPS.

Fixture entities | Before bytes/query-pair | After bytes/query-pair
--- | --- | ---
0 | 92.5776 | 0.0
1 | 840.0 | 704.0
64 | 1264.0 | 704.0
512 | 4880.0 | 704.0

Окончательный fixture уточнён после первого прогона: раздельные классы Actor/Boss, один entity case включает видимую цель. Финальные цифры только native-query.json/offline-final.log. Первичная ошибка source checker касалась выделения закрывающих скобок, исправлен сам checker; формулы не менялись.

## Дальнейшая диагностика

Для установления причин тормозов нужны реальные записи FPS/frame time или MSPT по воспроизводимой сцене; в текущем поручении игровые запуски запрещены. Статические кандидаты для отдельного измерения: TaleEvents.seen (повторные LOS/award при уже известном bandit), PolozCamera (list+stream для поиска одного результата). Они не изменены и не выданы за подтверждённые причины. Архивные модели и доказательства не удалять как якобы runtime ballast: они не входят в production JAR.

Краткая ручная проверка владельца: обычный мир без земляного духа; TREMOR/DEBRIS; PHASE/AWAKEN; уход за16 блоков; смена измерения/выход; сравнить camera shake/FPS в одинаковой сцене. Для этих правок нужен отдельный тестовый кандидат после разрешения на установку.
