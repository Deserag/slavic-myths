# Боссы 2.1.x — тестовая сборка 30.10.2026

Реализован игровой код #353/#354, подготовлен отдельный профиль для приёмки. Issues остаются открыты; оригинальный звук и художественная приёмка не завершены. 2.2 не активирована. Рабочая ветка codex/hell-bosses-completion-20261030 синхронизирована с hell9850b84c; main и исходный master не изменены. PR359 не слит.

| Критерий | #353 | #354 |
|---|---|---|
| Entity/HP/3 фазы/AI/атаки/эффекты/прислужники | PASS технических проверок | PASS технических проверок |
| Ритуалы, shaped рецепты, survival компоненты | PASS | PASS |
| Curios/руны/общая legendary_soul/exact-once награды | PASS | PASS |
| Native модели/текстуры/кости/анимации/emissive в клиенте | PASS запуска/рендера; приёмка PENDING | PASS запуска/рендера; приёмка PENDING |
| Оригинальные voice/whisper SFX | BLOCKED_AUDIO | BLOCKED_AUDIO |
| Совпадение с недоступным оригинальным PNG | BLOCKED оригинал; SVG просмотрены | BLOCKED оригинал; SVG просмотрены |
| Обычный survival баланс и финальная приёмка | PENDING владельца | PENDING владельца |

Java21 clean build/resources PASS.16/16 NeoForge GameTest: все12 attack IDs, точный контактный урон, зоны без stacking, пересечения волн, caps/cleanup, обе руны через настоящую Rune Anvil install/remove, Curios, cooldown, ритуалы и конкурирующий второй игрок.80 000 симуляций наград;500 native loot evaluations. Все8 компонентов получены из естественно сгенерированных сундуков5структур на seed11/29/101/2026/22022026:105/105. Разбитый мост без лута, не менялся; старый полный regression seed29 выявил отдельный дефект его настила (FAIL), компонентный suite PASS.

Dedicated server: два настоящих JVM write/restart, сущности боссов и прислужников загружены из region files; HP/owner/silence/TTL1200 проверены. Crash QA: два Runtime.halt61/62 и три JVM с native PlayerDataStorage, закреплённые награды без reroll/дубликатов/смены получателя. Реальные перезапуски не подменяются NBT roundtrip.

CI41016896761 SUCCESS: slim/wide, два настоящих клиента по loopback TCP,30 диагностических боёв (15 каждого босса), каждый с тремя наблюдаемыми клиентом фазами; peer отслеживал30разных UUID. Тестовые игроки200HP/35baseattack/full netherite/periodic healing. Это НЕ30обычных survival боёв и НЕ доказательство баланса. Короткие видеозаписи первого боя каждого босса получены из framebuffer. Windows desktop не использован. Sound cues —22семантических vanilla foley events с subtitles; оригинальные согласованные записи не заменены случайными ассетами и не объявлены PASS.

Изменённые системы: NetherSummonBoss/NetherBossCombat/MorovaPlagueEffect/NetherRetainer, NetherRituals/NetherBossLedger, NetherBossRewards + существующие BossRewardAuthority/Inbox, NetherTrophies/NetherBossRunes/NetherBossItems/NetherBossSounds; native renderers/model/glow/key/network/Curios tags;17предметных моделей/текстур; recipes/chest loot/lang. Генераторы воспроизводят ресурсы. Исправлен вскрытый тестами deadlock HallsWorldgen при inline ChunkEvent.Load: публикация через ограниченную очередь после тика.

Основные коммиты:3e1e2be8,583cf39a,913c5e0c,45769d54,1d2a8e9c,72a143d5,e81aea1c; синхронизация с актуальным hell5bc359d7. Runtime source для установленного CI JAR:e81aea1c30398e284370e7089577a08847e6ed35.

Профиль PolyMC: `C:\Users\user\AppData\Roaming\PolyMC\instances\Slavic-Myths-Bosses-2.1.6.1-Testing`.
Версия:2.1.6.1-BOSSES-TEST. JAR SHA256:`f17d37abae3be4eca66eee419f9b77c3d08e1d01f30942d7908e02bbe3a853be`.
Предыдущий профиль/миры не изменены, предыдущий JAR дополнительно скопирован в mod-backups/previous-working нового профиля. В новом профиле один slavicmyths JAR и Curios9.5.1. Minecraft новой сборки реально запускался в изолированном CI. Локальный запуск именно через PolyMC NOT_RUN (0), чтобы соблюдать изоляцию рабочего стола.

Операционная оговорка: неизвестная прерванная транзакция SPAWNING ритуала остаётся fail-closed reservation; автоматических повторов/возвратов, способных дублировать призыв, нет. Строгая устойчивость к физическому отказу диска не заявляется. Финальные art/audio, обычный баланс и локальная владельческая приёмка остаются открытыми критериями.

Проверка владельца: открыть отдельный TEST профиль, создать новый тестовый мир; получить компоненты в пяти подходящих структурах; изготовить призыватель по JEI/recipe book и провести Nether ritual; проверить все фазы, экипировку necklace/belt и назначаемые Controls keys, обе руны на штатной наковальне. Прежние миры для этой проверки не использовать.
