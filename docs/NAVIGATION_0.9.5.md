# Slavic Myths 0.9.5 — навигация и зоны поиска

Minecraft 1.21.1, NeoForge 21.1.255, Java 21. Задание:
`Slavic_Myths_0.9.5_Navigation_Search_Zones_V2_Codex_Package.zip`.
Существующие registry IDs, loot, AI, worldgen и баланс сохранены.

## Статусы

- **DONE, headless:** модель маркеров, фильтры, один tracked UUID, независимые данные игроков,
  сохранение и очистка, серверная авторизация, сетевые codecs, генератор зон и статистика.
- **DONE, server hooks:** клубок проверяет `placed`, активация существующего путевого камня,
  discovery крупных структур по уже загруженным chunks, существующие охоты/ритуалы/контракты.
- **COMPILES:** экран, клавиша, HUD и необязательный Xaero adapter; игровая проверка не выполнена.
- **BLOCKED:** технических блокировок сборки нет. Контракт без известного реального encounter
  остаётся без зоны до появления подходящего encounter; координаты не выдумываются.
- **NOT STARTED:** ручная клиентская QA. Круговой overlay Xaero не реализован: используется
  разрешённый fallback «приблизительный центр + радиус». Новые encounters не добавлены.

## Данные и безопасность

`navigation/NavigationState`, `SlavicMarker`, `MarkerCategory`, `SearchArea` — публичные значения.
`NavigationRecords` — SavedData Overworld `slavicmyths_navigation`, версия схемы 1.
Игроки индексированы UUID. Данные маркеров, категории, настройки, скрытые IDs, tracked UUID,
pending quest/revision и private assignments сохраняются. Неизвестная будущая версия SavedData
сохраняется без перезаписи её содержимого; повреждённые отдельные записи пропускаются.

Snapshot сериализует только `data.player(senderUUID).save()`.
Точный target UUID/anchor, seed/salt и чужие игроки существуют только в серверном SavedData.
До discovery публичная зона содержит центр X/Z, радиус, состояние; Y=0 означает отсутствующую
высоту зоны, не высоту логова. Request не принимает player UUID или координаты.
Права и ownership проверяются на сервере; лимит запросов — 12 за 20 ticks.
`forget` доступен только для permanent discoveries; quest marker можно скрыть, но нельзя удалить клиентом.

Восемь категорий: YAGA, WAYSTONE, KURGAN, BANDIT, BOSS, QUEST, SPECIAL_LOCATION, EVENT.
По умолчанию все включены. Семь настроек включены: always tracked, auto quest, auto thread,
fallback HUD, Xaero, hide completed, auto major discoveries. Скрытие не удаляет данные.
Одна цель отслеживается; ручное переключение не удаляет старую. Ручной выбор имеет приоритет
над отложенным auto-track контракта. При исчезновении цели tracking очищается с уведомлением,
в том числе после следующего входа. Для замены действующей quest-цели создаётся новая ревизия.
Архив последних 32 завершённых/истёкших меток доступен в списке при выключенном hide completed;
архив не экспортируется на карту и не может быть tracked.

## SearchArea

Детерминированный seed: private world seed/salt, quest-instance UUID и revision.
Один радиус, максимум 32 кандидата центра. Offset: 10% случаев 0.30–0.45R,
75% 0.50–0.85R, 15% 0.85–0.95R, случайный угол. Цель обязана быть внутри.
Публичный центр никогда не заменяется точным anchor при отказе всех кандидатов.
Проверяются 13 biome samples, dry/core-biome compatibility, world border,
noise heights: затопленный центр, край высоты мира и резкие перепады ближайшей поверхности.
Эти проверки не создают и не загружают chunks.

| Масштаб | Радиус | Текущие цели |
|---|---:|---|
| MINI | 180–300 | Овинник, Волколак |
| RARE | 240–380 | Огненный змей, Подвей |
| WORLD | 320–550 | Лихо одноглазое, Тугарин Змей |
| UNIQUE | 450–700 | Только диагностический scale; новых unique encounters нет |

1000 независимых детерминированных выборок всех четырёх scales без terrain rejection:
радиус min **180**, mean **391.032**, max **699**;
offset/R min **0.301403**, mean **0.671683**, max **0.949818**.
Внутри 0.25R: **0 (0%)**; 0.25–0.50R: **110 (11%)**;
0.50–0.85R: **757 (75.7%)**; 0.85–0.95R: **133 (13.3%)**;
снаружи: **0**. Это статистика генератора, не измерение распределения на реальной карте.

Зона привязана к immutable home/encounter anchor, не движется за боссом.
Вход: hysteresis R−4/R+12, уведомление не чаще раза за 200 ticks.
Discovery: реально загруженная цель в 48 блоках, цель атакует этого игрока либо игрок
нанёс ей урон. После discovery допустима временная точная метка; её положение обновляется
раз в секунду при перемещении минимум на 8 блоков. Постоянное логово одноразового босса не создаётся.
Смерть от другого игрока/исчезновение — expiration, не успешное завершение чужого контракта.
Unloading не считается смертью и не создаёт нового босса.

## Подключение существующего контента

Клубок: до `YagaData.placed=true` не готовит план избушки и не выдаёт её координаты.
После реального размещения записывает permanent YAGA marker со стабильным ID;
повторное применение не создаёт дубликаты. Существующие cooldown/звук сохраняются.

Path Stone: запись на существующем взаимодействии с блоком; прежнее RPG menu сохранено.
В этом моде камень сейчас не является системой телепортации. Новая travel-механика не добавлена.
Удалённый камень очищает только соответствующую owned метку; незагруженный chunk не запрашивается.

Автоматические major discoveries: Great Kurgan, large bandit camp, flooded shrine,
abandoned settlement. Малые структуры сохраняются только кнопкой «Сохранить место рядом».
Проверка: не чаще 40 ticks после движения на 8 блоков, окно 13×13 уже загруженных chunks,
не более 256 дополнительных loaded reference origins; проверка близости к bounding box.
`locate`/генерация/глобальный обход списка курганов не используются.

Охотничий рог и ритуальные предметы сохраняют прежний призыв; зона относится к фактически
созданному encounter. Близко призванная цель естественно быстро становится discovered.
Контракты Яги остаются заданиями на сдачу трофея. При принятии навигация выбирает подходящий
реальный индексированный encounter в текущем измерении, максимум 4096 блоков, чужие личные
encounters исключены. Без подходящего encounter контракт принят, зона пока отсутствует;
появление подходящего entity может связать pending quest. Отдельные fights/локации не создаются.

## UI, Xaero и команды

`/slavicmyths navigation` доступна обычному игроку. Клавиша «Навигация» первоначально не назначена.
Экран: список/фильтры, страницы, название/измерение/дистанция/состояние, track/hide/forget.
HUD без Xaero: компактные название, направление и расстояние. Для зоны — расстояние до границы,
внутри только сообщение о поиске. В другом измерении стрелка отсутствует.

Adapter `compat/xaero/XaeroNavigationBridge` client-only и проверяет Minimap **26.5.0**.
Используются public methods фактически установленного JAR, без reflection/mixins.
Dedicated server и core не требуют Xaero; чужие классы не входят в production JAR.
Для компиляции API нужно подготовить pinned JAR командой `python tools/fetch_xaero_api_095.py`.
Компаньоны и SHA закреплены в `packaging/test-pack-lock.json`.

Выбрать группу **Slavic Myths** в Xaero вручную. Adapter не меняет выбранную группу пользователя.
WorldMap отображает эти точки вместе с Minimap; WorldMap без Minimap использует встроенный HUD.
Fallback зоны: один приблизительный waypoint «название + радиус», без точной цели и множества
boundary waypoints. Внутри зона скрывается из guidance, HUD показывает «ищите в пределах зоны».
Точки помечены origin `slavicmyths:navigation/<UUID>` и `temporary=true`; они не записываются
в waypoint files Xaero. Источник persistence — серверные данные. Adapter меняет/удаляет только
собственные object references и никогда не присваивает пользовательскую точку по имени/координатам.
При выключении интеграции, смене мира и logout owned display copies очищаются.

Изучен подход [Xaero Waystones Compatibility](https://github.com/ArcaneAlloy/Xaeros-Waystones-Compatibility),
но код не копировался и addon не устанавливался; лицензия All Rights Reserved.

```text
/slavicmyths navigation
/slavicmyths dev navigation list
/slavicmyths dev navigation marker <UUID>
/slavicmyths dev navigation searcharea create-test mini
/slavicmyths dev navigation searcharea create-test rare
/slavicmyths dev navigation searcharea create-test world
/slavicmyths dev navigation searcharea create-test unique
/slavicmyths dev navigation searcharea stats
/slavicmyths dev navigation clear-owned
```

Все `dev` требуют OP 2. `create-test` создаёт только диагностическую зону, не босса;
её удаляет `clear-owned`. Эта команда очищает данные навигации данного игрока, не мир/чужие waypoints.

## Проверка

`verifyNavigation`: property checks генератора, codecs модели, фильтры, ownership, tracking,
secrecy, cleanup, dimensions и direction math. В loader добавлены три Navigation GameTests:
wire/model, два игрока/server permissions, lifecycle/клубок/камень/пersistence/future schema.
Вместе с четырьмя прежними port GameTests — семь тестов в каждом финальном server run.
Журналы, hash JAR и факт установки: `docs/verification/navigation-0.9.5/`.
Игровая QA: [MANUAL_QA_0.9.5_NAVIGATION.md](MANUAL_QA_0.9.5_NAVIGATION.md).
Ни клиент, ни PolyMC автоматически не запускаются.
