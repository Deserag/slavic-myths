# Slavic Myths 0.9.4 — итог переноса, 2026-10-05

> Исторический отчёт именно об артефакте 0.9.4. Текущий 0.9.5 добавляет навигацию
> по следующему пакету пользователя: [NAVIGATION_0.9.5](../NAVIGATION_0.9.5.md),
> [отдельная acceptance](../verification/navigation-0.9.5/acceptance.json).
> Старые acceptance/log hashes не перезаписаны. JAR 0.9.4 сохранён в
> `.tools/port-backups/slavicmyths-0.9.4-final.jar`; текущий build/libs содержит 0.9.5.

## Platform

Minecraft **1.21.1**, NeoForge **21.1.255**, Java **21.0.12**, ModDevGradle 2.0.148,
Gradle 9.2.1. Весь production source set, включая client/AI/worldgen, компилируется.
Clean build и A–F gates **PASS**. Профиль установлен для ручного запуска;
это не подтверждение игрового тестирования. Клиент не запускался.
Исторические BLOCKED checkpoints: [PORT_STATUS_HISTORY_0.9.4.md](PORT_STATUS_HISTORY_0.9.4.md).

## Current systems

Статус DONE ниже относится к указанной проверенной области, не к полной игровой QA.

| System | Status | Проверенная область / ограничения |
|---|---|---|
| Build / API migration | DONE | Все production классы Java 21; нет Forge 1.16.5 imports, CompoundNBT, SimpleChannel или WorldSavedData. Нет исключения контента из main и временных заглушек. |
| Registries | DONE (registration) | Реальный loader проверил 794 entries: 385 items, 153 blocks, 47 entities, 147 sounds и остальные типы. Все 47 entities создаются, LivingEntity attributes присутствуют. IDs сохранены. |
| Resources / static content | DONE (loading/parity) | Все 213 recipes, 227 loot tables, 154 advancements; target tags, damage/enchantment/worldgen codecs загружаются. 660 исходных JSON после schema-normalization эквивалентны; 547 PNG/OGG/NBT побайтно прежние. 2288 static references, ошибок 0. |
| Item state | DONE (codec/migration) | Data Components для runes, horn target, cloth, trophies, flight cargo. Nested stacks проходят DFU; copy/roundtrip/nested runes/чужие keys/invalid-slot atomic rejection проверены под loader. Смерть и перезаход: MANUAL. |
| Curios | DONE (registration/data), COMPILES (gameplay/render) | Реальные player slots head/necklace/ring/belt/charm = 1/1/2/1/1. Современные capability/accessory callbacks и models компилируются. Equip/death/login: MANUAL. |
| JEI | COMPILES | Четыре исходные категории/transfer перенесены. Core server действительно загрузился без JEI; все companion pins также прошли loader boot. Client screen/transfer: MANUAL. |
| Entities / AI / bosses / damage | COMPILES | Synched data, attributes, goals, projectile/damage APIs, spawn events и renderer contexts перенесены; registry construction проверена. Боевые сценарии/AI/фазы: MANUAL. |
| Screens / client / networking | COMPILES | GuiGraphics, menu registration, keys/layers, payload handlers. Roundtrip/defensive copies/NaN/Inf/truncated packets PASS; интерактивный multiplayer/client: MANUAL. |
| Authored art / model geometry | DONE (assets/CPU checks), COMPILES (renderers) | Исходные textures/sounds сохранены, 17 model geometries и UV/pivots/opacity проверены. Фактический вид/анимации/звук: MANUAL. |
| SavedData / player data | COMPILES; tested subsets PASS | Provider-aware save/Factory для всех семи, NBT/isolation/Hunt/Kurgan/Yaga logic tests PASS. Полный real save/death/logout/reload: MANUAL. |
| Structures / trees / biomes | DONE (registration/data), COMPILES (placement) | Все 13 structure/type/set IDs, 5 pieces, старые spacing/separation/salts и планы сохранены. Реальные datapacks загружаются; не означает проверку natural generation/frequency. Tree shapes и следующий RNG идентичны на 1024 seeds. |
| Yaga 0.9.3 hotfix | DONE (headless regression), COMPILES (runtime interaction) | Яйцо сохранено. Старые preflight/rollback/assertions и bounded search; дополнены negative-height tests (-60/-1). Нормальная поверхность Y=0/3/4/63, двери/опоры/запрет water/container/unloaded chunks PASS. Natural spawn/dialogue: MANUAL. |
| Hunt / Kurgan logic | DONE (headless regression), COMPILES (gameplay) | 1200 планов, 900 rosters, Hunt I/II/boss rules, persistence boundaries PASS. Квесты/loot/curse/AI в игре: MANUAL. |
| Commands | COMPILES / registered by server | Современные registry lookup, random-spread placement, height/bounds, safe teleport. Интерактивные locate/generate проверять по README. |
| QoL dedup | DONE (source scope) | Удалён только generic CombatHud/config/CombatTarget. Jade заменяет HP overlay. NPC mace push, boss bars и уникальные screens/HUD сохранены. |
| PolyMC / pinned companions | DONE (installation/hash) | Настоящий отдельный OneSix profile, 7 top-level JAR, SHA-256 checked, official component metadata. INSTALLED_NOT_LAUNCHED. |

BLOCKED production compile/load systems: **нет выявленных**. Игровая/client QA **NOT STARTED** по запрету автозапуска.
Дальнейший post-port redesign **NOT STARTED** и не входит в эту работу.

## Gates and evidence

| Gate | Result | Evidence |
|---|---|---|
| A Java/config/dependencies | PASS | Реальный Java 21 toolchain и exact target artifacts в clean build |
| B main + client compilation | PASS | finalize-clean-build.log, все production sources |
| C production build | PASS | clean build; Java class version 65; тесты/companion classes не включены в JAR |
| D resources/data | PASS | stage2-data-audit.json, resource-check.json, final-check.json; реальный server reload всех recipes/loot/advancements/worldgen |
| E existing checks | PASS | finalize-clean-build.log, finalize-runtime-nojei.log, finalize-runtime-companions.log; Yaga/placement/recipe assertions сохранены |
| F dedicated/common loader smoke | PASS | Два режима GameTest server: core+Curios без JEI и шесть точных companion pins; все 4 tests PASS в каждом |

За эту сессию: **6** реальных headless GameTest server boots (1 ранний тестовый отказ, затем 5 успешных),
отдельно один ранний loader failure до запуска сервера. Клиентских запусков **0**.
Последний дополнительный запуск проверяет четыре Gradle aliases на одном loader: finalize-runtime-aliases.log.
Все ранние ошибки исправлены; старые failed logs — история, не текущий результат.
Не измерялись MSPT, клиентский FPS и баланс/частота генерации.

## Production artifact

- `D:/slavic-myths/build/libs/slavicmyths-0.9.4.jar`
- SHA-256: `552a1cda30e0cab96bec6993523e444b34a42c7c947b10c0eeb2103ccf358a9e`
- Installed: `C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing/.minecraft/mods/slavicmyths-0.9.4.jar`
- acceptance.json связывает gates с точным JAR и SHA-256 evidence logs.

## PolyMC

`C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing` — Minecraft 1.21.1 / NeoForge 21.1.255 / LWJGL 3.3.3.
Java override: `C:/Program Files/Java/jdk-21.0.12/bin/javaw.exe`, memory 512–4096 MiB, отдельная `.minecraft`.
PolyMC 7.0 OneSix schema сверена с реально установленным launcher; metadata получена из
`https://meta.polymc.org/v1/`, не из выдуманных patch IDs. При первом **ручном** запуске
launcher может скачать недостающие game/loader libraries. Ни клиент, ни GUI PolyMC не запускались.
Старый `Slavic-Myths-Testing` 1.16.5, его JAR/config/pack SHA-256 неизменны; saves не копировались/не конвертировались.

## Companion mods

Curios 9.5.1+1.21.1; JEI 19.51.0.418; Jade 15.10.6;
Xaero Minimap 26.5.0; Xaero World Map 1.45.0; FallingTree 1.21.1.11.
Точные filenames, project/file IDs, SHA-256, source URLs и installed paths:
`packaging/test-pack-lock.json`. Embedded XaeroLib 1.7.1 выбран самим loader;
дополнительный standalone JAR не установлен. Все 7 hashes проверены после копирования.

## Manual QA / remaining regressions

[Ручной чеклист](../MANUAL_QA_0.9.4_1.21.1.md). Следующий конкретный блок:
пользователь запускает новый профиль, создаёт свежий тестовый мир и проверяет Curios/JEI,
рендер/управление, Ягу/избушку, структуры/AI, повторный вход и два клиента на сервере.
Документировать реальные воспроизведения и чинить port regressions отдельно.
Нет выявленного текущего build/loader blocker; отсутствие client QA не даёт права заявлять «игра проверена».
Биомные проверки для удалённых legacy biome-depth и Forge category API выражены target tags;
natural biome/terrain coverage требует ручной сверки. Старый мир 1.16.5 не обещан как совместимый для загрузки напрямую.
Worldgen quality/frequency, старые AI/loot/art/HUD backlog и 0.9.5 не перерабатывались.


## Launcher checksum repair — 2026-10-05

Первый ручной запуск остановился до загрузки Minecraft: ForgeWrapper не обнаружил installer. PolyMC-0.log показывает Checksum mismatch для обоих NeoForge 21.1.255 JAR. Official PolyMC metadata содержит SHA-1, отличающиеся от actual official NeoForge Maven .sha1; файлы launcher отверг, оставив отсутствующий installer. Установлены проверенные official installer/universal, создан instance-local patches/net.neoforged.neoforge.json с исправленными SHA-1/size. Global metadata cache, моды, версия NeoForge и старые профили не изменены.

Реальный ForgeWrapper detector теперь находит installer и Minecraft JAR (launcher-detector-check.log); это не запуск игры. Контрольные суммы и пути: launcher-repair-0.9.4.json и packaging/test-pack-lock.json. tools/verify_polymc_094.py теперь также проверяет эти launcher artifacts. Полностью закрыть и открыть PolyMC вручную, чтобы перечитать локальный patch, затем повторить запуск. Клиент автоматически не запускался. Первоначальная проверка profile metadata/mod hashes не обнаруживала отсутствие installer; эта область проверки расширена.


## Дополнительный post-port backlog, обнаруженный в аудите 0.9.6

NOT STARTED: 20 старых ссылок моделей дверей пяти семейств на отсутствующие vanilla parents 1.21.1. Это прежние ресурсы 0.9.5, оставленные без изменений в курганном этапе. Точный список: `docs/verification/kurgan-0.9.6/resource-check.json`, existingBaselineAssetIssues. Исторические gates переноса выше не переписаны.


## Обновление дополнительного backlog в 0.9.7

DONE по resource validation: linden_door — 32 native states / актуальные модели и старые aliases, четыре прежних missing parents исправлены. Остальные 16 (darkened/pine/rowan/willow) — NOT STARTED. Xaero/JEI runtime hotfix описан в docs/swamp/SWAMP_0.9.7_RUNTIME_FIXES.md; ручная проверка клиента не выполнена.
