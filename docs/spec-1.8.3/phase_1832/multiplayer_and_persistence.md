# Мировой ивент: клиент/сервер, несколько игроков и сохранение

## Единый authoritative сервер
- Единственная активная запись **на Overworld server level** в `SavedData` / текущем эквиваленте проекта: `eventUUID`, `bossUUID`, `ritualBlockPos`, `spawnBlockPos`, `state {PREPARING, ACTIVE, PAUSED, DEFEATED, FAILED}`, `elapsedActiveTicks`, `cooldownUntilGameTime`, `spawnCommitToken`, `participants` (UUID set), прогресс рога не хранить здесь, потому что item stack переносим.
- Boss сохраняет HP 3000/max, фазу, cooldowns, segment path-history либо минимально достаточную реконструкцию, weakspot/collision snapshot, minion UUID, target UUID/last aggressor. После save/load ни двойного spawn, ни HP reset, ни исчезнувших 40-блочных частей.
- При `reload`: идемпотентная проверка active boss uuid -> существующая entity; если entity выгружена, не спавнить дубликат, дождаться корректной загрузки/возобновить тикеты. При подтверждённой смерти cleanup active state и запись defeat.
- Важно: horn `hasSummonedOnce` — persistent data component на самом ItemStack; перемещение между ящиками/игроками не сбрасывает стоимость повторного вызова.

## Сеть и отображение
- Сервер вычисляет атаки, слабые точки, damage, направление, заражение, сохранение и трофеи. Клиент только отображает geom/particles/аудио/локализованный HUD.
- Bossbar только у игроков в радиусе 256 блоков от **ближайшего** body segment или специальное сообщение всем в Overworld при призыве; packet phase transition idempotent; при join in progress показывать текущую фазу/направление, не перезапускать 9-секундный звук.
- 40-блочное сегментированное тело: не создавать 40 отдельных самостоятельных mob AI/loot entities. Root entity и server-owned части / клиентская multipart-геометрия, либо доказуемо совместимая реализация с одним damage owner.
- Для 2+ игроков отдельные состояния предупреждений, выбора угла, роговой анимации; никакие локальные клиентские решения не выполняют `destroyBlock`.

## Защита от дубликатов/эксплойтов
- Одновременная активация разных рогов 2 игроками: server compare-and-set состояние event, победивший commit расходует прах, второму показывать active event без расхода. Повторный right click во время 9-sec cinematic не создаёт новых Полозов.
- Босс способен разрушать дома/деревни, и это ожидаемо. `mobGriefing` check перед ритуалом, предупреждение, сохранение; **никакой скрытой отмены разрушений из-за мультиплеера**.
- On server shutdown flush pending block queue safely: применённые изменения остаются, не применённые могут быть отброшены и пересчитаны при движении. Не оставлять corrupt tile operations после death.
- На death/drop грань правильного UUID: один root death -> one loot transaction, no multiplied scale per segment, no duplication по reconnect.

## Отложенный полноценный multiplayer balance
Пользователь отдельно обсудит баланс для нескольких игроков позже. **СЕЙЧАС** не скейлить HP в зависимости от игроков и не вводить голосование/права на призыв: 3000 HP фиксированы для любого числа. Но корректная серверная синхронизация и no-dupe уже обязательны.
