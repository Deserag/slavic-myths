> Исторический первый проход SOURCE. Позднее владелец разрешил независимый production-код без PNG; актуальные изменения и результаты: [новый отчёт](../lost-halls-2.0.4/REPORT.md).

# Чертоги заблудших 2.0.4 — начало исполнения, 28.10.2026

**IN_PROGRESS; BLOCKED_REFERENCE для художественной части. Биом не реализован.**
Независимая техническая подготовка выполнена; это не сдача #289 или всей цепочки.
Новые игровые механики не добавлялись. Весь scope остаётся порученным владельцем,
повторная команда после каждого этапа не требуется.

## Сделано

Получена `hell` из GitHub, отдельный рабочий checkout сохраняет старую грязную
`master`. Адресно перенесены 14 файлов контрактов/чертежей из origin/main,
без merge и изменения main. Сохранены снимки 10 порученных Issues в `issues/`.
Точные версии, пути, реальные интеграционные точки и риски — [baseline](baseline.md).

Добавлены исполняемые инструменты:

- `tools/audit_lost_halls_sources.py`: source/resource inventory, collision-кандидаты,
  manifest SHA256 всех src/main и проверка отсутствия production diff.
- `tools/check_lost_halls_references.py`: проверяет пять утверждённых SHA256,
  PNG decode/размеры, читает архивы без распаковки, ограничивает размер входа,
  не принимает совпадение имени за эталон и возвращает ненулевой код при блокере.
- `tools/test_lost_halls_references.py`: 8 тестов, включая плохой ZIP, подмену,
  повреждение, неверный размер, недостающий файл и чтение ZIP без записи наружу.

Проверено 469 PNG в доступных attachments, Downloads и docs/concepts.
Все пять оригиналов отсутствуют; ошибок чтения в этом поиске нет.
Это ограниченный поиск в перечисленных местах, не утверждение об отсутствии
файлов на всех дисках/в другой беседе. См. [результат](evidence/references.json).

Исправлена настройка Maven в build.gradle: Curios/JEI repositories ограничены
своими группами. Первая сборка пыталась скачать JEI через Curios и завершилась
сетевым таймаутом. Версии зависимостей не менялись; исходный fail-log сохранён.
Итоговые build/resource/JAR результаты фиксируются в `evidence/build-result.json`.

**Итог сборки:** обычный clean build повторно FAIL из-за недоступного Curios Maven
(его адрес подтверждён [официальной документацией](https://docs.illusivesoulworks.com/curios/getting-started)).
С локальным init-script и тем же Curios 9.5.1+1.21.1 из Modrinth, после проверки
SHA512 — clean build PASS за 28s. Первую ошибочную версию локального init-script
исправили; её fail-log также сохранён. Проверка resources/JAR PASS: 8254 ресурса,
2004 Java21 класса, 4624 JSON, 1474 PNG. Test/smoke классов в production нет.
Gradle test — NO-SOURCE, это не прохождение игрового теста. Компилятор показал
100 предупреждений прежних deprecated/unchecked API; они не исправлялись вне scope.

JAR: `build/libs/slavicmyths-2.0.8-HELL.jar`, SHA256
`872c3f468b5c626faafc4aeb32b500da2b7214f685905178ac93d9c52778be18`.
Это сборка существующих двух биомов, НЕ реализация третьего.

## Матрица текущего этапа

| ID | REQUIREMENT / SOURCE | EVIDENCE / ACTUAL | RESULT |
|---|---|---|---|
| G01 | #289.1, hell/HEAD/сохранение локальной работы | baseline.md; original-checkout-status.txt | PASS |
| G02 | #289.1, фактический inventory | registry_inventory.tsv, inventory.json; static scope явно указан | PASS (static only) |
| G03 | #289.2, 3 исходных PNG и SHA | references.json: blocks/scenes/mobs отсутствуют | BLOCKED_REFERENCE |
| G04 | #289.3, камень №3, approved/rejected sheet, палитра | нет исходного blocks PNG; текстуры не выдумывались | BLOCKED_REFERENCE |
| G05 | #289.4, floorplan/3 сечения/проходы по оригиналу | нет scenes PNG; геометрия не изготовлена | BLOCKED_REFERENCE |
| G06 | #289.5, ID plan/ownership | id-plan.tsv, source collision scan; runtime registry не запускался | PASS (static proposal only) |
| G07 | #289.6, native renderer before/after | Minecraft запрещён, 0 запусков | NOT_RUN_ENV_BLOCK |
| G08 | #301/#293/#303, rewards/posten originals | references.json: оба PNG отсутствуют | BLOCKED_REFERENCE |
| G09 | #302, оригиналы Сторожа/Жареницы/Постеня | original art не получен; существующая zharenitsa_head не = новый контракт | BLOCKED_REFERENCE |
| G10 | проверка самого source validator | reference-validator-tests.log, 8 tests OK | PASS (tool tests only) |
| G11 | сохранность production | inventory.json: production_diff/untracked_production пусты | PASS (source only) |
| G12a | обычный Java21 clean build | clean-build-retry.log: Curios repository timeout | FAIL_NETWORK |
| G12b | clean build с локальным точным Curios | clean-build-local-curios-retry.log, build-result.json | PASS (local dependency fallback) |
| G12c | ресурсы/production JAR | resources.log, 8254 resources/2004 classes, SHA выше | PASS |
| I02–I08 | #290–#293/#301–#303, полный контент | production третьего биома отсутствует | BLOCKED_DEPENDENCY |
| Q01–Q14 | #294 natural worldgen/бой/MP/старый мир/ART | ни один игровой тест не запускался | NOT_RUN_ENV_BLOCK |
| OWNER | ручная приёмка владельца | нет | NOT_RUN |

Строки Q01–Q14 не заменяются числом unit assertions или успешной сборкой.
Полная построчная исполнительская матрица следующих этапов составляется по их
фактическому коду; текущая таблица не объявляет их требования исполненными.

## Что нужно для продолжения художественной части

Приложить `Чертоги_заблудших_2.0.4_жёсткое_ТЗ_референсы.zip` с оригиналами
blocks/scenes/mobs. Также нужны оригинальные PNG наград и Постеня из отдельных
архивов; для #302 — утверждённые оригиналы двух предыдущих боссов.
Пути и пять точных SHA уже находятся в `evidence/references.json` и контрактах.

Основание: STRICT_ACCEPTANCE §1 — «Если не получил — остановить художественную
часть, BLOCKED_REFERENCE»; #289 — «Не начинать блоки до принятого baseline/texture
sheet». Независимые tooling/build/inventory выполнены. Продолжать игровой контент
с заменой рисунков или невидимыми placeholder-предметами нельзя.

## Как проверить в Minecraft

**Сейчас запуск не выполнялся и не требуется для проверки этих tooling-изменений.**
Новая сборка, если build gate успешен, содержит прежний контент 2.0.8-HELL,
не Чертоги; POLYMC_NOT_INSTALLED. Не искать в ней отсутствующий третий биом.

После реализации и разрешения runtime обязательный маршрут владельца:

1. Отдельный Survival-мир: без OP построить портал, найти Чертоги, пройти S1–S6
   и вернуться; обязательный путь не требует ломания блоков/полёта/лавы.
2. Добыть материалы обычными инструментами, создать формы/метки/камертон;
   неверная опора/полный инвентарь/две руки не дают дюпа, камератон сообщает no target.
3. Найти пять естественных видов, приручить и вылечить Гулокрыла, мирно получить
   склянку/споры; проверка unload и настоящего перезахода сохраняет владельца/cooldown.
4. Найти арену пешком; Постень 650 → split 4×65 при 260, одна полоса и один batch
   наград; клоны не дают вторые трофеи, после перезахода босс не возрождается.
5. Проверить руну 80/120 ticks, три боевые копии, их урон/исчезновение; три разные
   головы на полу/стене, включение/остановку масок Постеня на дистанциях 6/8.
6. Seeds 42041–42045 × четыре заранее записанных разнесённых окна, все пять vanilla
   биомов и оба прежних; доступная арена каждой полной области, FPS/MSPT, old world
   только в копии, два клиента и anti-dupe. Сейчас все эти пункты NOT_RUN.

## Риски/откат и следующая работа

Сохранения и установленный JAR не затронуты; откат новых tooling/docs адресный,
без reset/delete пользовательской работы. Две строки build.gradle ограничивают
репозитории заявленными группами; при новых dependency groups нужно явно расширить
фильтр. Следующий шаг — получить PNG, проверить SOURCE, затем закончить #289
и перейти к блокам #290 в уже порученной последовательности.
