# Мультиплеер, сохранения, масштабирование и анти-эксплойты

- Единая серверная истина: boss phases/hp, вороны UUID и владельцы, damage, fear/waning шкалы, key-bound способности, cooldown, награды, амулеты и их птицы. Client хранит только визуальные фантомы конкретного игрока, эффекты/camera и interpolation.
- Пакеты должны проверять игрока отправителя, inventory slot, cooldown, жив/в той же dimension, допустимая дистанция, что предмет реально экипирован. Не принимать клиентскую «цель на расстоянии 100 блоков» и «я нанёс 5000 урона» без серверной проверки.
- На разных игроках независимые галлюцинации. Вороны boss-level общие, союзные — собственность конкретного ownerUUID. Все птицы cleanup после `yield`, owner logout/death, transfer, unload/dimension change.
- В истории испытаний per player UUID persistent `karachunTrialWins`, `karachunBlackWingClaimed`, `karachunTrialCompleted`, `lastRewardedSessionId`/ledger и предметные КД. Все data migration осторожно и в рамках текущих capability NBT conventions. Нельзя обнулять Морозко и текущие аксессуары.
- Дублирующий event tick, пакет, teleport или twice-death не запускает двойные награды/перепризыв. Scythe vampirism no farming собственного ворона, no hits against arbitrary fake phantom clients.
- Отмена испытания (уход, wipe, server stop) чистит только Karachun-specific sources; не удалять vanilla vanilla Hunger от еды/зомби, если он действительно пришёл от другого источника. Угасание чистится по owner/session provenance, если другое существо его применило — не трогать чужую длительность.
- Переход при 5% безопасен при высоком Burst DPS, попаданиях богатырей и several DOT at same tick. `YIELD` idempotent, boss не drop ordinary XP/items, health >=0.
- Не использовать global tick callbacks с полным world/entity scan. Эффективно обновлять только tracked trial participants и own summoned ravens. Проверить capacity на dedicated server.
