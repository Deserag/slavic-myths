# Ручная QA 0.9.5 — ещё НЕ выполнена

Запускать вручную новый тестовый мир Minecraft 1.21.1 / NeoForge 21.1.255 / Java 21.
Не переносить мир 1.16.5 в этот профиль. Автоматически клиент не запускался.
Для сценария без Xaero временно отключить оба Xaero в копии тестового профиля.
Использовать cheats для dev-команд; multiplayer — два настоящих игрока.
NBT/codec roundtrip и FakePlayer tests не заменяют реальный relog/UI/сетевой multiplayer.

1. [ ] Без Xaero: `/slavicmyths navigation`, назначить клавишу; список, фильтры и HUD работают.
2. [ ] С Minimap 26.5.0/WorldMap 1.45.0: выбрать группу Slavic Myths; видны owned markers.
3. [ ] Скрыть/включить каждую из восьми категорий; записи не удаляются.
4. [ ] Выбрать постоянную цель: одна стрелка, корректное расстояние/название.
5. [ ] Выбрать вторую цель: первая сохранена, отслеживается только вторая.
6. [ ] Скрытая категория tracked: visible при always tracked ON; OFF действительно скрывает.
7. [ ] Клубок до `placed`: нет метки/направления/координат planned hut.
8. [ ] Клубок после размещения: permanent YAGA marker; auto thread ON/OFF соблюдается.
9. [ ] Повторить клубок, relog и применение: одна метка избушки.
10. [ ] Активировать один Path Stone дважды: один travel point, прежнее RPG menu работает.
11. [ ] Удалить камень, включая explosion; при loaded chunk его метка очищается, чужие нет.
12. [ ] Дойти до Great Kurgan, включая край большого bounding box: один permanent marker.
13. [ ] Пройти малые POI: нет автоспама; «Сохранить место рядом» сохраняет найденное место.
14. [ ] Настоящий контракт с известным подходящим encounter: приблизительная зона; без encounter
    контракт принят, сообщается отсутствие известной цели, не появляется выдуманная точка.
15. [ ] Найти пример цели далеко от центра зоны; поход в центр не вызывает discovery сам по себе.
16. [ ] Найти near-edge пример; цель внутри внешней границы зоны.
17. [ ] Проверить snapshot до discovery: только публичная геометрия, без target UUID/anchor/seed/salt.
    Запись точного anchor в server SavedData ожидаема; клиенту этот SavedData не передаётся.
18. [ ] Пересечь границу несколько раз: входное сообщение без спама; внутри qualitative search.
19. [ ] Подойти к реально loaded цели на 48 блоков / получить агро / нанести урон:
    временная точная метка после discovery; движущийся найденный босс обновляется.
20. [ ] Завершить quest/убить свою цель: temporary marker/area удалены, tracking снят, permanent сохранены.
21. [ ] Failure/despawn/dev clear/убийство другим игроком: expiration, нет orphan area; replacement
    действующего контракта получает новую revision и сохраняет прежний tracking intent.
22. [ ] Relog при active quest: UUID/геометрия/revision/hidden settings сохранены, новый encounter не создан.
23. [ ] Dedicated server без Xaero: нет загрузки клиентских классов. Headless boots проверены;
    реальное подключение клиентов всё ещё проверить.
24. [ ] Два игрока с разными quests: только свои зоны/tracked target; нет чужого snapshot.
25. [ ] Создать пользовательский Xaero waypoint с тем же именем/местом: hide/clear/logout
    Slavic navigation его не изменяют; чужие группы остаются выбранными как прежде.
26. [ ] Фильтры и все семь настроек сохраняются после restart server/relog.
27. [ ] Valid tracked permanent/active target сохраняется после death/dimension switch/relog;
    в другом измерении нет стрелки. Завершённая цель не восстанавливается.
28. [ ] Reload world при completed/expired quest: нет orphan owned display points;
    отключение/включение интеграции пересоздаёт только актуальные точки без дубликатов.

Дополнительно: низкое разрешение/GUI scale, страницы списка и настроек, локализация RU/EN,
auto quest OFF, ручное переключение пока pending quest ждёт encounter, выключенный HUD,
только WorldMap без Minimap, другая версия Minimap (должен работать встроенный fallback).
Диагностика: `/slavicmyths dev navigation searcharea create-test mini|rare|world|unique`,
`searcharea stats`, `list`, `marker <UUID>`, `clear-owned`. Dev зоны не создают encounters.

Результаты отмечать только после фактической проверки; снимки экрана и свежий latest.log
сохранять отдельно. Код не измеряет MSPT; ограниченные scans не являются заявлением о benchmark.
