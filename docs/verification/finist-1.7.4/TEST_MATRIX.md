# Приёмка1.7.4 — фактические результаты

Полная игровая приёмка не завершена. Один последовательный dedicated GameTest проверяет отдельные серверные сценарии; это не51 игровых кейсов и не реальный multiplayer/relog. Offline ModelPart previews — не скриншоты Minecraft. Клиент не запускался согласно AGENTS.md.

Доказательства: runtime-final-04.log (итоговые assertions), offline-final.log, clean-build-final.log, resource-gates.json, polymc-installation.json. Статус PASS(server) ограничен явно указанной серверной частью; визуальные/сетевые наблюдения не подразумеваются.

| ID | Статус | Доказательство / предел |
|---|---|---|
| G01 | PARTIAL | Server feather summons170HP; client visibility/audio NOT_RUN |
| G02 | PASS (server) | 20 повторных вызовов, один UUID, item count1 |
| G03 | PARTIAL | Shared canonical slots/max2 checked; two actual previous hero entities not staged |
| G04 | NOT_RUN | NBT/ledger roundtrip проверен; реальный relog не выполнялся |
| G05 | NOT_RUN | Dimension overlap isolation проверена; реальный портал не выполнялся |
| G06 | NOT_RUN | Native pose geometry checked offline, not client locomotion |
| G07 | PARTIAL | Last Stand1HP, retreat flag, held slot, replacement rejection; visual route NOT_RUN |
| G08 | PASS (server) | Реальный урон, ограниченное окно dodge, второй lethal убивает |
| F01 | NOT_RUN | Companion model/renderer implemented; real perch render untested |
| F02 | NOT_RUN | Bounded scout controller implemented; actual3 markers/LOS scenario not separately tested |
| F03 | NOT_RUN | Dive controller implemented; real contact scenario not separately tested |
| F04 | PASS (server) | Один interruptible melee cancelled, следующий проходит, cooldown240 |
| F05 | PARTIAL | Initial dodge/АОЕ distinction checked; visual morph/shape movement NOT_RUN |
| F06 | NOT_RUN | Clear arc/return safety implemented; low ceiling morph not played |
| F07 | PARTIAL | Physical12HP anchor and disabled companion state tested; chain/HUD NOT_RUN |
| F08 | PASS (server) | Destroyed anchor releases immediately, stagger50t, immunity180t |
| F09 | NOT_RUN | 240t release implemented; full timeout fixture not run |
| F10 | PARTIAL | Victory cleanup tested; logout with active curse NOT_RUN |
| K01 | PARTIAL | Real spawn egg registry points to650HP Kartaus; useOn/render NOT_RUN |
| K02 | PARTIAL | Actual item server use frame, delayed single boss, no consumption; client ritual NOT_RUN |
| K03 | PARTIAL | Clear17 samples and lava refusal tested; dense interior test NOT_RUN |
| K04 | PARTIAL | Other hero canonical slot rejects start; all3 old hero scenes not run |
| K05 | PASS (server) | Finist-only start and all summon/recall replacements locked |
| K06 | PARTIAL | 45HP banner, P1 cap1, mitigation10+15 percent; visibility NOT_RUN |
| K07 | PARTIAL | One banner destroyed, cast stagger/recast160; two-banner case NOT_RUN |
| K08 | NOT_RUN | Safe alternative prop placement implemented, blocked-banner scenario not run |
| K09 | PARTIAL | Repeated spawns capped3, own target/UUID and cleanup tested; XP/drop kill fixture NOT_RUN |
| K10 | PARTIAL | Actual traveling wave6.5HP once tested; client stripe/dodge visibility NOT_RUN |
| K11 | PARTIAL | Actual diamond initial chains2HP; full DOT/expiry sequence NOT_RUN |
| K12 | PASS (server) | Native AI eye locks at21 and misses moved player at42 |
| K13 | PARTIAL | Phase2 curse/eye work; automated health crossing not separately tested |
| K14 | NOT_RUN | One rage/cooldown scaling implemented; full phase3 fight not played |
| K15 | PASS (server) | Normal full diamond: melee6, wave6.5, eye7; Resistance eye5.6 |
| K16 | NOT_RUN | Sustained fast-hit combat not played |
| K17 | PASS (server) | Actual story death cleans helpers/flags, no new loot rewards |
| K18 | NOT_RUN | 48-block/160t abort implemented; running-away scenario not played |
| K19 | NOT_RUN | Owner death cleanup implemented; actual player-death scenario not played |
| K20 | NOT_RUN | Real2-player network session not run |
| K21 | PASS (server) | Two synthetic owners: overlap refused, no shared spawn |
| K22 | NOT_RUN | Real disconnect/reconnect not run |
| R01 | PARTIAL | Exact supplied64px sprites/models/locales checked; inventory/hand render NOT_RUN |
| R02 | PARTIAL | Native heights/UV and offline images checked; actual client reference comparison NOT_RUN |
| R03 | PARTIAL | 54 original OGG fully decoded/registered/subtitled; listening NOT_RUN |
| R04 | NOT_RUN | Independent projected quads implemented; low-particles client not run |
| R05 | NOT_RUN | Static bounded jobs/caps audited; measured performance not run |
| P01 | PASS (server) | Verified actual story kill persists owner victory/participation/deferred reward flags |
| P02 | PASS (server) | Debug kill leaves story count unchanged |
| P03 | PARTIAL | Idempotent finish/no double count and retry/abort tested; second full victory NOT_RUN |
| P04 | PARTIAL | Old items accessible, shared max2 checked, old entity/resource JAR entries preserved; old full combat not repeated |
| P05 | PASS | gradlew.bat clean build; production artifact gates |
| P06 | PASS (scoped) | IDs/resources/common-client isolation and no test leakage checked; original dirty tree preserved |

Ручной список: отдельный survival world; перо/печать/яйцо через Creative или give; полный бой со всеми фазами/дружиной/знамёнами/оковами, полёт/low ceiling/низкие particles, inventory/first-person/звук, реальный relog/портал/два клиента/отключение, длительная навигация и measured MSPT. Пользовательские миры не использовать для автоматических разрушительных тестов.
