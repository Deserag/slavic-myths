# #6 — предсказуемый морозный поток

База отдельного PR: `origin/main` b7616080. Баланс урона не менялся.

Причины симптома разделены: штатное окончание через 60 тиков и ранний stop
после heartbeat gap >7. Нативный baseline из диагностики #3 подтвердил,
что gap8 прекращал использование и назначал полный CD200.

Grace теперь 20 тиков; gap21 завершает поток с объяснением. Это ограниченный
допуск, а не обещание переживать произвольную потерю сети. На сервере записывается
hard deadline start+60; повторный start не продлевает интервал. Release-пакет
останавливает только активный поток, не использование другого предмета.
Сервер отправляет remaining/ready в существующем View, HUD показывает остаток,
готовность и перезарядку. Причины release/menu/connection/finished локализованы.
Исправлено использование неподдерживаемого Minecraft-переводами `%.1f`:
число форматируется отдельно и передаётся в `%s`.

14 октября 2026, GameTest server 19:22 MSK: 4/4 PASS.

- gap8/gap20 сохраняют поток; gap21 завершает use/state и ставит CD200.
- Повторный start сохраняет deadline; heartbeat не позволяет пересечь deadline.
- Release и menu завершают поток.
- Настоящие 60 серверных тиков: heartbeat каждые3, поток активен по59,
  прекращается на60. Fixture вызывает production onUseTick на реальном сервере.

Это native server lifecycle с injected heartbeat gaps; пакеты fixture
отправляются в заглушку connection. Это **не** проверка lag/drop на реальном
сетевом клиенте. Dev client, HUD/звук глазами и подключённый dedicated
пока PENDING. Issue не закрывается до сетевой/клиентской проверки.

Реальные запуски задачи #6: GameTest server1, client0. Общий счёт #3–#10:
GameTest server3, client0, standalone dedicated0. Между стартами >10мин.

Проверки: `gradlew.bat clean build`; затем
`gradlew.bat -I tools/reward-issues.init.gradle runRewardChecks`.
Test fixtures находятся вне main и исключаются из production JAR.
