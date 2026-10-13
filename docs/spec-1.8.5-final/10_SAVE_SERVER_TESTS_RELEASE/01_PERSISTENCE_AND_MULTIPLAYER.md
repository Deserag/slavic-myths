# Сохранения, сервер, кооператив, защита от дублирования

## Устойчивые поля на игрока (реализовать через текущую сохранённую систему проекта)
- `knownYagaHut` (dimension, position, orientation/POI link, last validated)
- `metYaga`, `metBayun`, `metWolf`/existing state, `trustYaga/Bayun/Wolf` read-only from existing system
- `lastYagaDialogAt`, `lastBayunDialogAt`
- `firstMorEncounterAt`, `firstMorKillAt`, `morKillsByType`
- `invitationShown`, `teleportChoice`, `introStarted`, `introCompleted`, `dialoguePageIndex`
- `quest185.stages[id]` with `status`, `requirements`, `rewardClaimed`, `pendingReward`
- `wolfSideQuest.w1/w2/w3`, `wolfCallUnlocked`, `wolfSummonCooldownEndTick`
- `trackerCooldownEndTick`, `trackerActiveUntilTick`, `morWardActiveUntilTick`
- `polozDefeatEvidence` only as data link to REAL 1.8.3.2 event ledger, no assumed fake defeat flag
- `cleansingRecipeRevealed` и `cleansingKnowledgeUnlocked` — после исследования Лиха (не после Полоза); рецепт физически доступен в верстаке и до разговора
- `likhoResearchRewardClaimed`, `earthRestorationRewardClaimed`, `restoreEarthCleanedCount` (0..20), `earthTutorialInstanceId` — серверные per-player one-time fields
- `purificationTaskLedger` (dimension + positions + owner UUID + persisted progress/consumed receipt), `cleansedBlockImmunity` (expiry на 1800 тиков, очистка при блок-изменении); масштабное хранение по чанкам или имеющемуся SavedData, не в player NBT на все блоки мира

## Scope
- Хранение игроком по UUID и сохранению сервера; не привязывать к nickname.
- В исходной системе доверие может быть связана с NPC и World — использовать её существующий API, а не второе trust state.
- Данные сохраняются при death, disconnect, restart, update 1.8.3.2→1.8.5.
- КД воспроизводимые после перезахода, делаются на server tick clock / persistent remaining-time, не на клиентском wall-clock, не на FPS.

## Дедупликация
- Любая сдача квеста — атомарная серверная операция `check→consume (если нужно)→set completed→grant reward/queue`. Два одновременных пакета не должны дважды выдать опыт/предметы.
- Для Q2 предметы тратятся только после успешной транзакции.
- Для Q3 Око/Сердце **не расходуются**. Для Q6 предметы Полоза **не расходуются**.
- При полном инвентаре `pendingReward` persist, кнопку «Забрать» повторно сделать безопасной.

## Минимальная мультиплеерная поддержка (расширенная кооперативная логика потом)
- Каждый игрок индивидуально получает приглашение, trust-gated участников и свои награды.
- Сцена у общей избы не должен перемещать/подменять реальные NPC, не должно возникнуть бесконечное количество физических NPC при 3 игроках одновременно. Временные актёры scoped by session/player.
- Чат-приглашение и quest popups видны только адресату.
- `wolf_call` призывает 1 support Wolf per owner. Собака другого игрока не даёт повторных наград чужому владельцу.
- При убийстве Полоза засчитывать только реальных участников, подтверждённых существующим event/credit API. Если нет такого API, минимальный честный критерий: игрок находился не дальше 128 блоков от Полоза во время боя и нанес хотя бы 1 валидный удар либо получил от него урон; к моменту смерти находится online в том же измерении. Это не меняет выдачу основного лута 1.8.3.2.
- Если серверная битва уже завершена, не заставлять каждого участника повторять boss spawn ради квеста.
- Новый патч не решает произвольный масштабируемый raid party matchmaking (отложено).

## Особый случай Wolf live NPC
Не удалять настоящего Серого Волка при истечении призывного NPC; support entity отличать owner/sourceId и noDrop=true. При конфликте с текущим Wolf summon у игрока — повторно использовать существующий summon item/state, не создавать одновременно две разные системы.
