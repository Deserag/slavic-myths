# Superseded historical checkpoint

Current candidate is 1.9.7.66 with help gates for all four heroes, tested TCP multiplayer and verified installation. See [current matrix](../heroes-coop/REPORT.md) and [installed receipt](../polymc-19766/installed.json). Statements below describe the earlier unfinished Ilya-only checkpoint.

# Согласованная цепочка Ильи — IN_PROGRESS

Владелец выбрал: направление от NPC → встреча с Ильёй → помощь → постоянный вызов. Код находится в рабочей ветке codex/feedback-19765; автоматического коммита и установки нет.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Направление от Яги/Баюна | IlyaIntroduction, YagaMenu707, BayunNetwork10, существующие TaleData/книга | PASS (код); финальный runtime NOT_RUN |
| Встреча и помощь | Два разговора; сервер расходует 6 хлебов, бутылочку мёда и 4 железа, возвращает бутылочку | PASS (код); ручная приёмка NOT_RUN |
| Постоянный вызов | StoryPolicy, crafting gates, ilya_friendship; владение знаком не открывает вызов | PASS (код); ручная приёмка NOT_RUN |
| Сохранения | Registry IDs сохранены; прежние подтверждённые вызовы/победы Ильи учитываются | PASS (аудит); перезаход NOT_RUN |
| Защита встречи | Один сохранённый UUID NPC, повторный запрос возвращает подсказку; смерть освобождает резерв | PASS (код); сетевой MP NOT_RUN |
| Перунитовые инструменты | feedback19765_assets.py сохраняет силуэты iron tools и рукояти, меняет цвет металла | PASS (ресурсы); визуальная приёмка NOT_RUN |
| Голова Полоза | Змеиный профиль вместо кошачьих очертаний; before сохранён | PASS (ресурс); визуальная приёмка NOT_RUN |
| Метла и репутация | Исправлена иконка first_flight; прежний runtime-feedback65-2.log проверял торговлю и взлёт | Исторический PASS; текущий runtime NOT_RUN |
| Исследование Чуди | STRUCTURE_STARTS future вместо удалённой FULL загрузки; посещение по native references загруженного чанка | PASS (код); свежая выдача runtime NOT_RUN |
| Возврат к NPC | client/research-results.txt: Visited/Claimed и +1 ancient_sign для Яги и Баюна | Исторический PASS старых назначений |
| Чёткость диалога Баюна | Устранено повторное размытие поверх текста; TaleScreen уже исключает blur | PASS (аудит); визуальный runtime NOT_RUN |
| Финальная установка | Незавершённая цепочка не установлена | POLYMC_NOT_INSTALLED |
| Clean build Java 21 | work/resume-235-237-236/build-feedback65-current.log: BUILD SUCCESSFUL, 1m24s | PASS |
| Production JAR/resources | verify_current_resources.py: 7734 resources, 4193 JSON, 1385 PNG, 1789 models, 1864 Java21 classes | PASS |

Локальный JAR пока имеет прежнюю версию 1.9.7.64, это не установленный JAR профиля. SHA256 локального артефакта: b462d5d654ccbab6b352fe7961b49a2ae536f1e6d056f7735d724e6446cf0731. В PolyMC он не копировался.

Сохранены результаты прежних запусков: два GameTest сервера (10/10, 11/11) и два клиента. Первый клиент выявил незасчитываемое посещение и задержки синхронной генерации; второй подтвердил посещение/возврат обеим NPC по старым назначениям. Эти результаты предшествуют последним правкам обработки ошибок future и отрисовки Баюна и не подтверждают весь текущий diff. Размытые items-before/after не принимаются за итоговое доказательство art.

После последних инструкций AGENTS.md Minecraft/PolyMC/GameTest/сервер не запускались. Свежая асинхронная выдача с Bounds/StartPieces, реальные сетевые клиенты, окончательные сопоставимые screenshots и ручная приёмка остаются NOT_RUN. Issues не закрываются, готовность не заявляется. Ад и очистка Git не затронуты.

## Проверка остальных спутников

Личная цепочка знакомства/помощи добавлена только Илье. HeroCalls.toggle для Алёши и Финиста не требует личного поручения. Для Добрыни проверяются first_book_entry либо книга в инвентаре (или права оператора), но не помощь богатырю. Общие ограничения боя, cooldown и состава группы сохранены. Эти три цепочки в данном пакете не реализованы.

Владелец отдельно поручил установить текущий тестовый кандидат 1.9.7.65; установка не означает приёмку или завершение цепочки Issues.
