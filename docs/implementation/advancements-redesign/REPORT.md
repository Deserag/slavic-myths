# Этап A: аудит и проект для рассмотрения

Дата: 15.10.2026. [Issue #30](https://github.com/Deserag/slavic-myths/issues/30). База `main@fb2994e958911169317523e1066b8a1ca7a155af`, повторно проверена перед публикацией. Ветка `codex/advancements-redesign-design`, отдельный managed worktree. Изменения исходной папки Classes3 и открытых PR #25/#27 сохранены.

## Результат

- [AUDIT.md](AUDIT.md): решения по каждому из129, исходные RU/EN, trigger/requirements/parent/category/stage, аргумент, successor, main dispatch, save/test policy; полный состав босcов/элит и границы их зачёта.
- [NEW_TREE.md](NEW_TREE.md): **55 карточек** в шести ветвях (1+6+48), полный display-контракт RU/EN с условиями, этапами/иконками/flags; [SVG](TREE.svg) для визуального рассмотрения.
- [MIGRATION.md](MIGRATION.md): сохранение всех206 старых JSON/criteria/requirements/rewards, data-only85, technical77, tales234; silent import и downgrade ограничения; QA следующего PR.
- [SOURCE.json](SOURCE.json), [DECISIONS.json](DECISIONS.json), [TREE.json](TREE.json): исходный snapshot и точное соответствие. 44 старых display ID, 11 новых; решения24 RELOCATE/77 MERGE/13 KEEP/8 RETIRE/7 REWORK. RETIRE означает убрать карточку, не удалить ID/данные.

## Реально выполненные проверки

| Проверка | Результат |
|---|---|
| Java21 `gradlew.bat clean build` | PASS,29s; [BUILD.log](BUILD.log). Обычный Gradle test имеет NO-SOURCE: не выдаётся за серверные тесты |
| Документация / source / resources / production JAR | PASS,20605 проверок; [VERIFY.log](VERIFY.log), [VERIFICATION.json](VERIFICATION.json) |
| Полнота решений,55 карточек,6 ветвей, отсутствие циклов/потерянных parent, существование иконок | PASS, статически |
| Исходные206 advancement JSON не изменены | PASS, SHA256 и полная структура snapshot; PR содержит только docs |
| Технические77 / tales234 | Сохранены в исходном коде и JAR, не мигрировались |
| JSON/PNG/NBT/OGG текущих ресурсов | 3919 JSON parsed;1325 PNG signatures;768 NBT +788 OGG — побайтная parity с JAR. NBT/аудиодекодирование и визуальный вид этим не проверяются |
| Production classes | 1685 class files Java21; каждый outer class имеет source в main, test fixture packages отсутствуют |
| Установленные NeoForm исходники | optional display / native persistence / initial reset toast guard подтверждены статически, hashes в VERIFICATION |
| Серверные/клиентские запуски этой задачи | **0 / 0** |
| Runtime migration / boss lifecycle / два клиента / реальные GUI / реальный перезаход | **NOT_RUN**, относятся к отдельному implementation PR |
| MSPT/FPS | **NOT_MEASURED** |

Production baseline JAR: `build/libs/slavicmyths-1.9.7.5.jar` внутри этого worktree; SHA256 `77b79947b0c0df4ae2157410d7fa25c5ec225ecc9700fd603b04d3bf4cda887f`. Это сборка **нынешнего main**, не реализация нового дерева. Ничего не установлено в PolyMC; миры не открывались. Нативная допустимость data-only проверена по API, zero-loss runtime migration пока не доказана — это явный gate этапа B.

## Повторение проверки

На той же базе с доступным read-only ref `origin/pr25`:

```powershell
python -X utf8 docs/implementation/advancements-redesign/prepare_design.py
# JAVA_HOME задаётся только в этом процессе команды сборки
$env:JAVA_HOME='C:/Program Files/Java/jdk-21.0.12'
.\gradlew.bat clean build
python -X utf8 docs/implementation/advancements-redesign/verify_design.py
```

`prepare_design.py` пишет только эту директорию документации; production generators не вызываются. `verify_design.py` читает production ресурсы/JAR и пишет только VERIFICATION.json. Для другого HEAD повторный аудит обязателен, нельзя молча регенерировать исходный snapshot и назвать его тем же baseline. Верификатор не является заменой CI/runtime #25; #25 пока открыт, его checks будут сохранены при реализации.

## Что требуется для продолжения

Рассмотрение владельцем дерева55, таблицы решений и правил миграции. Затем отдельный PR: обновление исходников генерации/JSON/RU/EN, точечные server-award adapters, native GameTest и безопасный import, реальная UI-приёмка с разрешённым запуском клиента. До этого реализация не начинается, issue30 не закрывается, auto-merge не выполняется. Новый mob AI/бой/worldgen/оружие/модели/артефакты не входят в этот scope.
