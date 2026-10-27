# Пакет проверок «Славянская мифология» — 27.10.2026

Единая папка для передачи ChatGPT и составления дальнейшего плана. Рабочая ветка **hell**, текущая установленная тестовая версия **2.0.7-HELL**. Issues остаются OPEN, ручная приёмка владельцем не состоялась.

## Что отправить ChatGPT

1. [CHATGPT_BRIEF.md](CHATGPT_BRIEF.md) — готовое задание для анализа.
2. [ANSWERS.md](ANSWERS.md) — актуальные ответы, выводы и открытые вопросы.
3. [FULL_REPORTS.md](FULL_REPORTS.md) — все12основных отчётов последовательно одним файлом.
4. При необходимости — содержимое [snapshot/docs/implementation](snapshot/docs/implementation): все дополнительные таблицы, логи, JSON-квитанции, скриншоты и патчи. Для статистики/конкретного PASS открывать доказательство, а не полагаться на количество assert-ов.

## Карта отчётов

| Этап | Отчёт | Как читать |
|---|---|---|
| #286 предметы/механики/экономика | [items286](snapshot/docs/implementation/items286-20261026/REPORT.md) | Аудит, исправленные транзакции, предложения без ребаланса |
| #276 исходный аудит | [baseline](snapshot/docs/implementation/loot276-20261026/REPORT.md) | Исторический baseline, не описание конечного лута |
| #276 курганы/лагеря L1 | [L1](snapshot/docs/implementation/loot276-l1-20261026/REPORT.md) | Реализация шести типов, вероятности/ожидания |
| #276 расширенный лут | [all](snapshot/docs/implementation/loot276-all-20261026/REPORT.md) | Святилища, крепость, болота, глубины; runtime на этом этапе ещё0 |
| #276 отдельный server integration | [runtime](snapshot/docs/implementation/loot276-runtime-20261026/REPORT.md) | Ограниченный native scope, не полный плейтест |
| #269 комплексный старт | [основной](snapshot/docs/implementation/stabilization269-20261026/REPORT.md) | Каталог/механики/исправления/первые миры |
| #269 продолжение | [resume](snapshot/docs/implementation/stabilization269-resume-20261026/REPORT.md) | Дополнительные native проверки |
| #269 версия2.0.5 | [205](snapshot/docs/implementation/stabilization269-205-20261026/REPORT.md) | Установка, старые saves, новые природные цели |
| #269/#276 ритуалы MP | [MP](snapshot/docs/implementation/stabilization269-mp-ritual-20261026/REPORT.md) | Два настоящих клиента/перезапуски, ограниченный scope; позже MP-повторы остановлены |
| #269 кухня2.0.6 | [кухня](snapshot/docs/implementation/stabilization269-kitchen-20261026/REPORT.md) | Таймеры/тара/пакеты/перезапуск; исправление UI |
| #269 брожение2.0.7 | [брожение](snapshot/docs/implementation/stabilization269-brewing-20261026/REPORT.md) | Натуральная выдержка/достижение/порча/сохранение |
| Чудь/Яга/Горыныч/городище | [разведка27.10](snapshot/docs/implementation/scouting269-20261027/REPORT.md) | Свежий мир, расстояние248, естественные starts, ограничения квестов |

[Остаток16направлений](snapshot/docs/implementation/stabilization269-kitchen-20261026/REMAINING.md):2scoped gates пройдены,14имеют непроверенные сценарии/owner review. Это **не14тестов** и не процент готовности.

## Важная граница Git и установленной сборки

Этот commit публикует **только пакет отчётов**. Он НЕ делает живые незакоммиченные исправления исходников частью опубликованного gameplay-кода ветки.

Установленная2.0.7 собрана из base `61cb344eeaf3a31db098052800a5e8ab710568de` **плюс dirty production diff**. Его точная копия — [CURRENT_PRODUCTION.patch](CURRENT_PRODUCTION.patch), SHA256 `a2a91e45aeb6914d7ae331e3313e825c8177b26c55dd4c08aecc570b16d08238`. Патч приложен для чтения/сопоставления, не является инструкцией применять его вслепую. Уже присутствующие локальные изменения не перезаписывать и повторно не накладывать.

JAR2.0.7: SHA256 `6995a9fbc54f5e70f84227341cf020d309b27ae48f43b7c6fcfa7dd6c76dc221`. [Установка/source/backup](snapshot/docs/implementation/stabilization269-brewing-20261026/installation-receipt-207.json), [финальная сверка1950классов/сборки](snapshot/docs/implementation/scouting269-20261027/final-gates.json). JAR/миры/Gradle caches в этот пакет не включены; installed path и абсолютные пути evidence относятся к компьютеру владельца, а не к файлам GitHub.

[snapshot/tools](snapshot/tools) — снимок QA исходников и связанных изменённых tools для анализа. Это архив, не новый production sourceSet. [PACKAGE.json](PACKAGE.json) фиксирует источник/границы, [MANIFEST_SHA256.json](MANIFEST_SHA256.json) — хеши файлов пакета. Копии отчётов сохранены без переписывания исторических результатов; старые даты, версии, FAIL/NOT_RUN и локальные пути намеренно оставлены. Текущие выводы читать в ANSWERS.md и позднейших отчётах.

Новые запуски Minecraft, изменения баланса, реализация квестов, закрытие Issues или слияние main этим пакетом не выполнялись.
