# Обновление PolyMC — 29.10.2026

По прямому поручению владельца обновлён тестовый кандидат, не объявлен принятым релизом.

- hell подтянут fast-forward до `537e3a7a231b8332aa4e1b3c61aa8ff416b2ebfe`; исходники не менялись.
- Java21 clean build PASS25s; resource/JAR gate PASS,8601 resources,2102 Java21 classes.
- Версия `2.0.10-HELL`; установлен `C:\Users\user\AppData\Roaming\PolyMC\instances\Slavic-Myths-2.0.8-HELL-Testing\.minecraft\mods\slavicmyths-2.0.10-HELL.jar`.
- SHA256 `685387c355ba9665e6921dbd1757761cfddfe75651ff6bd5b48ab50f826a77f9` совпадает с build JAR.
- Backup `C:\Users\user\AppData\Roaming\PolyMC\instances\Slavic-Myths-2.0.8-HELL-Testing\mod-backups\20261029-073605` содержит прежние JAR и instance.cfg; matching mod JAR ровно1.
- Отображаемое имя инстанса обновлено; имя каталога сохранено. Minecraft1.21.1/NeoForge21.1.255, миры и настройки Java сохранены.
- Игра/PolyMC/server/GameTest не запускались; gameplay/relog/MP NOT_RUN. Открытые художественные/игровые блокеры задач остаются.

| REQUIREMENT | EVIDENCE | STATUS |
|---|---|---|
| Актуальная hell | source_commit в INSTALLATION.json | PASS |
| Clean build/resources | clean-build.log, resources.json | PASS |
| Installed SHA/backup/no duplicates | INSTALLATION.json | PASS |
| Runtime/gameplay |0 запусков| NOT_RUN |

## Как проверить в Minecraft

1. Сделать отдельную копию старого мира; открыть вручную тестовый инстанс2.0.10-HELL. Проверить версию в списке модов и отсутствие ошибок загрузки.
2. В новом survival-мире пройти обычный портал в Ад, исследовать новые chunks, проверить мобов/биомы/добычу/рецепты. Постень: отдельно проверить естественную встречу и сохранение боя; OP summon — только дополнительная диагностика, не доказательство доступности без читов.
3. Негативно проверить повторное получение награды и рецепт без нужных ингредиентов; сохранить/перезайти. Затем проверить копию старого мира и MP на одинаковой сборке.
4. Полные шаги предыдущего пакета: ../p0-bosses-327/OWNER_SMOKE_CHECKLIST.md. Их результаты этим обновлением не подтверждены; новые изменения ветки требуют своих проверок.

## Риски/откат

Новые registry IDs могут сохраниться в мире. Для отката закрыть игру, убрать2.0.10 JAR из mods, вернуть прежний JAR и instance.cfg из backup; мир восстанавливать только из согласованной полной копии.
Следующая задача — ручная игровая проверка; другие milestone автоматически не запускаются.
