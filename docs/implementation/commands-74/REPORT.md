# #74 — единые команды: IN_PROGRESS / OWNER_PENDING

После #73, source base09d1d844.37 production registrars (включая Events/Residence/
Showcase) теперь передают builders в локальный CommandTree; единственный
RegisterCommandsEvent — SlavicCommands. Новый root собирается при каждом reload,
без static dispatcher. Сервисы, команды/аргументы и ограничения генераторов
сохранены; синтаксис меняется через совместимые пути.

Канонический root /slavicmyths; административные операции /slavicmyths dev.
/sm — один корневой redirect; /smclass и2 точных custom /locate structure сохранены.
OP2/конфигурация и исходные ancestor predicates переносятся на конечные узлы;
guard повторяется при выполнении. Нуждающиеся в игроке действия отделены от
консольных; clear_kurgan_curse <player> остаётся доступна консоли.

Исходный raw dump:362 узла/267 endpoints, без init7 профессий; ограничения
этого снимка указаны в COMMAND_MIGRATION. Реальные текущие pre-normalization
contributions после init дают274 endpoints. Объединены26 подтверждённых повторов;
248 исходных канонических действий +2 help =250 (IDE-only Сивка включён в каталог;
в production mode249).305 compatibility nodes + /sm root redirect,647 узлов всего.
Ни один исходный исполняемый путь не удалён. Прежние guard-config различия
у похожих команд сохранены, а не названы дублями.

Все647 узлов, аргументы/границы/examples/custom suggestions, исходные handlers,
контексты OP0..4, player/console, aliases и descriptions включены в registry.json.
Полный справочник — docs/COMMANDS.md; точная миграция — docs/COMMAND_MIGRATION.md.
Инструмент command_reference.py --check выявляет дрейф документации/исходников,
новая команда без явного описания вызывает ошибку.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Исходный каталог и все37 источников | registrars-before.json; tree-before.json; contributions.json | PASS |
| Один root/composable builders; исходные services | SlavicCommands/CommandTree; source diff | PASS |
| Дубли, алиасы, миграция всех старых вариантов | aliases.json; COMMAND_MIGRATION.md; parse spies274 endpoints | PASS |
| OP0..4/player/console/config/IDE, reload и один callback | checks.json;9057 scoped assertions | PASS |
| Help RU/EN и аргументы/автодополнение | Реальное выполнение help; class/help completion; bounds/UUID tests | PASS |
| Полный реестр соответствует справочнику | command_reference.py --check | PASS |
| Java21 clean build, ресурсы и production JAR | FINAL_BUILD.txt (1m6s); jar-check.json; verify_user_text.py --jar | PASS |
| Реальный event bus/dispatcher на запущенном сервере, selectors/world completions | Запусков0; RUNTIME_NOT_RUN | NOT_RUN |
| Ручная приёмка владельцем | Issue остаётся OPEN; owner acceptance не подменяется | NOT_RUN |

Plain JVM использует настоящие production builders и Brigadier. Player identity —
не сконструированный type token для instanceof; не настоящий игрок/сервер.
Для274 вариантов проверяется разбор каждого прежнего вызова и единичный вызов
считающего spy вместо игрового callback. Это не проверка world side effects.
Brigadier completion и server-tree canUse проверяются отдельно: сам raw dispatcher
может перечислять недоступные literals, клиентское дерево фильтрует Minecraft.
Реальная передача дерева/сетевая подсказка и выбор онлайн-сущностей NOT_RUN.

Полный источник Морозко/зеркала, art/audio/registry IDs не меняются этой задачей;
отдельное исправление посоха по новому сообщению владельца описано рядом:
../frost-staff-targeting/REPORT.md. PolyMC не устанавливается этой задачей.
Ручная проверка: help безOP/OP, старые aliases, команда с <player> из консоли,
перезагрузка ресурсов; генераторы проверять только в отдельной копии мира.
