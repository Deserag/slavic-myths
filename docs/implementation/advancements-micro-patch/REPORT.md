# Issue #22 — микропатч достижений

База: `origin/main` / `fb2994e9`; отдельный worktree `D:/slavic-myths-advancements`, ветка `codex/advancements-micro-patch`. Пользовательские незакоммиченные материалы Classes 3.0 остались в исходной папке. Версия 1.9.7.5, Minecraft 1.21.1 / NeoForge 21.1.255 / Java 21 сохранены.

| ID | Подтверждённый дефект | Причина | Исправление | Проверка |
|---|---|---|---|---|
| lore_keeper | Недостижим обычным открытием книги | Единственный award находился в больше не вызываемом Knowledge.mask | Проверка трёх разных прежних Knowledge ID при принятом открытии книги; без synthetic awards mask | Реальный сервер: два ID, включая дубликат depth_gift, не дают достижение; три без книги не дают; три с книгой в offhand дают |
| kitchen_meal | Нет ни одного dispatch для impossible criterion | Кухонный результат и выдача порции не сообщали о получении | Award после взятия основного результата (обычно/Shift) либо принятой выдачи порции | Реальный сервер: пустой выход/котёл, неверная посуда и остаток рецепта не дают; обычное/Shift-взятие и порция дают |
| claimed_water_fragment / claimed_fire_fragment | Получение осколка не выдаёт достижение | FolkAccessoryItem.award всегда выдаёт event, но оба JSON требуют claim | В принятой транзакции GuardianLedger вызван Knowledge.award, без смены criteria и наград | Native claim обоих типов; READY/standalone/spectator/full inventory отклонены, receipt и дубликат проверены |
| shrine | RU/EN обещают несуществующий путь через книгу | Подсказка осталась от mask, действующий источник — IdolBlock | Уточнён текст взаимодействия с идолом; генератор синхронизирован | Аудит действующих вызовов; reconcile не выдаёт shrine по ancient_find |
| lore_keeper, kitchen_meal | Неясные условия описания | «Три записи» смешивались со всеми записями новой книги; кухня не указывала получение результата | RU/EN уточнены, оба генератора синхронизированы | Полный паритет visible title/description; native tests выше |

## Полный аудит

На актуальной базе **206 JSON: 129 игровых узлов и 77 recipe unlocks**, а не историческое число 130 из Issue. Ни один JSON достижения не изменён и не удалён. Граф ацикличен, родители существуют; один собственный root, девять доступных тематических разделов. Шесть прежних игровых узлов входят в vanilla husbandry, recipe unlocks — в vanilla recipes/root. Это осмысленные внешние связи, сохранены. Корневые святилища также сохранены: убедительного дефекта в их положении нет. Скрытые `black_feather`, `great_poloz_fall` остаются скрытыми.

`AUDIT.json` содержит все criteria, requirements, родителей, исключения recipes/hidden/vanilla и конкретные пути server dispatch для каждого impossible. `CONDITIONS.md` — вся ведомость условий и RU/EN описаний. Все 234 tales проверены на ссылки advancements/links и локализацию заголовков. Все видимые достижения имеют RU/EN title/description; отсутствующих переводов не найдено.

`save-contract.json` фиксирует все 206 ID, полные criteria, requirements и rewards из базы. Автоматическая проверка запрещает их случайную замену/удаление. Namespace, criteria names, награды, Knowledge.ENTRIES/flags, формат данных и TaleService.reconcile не менялись. Дубликат depth_gift оставлен в старом bitmask-контракте; новая проверка открытия считает distinct IDs. Новых login awards, сброса и миграции не добавлено. Classes 3.0, оружие, руны, боевые механики боссов, art и мирогенерация не менялись; у GuardianLedger исправлен только вызов достижения после принятой награды.

## Воспроизведение

```powershell
$env:JAVA_HOME='C:/Program Files/Java/jdk-21.0.12'
./gradlew.bat clean build
python tools/verify_advancement_tree.py --jar
python tools/test_advancement_audit.py
./gradlew.bat runAdvancementChecks -PadvancementCheck -PwithoutJei
./gradlew.bat runAdvancementChecks -PadvancementCheck -PadvancementPhase=verify -PwithoutJei
```

Для --jar нужен Pillow; статические проверки используют только стандартную библиотеку Python. Серверные fixtures включаются только с advancementCheck, используют отдельную run-advancement-checks и никогда не входят в production JAR. Первая команда runtime записывает vanilla advancement JSON фиксированного тестового UUID; вторая читает его новым процессом. Повторные прогоны безопасны в этом тестовом мире. Для совершенно чистого прогона используйте новый checkout. GitHub Actions автоматически запускает аудит и десять mutation tests на PR/push main.

## Реальные результаты

- `BUILD_RUNTIME.log`: clean build и **6/6** GameTests PASS; первый настоящий GameTest server.
- `RESTART_RUNTIME.log`: **7/7** PASS; второй настоящий GameTest server. Включает **37/37 inventory criteria**, каждый с отрицательным и положительным native INVENTORY_CHANGED trigger; реальные загруженные item tags/registries.
- Все три процесса: loader действительно загрузил все **206** advancements и **234** tales. Изменённые события проверены на настоящих ServerPlayer и ванильном прогрессе.
- `ADVANCEMENT_NATIVE_NEW_PROCESS_LOAD_PASS`: заработанные lore_keeper/kitchen_meal/meet_leshy сохранены между процессами; earth_heart остаётся незаработанным. Повторный reconcile сохраняет earned/unearned; записи восстанавливаются по действующим связям.
- `FINAL_BUILD_RUNTIME.log`: финальная **clean build PASS + 8/8 GameTests PASS**, третий сервер; обоим fragment claim проверены valid/READY/standalone/spectator/full inventory/duplicate и сохранение receipts.
- `AUDIT_TESTS.log`: **10/10** mutation tests PASS — проверены отказы при missing parent/cycle, изменении criteria, отсутствующих переводе/иконке/dispatch, неверном frame, несовместимом helper criterion, битых tale links и advancement refs.
- Первый boot сообщает отсутствие server.properties в новой тестовой директории; сервер создаёт настройки и успешно завершает тесты. Компиляция содержит прежние deprecation warnings NeoForge. Проваленных сборок/GameTests в этой задаче нет.

Финальные clean build + runtime после исправления guardian criteria: FINAL_BUILD_RUNTIME.log; JAR gates: **8489 PASS**, **221 event subscribers**; production JAR `build/libs/slavicmyths-1.9.7.5.jar`, SHA256 `c819ad10ad3aa3d0c578326f0950b9d21641d487966b2ca28c78f69361e66ae3` (AUDIT.json → production). Предыдущий FINAL_BUILD.log — успешная чистая сборка до последнего адресного исправления.

## Границы доказательств

Всего **3 настоящих GameTest server запуска**, **0 клиентов**, **0 PolyMC**. Это серверные автоматические тесты, а не полная игровая приёмка. Остальные event-only достижения проверены по реальному dispatch и условиям исходного кода, но не воспроизведены все бои/квесты/ритуалы. Локационные predicates прошли настоящий loader; посещение каждой структуры в Survival не моделировалось. Native inventory tests проверяют triggers и данные, а не добычу каждого предмета.

Сохранение проверено чтением реального vanilla advancement JSON новым процессом, не клиентским disconnect/reconnect. Пользовательский старый мир не загружался: совместимость основана на неизменных всех ID/criteria/rewards/форматах и проверенной native persistence. GUI, 2-client MP, настоящий client relog и длительное Survival прохождение **NOT_RUN**. Ручная приёмка пользователя не требуется для запуска поставленных воспроизводимых проверок; перечисленные области не объявляются проверенными.

PolyMC, пользовательские сохранения и исходный рабочий каталог не менялись. Merge не выполняется.
