# Единое поручение #261–#267: актуальная сверка 27.10.2026, 21:09 МСК

Проверены обновлённые верхние разделы всех семи Issues (updated_at17:55UTC),
актуальный strict и доступные вложения. Последнее поручение разрешает исполнить
все этапы в одном задании, без нового разрешения между ними. Оно не отменяет
источники/качество/запрет игры и приёмку владельцем. Исходники следующего этапа
не создавались под видом «технической подготовки».

## Подтверждённые факты

- `hell` HEAD02765c5ebcbced3595181b8ada8d01cdda0e1ad0, local main8789e08d.
  Выполнен безопасный fetch origin/main: remote main **ba5c07cbf389dd164707de10797853c0c07db2e0**.
  Локальная main и рабочая hell не переключались/не сливались. Предыдущие delta
  сохранены, новых production-правок в этом продолжении нет.
- Исправлена предпосылка старого аудита: частичная печать действительно есть
  в **свежем origin/main**, хотя её нет в устаревшей local main и hell.
  Прочитаны реальные `blackcinder/ZharenitsaSeals.java`, `item/ArmorSetHealth.java`
  и boss spec из GitHub. ID `slavicmyths:zharenitsa_heat_seal`; custom data key
  `slavicmyths_zharenitsa_heat_seal`, modifier `zharenitsa_seal_health`.
  Реализация копирует iron/diamond stack, помечает его, расходует1печать при sneak
  right-click smithing table, считает0/1/2/3/5HP. Это source review, **не runtime PASS**.
  Реализацию предстоит адресно перенести/интегрировать в #266 без merge всей main;
  повторную печать с другим ID не создавать.
- Сравнение refs/files: [branch-inventory.json](branch-inventory.json).
  Полные актуальные тексты #261–267 сохранены в `issues/`.
- **Физически доступных новых вложений нет.**
  [attachment-inventory.json](attachment-inventory.json):58файлов=57TXT+1JSON,
  ZIP0/PNG0. Последнее по времени вложение — прежний текст о Навьей роще.
  Поиск в Downloads/Desktop/Documents/attachments по имени/размеру8686150байт
  нового архива также без результата. Это не означает, что файл отсутствует в
  библиотеке ChatGPT; означает, что байты не переданы в локальный доступ Codex.
- Новые верхние разделы сами подтверждают: запись ZIP в библиотеке найдена,
  байты не получены; без PNG разрешена **только независимая техническая подготовка**.
  Запрошен абсолютный путь/доступная ссылка, разрешение на реализацию повторно
  не запрашивается. Отдельное полное ТЗ, указанное как приложенное, также не поступило.
- Существующий verifier вызван ещё раз:
  [latest-reference-gate.json](../latest-reference-gate.json) — все4originals missing.
  Предыдущие9119PNG/19dimension candidates/0SHA matches сохраняют силу как прошлое
  свидетельство, не выдаются за повторный прогон. Никакие оригиналы не подменены.

## Матрица всех этапов

| REQUIREMENT / ISSUE | APPROVED_IMAGE_SHA | REAL_SOURCE / EVIDENCE | ACTUAL TEST | RESULT |
|---|---|---|---|---|
| #262 оригиналы4PNG, размеры/SHA/фактический осмотр | все4изstrict заданы, actual SHA отсутствуют | latest-reference-gate.json; attachment-inventory.json | Файлы недоступны, verifier exit1 | BLOCKED_REFERENCE |
| #262 source inventory/техконтракт | не ART | branch-inventory.json; ../CONTRACT.md; snapshots | Сверка local main/remote main/hell, source чтение | PASS (technical subset) |
| #262 texture sheet/пропорции/native baseline | original отсутствует | нет | Без PNG не делать art; игра запрещена | BLOCKED / NOT_RUN |
| #263 пять природных семейств/каменные формы/дым/ресурсы | blocks_REVISED actual SHA отсутствует | production не добавлен | Зависит от SOURCE/art #262 | BLOCKED_DEPENDENCY |
| #264 чернодрево2log variants/все формы/крона/FallingTree | blocks_REVISED actual SHA отсутствует | production не добавлен | Зависит от #262/263; runtime0 | BLOCKED_DEPENDENCY |
| #265 четыре уникальные3Dмодели/UV/позы/AI/атаки/audio | mobs actual SHA отсутствует | production не добавлен | Роли подтверждены ТЗ; моделей/поведения нет | BLOCKED_DEPENDENCY |
| #266 Жареница750HP/2фазы/4атаки/head/rune/soul/seals | zharenitsa actual SHA отсутствует | source частичной печати только origin/main | Отдельная печать не весь босс; в hell не перенесена | BLOCKED_DEPENDENCY |
| #267 natural terrain/4spawns/guaranteed arena/defeat state/locate | landscape actual SHA отсутствует | production не добавлен | Последняя зависимость ещё не обеспечена | BLOCKED_DEPENDENCY |
| #267 ≥5seed×4окна/old save/MP/perf/реальные кадры | n/a | нет новых миров/логов | Minecraft/сервер/GameTest запрещены;0запусков | NOT_RUN |
| BUILD существующей базы после предыдущего compile-fix | n/a | ../build-result.json; SHA03547c81…ead53 | Предыдущие Java21 clean build/resources/JAR PASS; не повторялись для docs | PASS (prior unchanged game source only) |
| Установка/owner acceptance | n/a | не выполнялись | Нового биома нет, приёмка не заявлена | NOT_RUN / OWNER_PENDING |

## Исправлено / не сделано / риски / следующий шаг

Исправлена source-инвентаризация свежего main: печать найдена. В этом продолжении
изменены только снимки актуальных требований/доказательства/статусные абзацы.
Весь функциональный пакет второго биома **не реализован**; команда поиска ещё
не существует. Не выдавать `/locate biome slavicmyths:black_cinder` за рабочую
команду до регистрации фактического biome ID. JAR не установлен, миры не менялись,
коммит/push/Project Status/Issue state не менялись.

Для отката удалить только single-run-update/latest-reference-gate и добавленные
статусные абзацы; предыдущие source/doc delta не трогать. Главный риск — продолжить
с выдуманным art, вопреки строгому верхнему разделу обновлённых задач.
Следующий шаг: получить **байты ZIP и полного ТЗ**, проверить SHA/размеры и открыть
каждый PNG, затем продолжать весь пакет по зависимостям без новых поручений.

## Как проверить в Minecraft — сейчас NOT_RUN

Текущего игрового кандидата нет. После реальной реализации и успешных gates
потребуются exact JAR path/version/SHA/source, отдельный новый Survival-мир и копия
старого. Без OP: портал → найти гарь → пройти безопасный маршрут → добыть/поставить
материалы → увидеть четыре естественно возникших вида → найти арену → проверить
две фазы, четыре атаки и все награды. Негативные
сценарии: нейтральный дух до провокации, мирный Искровик, камень без burn damage,
нет повторного boss/loot после reload; relog/MP/снятие печати/сохранённые чары/trim.
Сохранить старые блоки/порталы на копии старого мира. 5 seed × 4 разнесённых окна,
координаты/скриншоты/логи/perf обязательны после отдельного разрешения запуска.
Все эти игровые проверки, art/UV/анимации/звуки и actual natural spawn сейчас
**НЕ ПРОВЕРЕНЫ**. Previous clean build их не подтверждает.
