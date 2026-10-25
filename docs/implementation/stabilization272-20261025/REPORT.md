# #272 — контракт текущего подпакета (IN_PROGRESS)

Baseline: hell 2f82f20e, clean tree; origin/hell fast-forward получен без изменения main.

| REQUIREMENT | REAL_SOURCE | PLAYER_PATH | TEST | EXPECTED |
|---|---|---|---|---|
| Доступ после survival-победы | WorldBoss.die → AdvancementMilestones → TaleEvents.earned → YagaDiscovery | Найти обычного Лиха/Тугарина, победить, открыть дорогу в книге | Offline policy + source wiring; будущий survival runtime | Сервер сразу сообщает книге доступ |
| Creative без побед | YagaDiscovery + PlayerChangeGameModeEvent/login | Войти в creative; доступ — отдельное правило, без боевых наград | Полная таблица access-policy; отменённый event и итоговый режим проверяет отложенный callback | Дорога stage1, создание по явной кнопке |
| Сохранение доступа | существующий overworld TaleData/Entries/yaga_search | Вернуться в survival, умереть/перезайти | Offline policy; реальный restart NOT_RUN | stage1/2 остаётся допуском, новые NBT stores не нужны |
| Без дубликатов/ложного дома | существующие YagaDiscovery Build + YagaData.placed/npc | Нажать создание дважды | Source review; runtime NOT_RUN | Одна очередь; stage2 только placed+anchor+npc |

Доказанная недоработка: yaga_search не связан с advancement в каталоге; TaleEvents.earned не вызывает YagaDiscovery.reconcile. Поэтому push обновления дороги после победы отсутствует до повторной reconcile при открытии книги/login. Это не доказывает полную причину наблюдённого владельцем отказа. Creative-path отсутствует. Полный #272 (UI, trust, NPC, реальная победа/сохранение/MP) остаётся IN_PROGRESS.

## Реализованный подпакет

- `YagaAccessRules`: creative ИЛИ сохранённый stage>=1 ИЛИ одна из трёх прежних побед. Это НЕ grant боевых advancements и НЕ повышение доверия.
- `YagaDiscovery.mode`: cancellable event обрабатывается через следующий server TickTask; проверяются фактический creative-режим и та же онлайн-сущность игрока. Отменённый переход не должен открывать путь. Login использует прежний TaleService.reconcile, уже творческий персонаж тоже получает доступ.
- `TaleEvents.earned`: для name_your_misfortune/steppe_champion/defeat_nightingale вызывается refresh — Entries и публичные Hints отправляются сразу. Клиентский TaleScreen.receive уже перестраивает открытый экран при delta.
- Существующий TaleData overworld хранит открытый stage; новый NBT store/registry ID/миграция не добавлялись. Открытие книги/creative не запускает строительство. Создание остаётся явной кнопкой, сервер проверяет eligibility; общий BUILDS сохраняет одну очередь. Stage2 по-прежнему требует placed+anchor+npc.
- WorldBoss.die, AdvancementMilestones, reward/trust/loot, YagaData и YagaServices сохранены. Ledger-gated зачёт существующего боя не обойдён. Если отказ владельца вызван отсутствием принятого credit, а не обновлением книги, этот остаток ещё требует реального воспроизведения.

## Офлайн-проверки

`tools/yaga-access-offline/YagaAccessCheck.java` компилируется отдельно вместе с actual production YagaAccessRules, проверяет80 комбинаций доступа, сохранённый stage после creative, отсутствие доступа без причин и границы трёх victory-trigger IDs. Никакого Minecraft bootstrap/world/server. `tools/verify_stabilization_access_spawn.py` проверяет event wiring и сохранность смежных исходников/ресурсов относительно2f82f20e (с нормализацией Windows CRLF). Это source gate, не тест доставки сетевого пакета.

## Что тебе проверить в Minecraft

Только позже, после отдельного разрешения установки: точный JAR/SHA ниже. Сначала отдельный новый survival и копия старого мира; существующие сохранения не перезаписывать.

1. Survival без OP: найти естественного обычного Лиха, победить штатным оружием. Ожидается реальное достижение и доступная «Дорога к Бабе-яге» сразу, без перезахода. В книге должна появиться кнопка создания, если дома нет. Дополнительно проверить Тугарина и Соловья как независимые альтернативы.
2. Отдельный тестовый персонаж: creative → survival → естественное Лихо → победа. Результат тот же. Отдельно проверить ранее убитого в creative Лиха, а затем новое законное убийство в survival. Креативная смерть не обязана давать боевое достижение.
3. Отдельный персонаж без побед: открыть книгу, перейти в creative (смена режима здесь неизбежно требует разрешённого игрового управления/OP только для подготовки). Через следующий серверный тик дорога открыта, боевые достижения не выдаются. Открытая книга обновляется. При возврате в survival доступ остаётся.
4. Нажать создание дважды, затем повторить вход в creative/survival. Ожидается одна очередь, один дом/NPC; резервация без finished placement не должна выглядеть найденным домом. Дойти до реального дома и поговорить с Ягой.
5. Сохранить, выйти, зайти; затем умереть/возродиться и сменить измерение. Доступ не пропадает. На копии старого мира проверить персонажа с прежней победой. Обычный новый survival-персонаж без побед и без creative остаётся закрыт; spectator не открывает доступ.
6. При существующей избушке победить другим персонажем: карта должна обновиться вместе с дорогой. Два игрока проверяют независимый прогресс и общий единственный дом. При наличии плагина отмены game-mode event проверить отменённый creative-переход: доступа он не выдаёт.
7. С прежней победой поговорить с Ягой несколько раз: уже существующее recognizeVictory должно выдать прежнее доверие/награду один раз, снять main:0 и не повторять выдачу после relog. Creative без побед не повышает доверие. GUI scale2/3/4: текст и кнопки не перекрываются; текущий подпакет UI не меняет.

**НЕ ПРОВЕРЕНО:** фактическая серверная смерть/credit в сообщённом владельцем случае, native game-mode event/сеть/открытый GUI, настоящее сохранение/перезаход, строительство/NPC,trust/reward,MP. Запусков Minecraft/PolyMC/server/GameTest:0. Ошибка владельца не объявляется устранённой целиком. Возможные регрессии: слишком раннее открытие записи по legacy stage и очередность событий; для них нужны сценарии выше.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Creative-допуск без combat grants |80 native plain-Java policy cases, source wiring | PASS (OFFLINE_ONLY) |
| Немедленное обновление после трёх побед |TaleEvents.earned → YagaDiscovery.refresh → Entries/Hints | PASS (source only) |
| Сохранение допуска |Существующий TaleData stage + policy cases | PASS (policy only) |
| Реальное сохранение/сеть/game-mode/GUI |Сценарии выше, запусков0 | NOT_RUN |
| Survival kill владельца полностью воспроизведён/устранён |Найден missing refresh, исходный runtime не воспроизводился | NOT_RUN |
| Реальный дом/NPC,trust,rewards/UI/MP |Прежний код сохранён, требует игровых сценариев | NOT_RUN |
| Owner acceptance |Не запрашивалась вместо игры | NOT_RUN |

Статус всей #272: IN_PROGRESS, Issue OPEN. GitHub Project Status не менялся. Следующий scope после этого ограниченного подпакета: #271 gate-аудит/согласование вероятностей; затем #275. Полная программа #269 НЕ завершена.

## Финальная техническая квитанция

- Source baseline: `2f82f20e` + production-файлы из `source-manifest.json`; итоговый commit текущего пакета содержит эти байты.
- Java21 `clean build --no-daemon --max-workers=1`, heap1536MiB/ActiveProcessorCount2: **PASS,2м25с** (`build.log`). Dry-run не содержит runClient/runServer/GameTest. Gradle test: NO-SOURCE;80проверок policy выполнены отдельно (`offline.log`). Предупреждения deprecated/unchecked остаются.
- Ресурсы/JAR: **PASS**,8001упакованный ресурс,4402JSON,1443PNG,1886моделей,1929Java21classes. Production policy class присутствует, offline fixture отсутствует.
- BUILT: `D:\slavic-myths\build\libs\slavicmyths-2.0.1-HELL.jar`, version `2.0.1-HELL`; SHA256 `941b7c3d15cd811651d1895b6dada72161ecba93ee072bfc3c18187686a8456d`.
- `POLYMC_NOT_INSTALLED`: этот JAR не устанавливался; пользовательский экземпляр/настройки/миры не менялись. Запусков Minecraft/PolyMC/GameTest/server:0, GAMEPLAY_NOT_RUN.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Java21 clean build | build.log,2м25с | PASS |
| Resources/production JAR | resources.log/jar-receipt.json | PASS |
| Native access-policy/source preservation | offline.log | PASS (OFFLINE_ONLY) |
| Runtime/owner acceptance |0запусков, checklist выше | NOT_RUN |
