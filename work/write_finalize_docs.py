from pathlib import Path
import hashlib, json

root=Path(__file__).resolve().parents[1]
out=root/'docs/port'
lock=json.loads((root/'packaging/test-pack-lock.json').read_text(encoding='utf-8'))
jar=lock['slavicMyths']; instance=lock['installedInstancePath']
def write(path,text): (root/path).write_text(text.strip()+'\n',encoding='utf-8')
old=out/'PORT_STATUS_0.9.4.md'
history=out/'PORT_STATUS_HISTORY_0.9.4.md'
if not history.exists(): history.write_text(old.read_text(encoding='utf-8'),encoding='utf-8')
write('docs/port/PORT_STATUS_0.9.4.md',f'''
# Slavic Myths 0.9.4 — итог переноса, 2026-10-05

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

- `{root.as_posix()}/build/libs/{jar['filename']}`
- SHA-256: `{jar['sha256']}`
- Installed: `{jar['installedPath']}`
- acceptance.json связывает gates с точным JAR и SHA-256 evidence logs.

## PolyMC

`{instance}` — Minecraft 1.21.1 / NeoForge 21.1.255 / LWJGL 3.3.3.
Java override: `{lock['javaPath']}`, memory 512–4096 MiB, отдельная `.minecraft`.
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
''')

summary='Minecraft 1.21.1 / NeoForge 21.1.255 / Java 21. Все production sources компилируются; clean build, headless logic/resource/loader gates A–F PASS. Отдельный PolyMC Slavic-Myths-1.21.1-Testing установлен с семью проверенными JAR. Старый 1.16.5 профиль/миры сохранены. Клиент не запускался; игровая QA ещё не выполнена.'
for name in ['PROJECT_STATUS.md','ROADMAP.md','ARCHITECTURE.md']:
    path=root/'docs'/name;s=path.read_text(encoding='utf-8');old_summary=s.split('\n\n')[1]
    s=s.replace(old_summary,summary,1).replace('Next concrete block: repair real loader/datapack failures; pass all migrated headless + inventory checks; clean production build; verify exact companions and independent PolyMC metadata/mod hashes. Client visual/gameplay QA remains manual. No 0.9.5 scope.',
        'Next concrete block: manual launch of the new profile and fresh-world QA (client/Curios/JEI/AI/structures/persistence), then fix reproduced port regressions. No 0.9.5 or gameplay redesign scope.')
    path.write_text(s,encoding='utf-8')

readme=root/'README.md'; legacy=root/'docs/README_LEGACY_1.16.5.md'
if not legacy.exists(): legacy.write_text('# Historical README — Forge 1.16.5; not current port commands\n\n'+readme.read_text(encoding='utf-8'),encoding='utf-8')
write('README.md',rf'''
# Slavic Myths 0.9.4 — Minecraft 1.21.1 / NeoForge

Существующий контент перенесён на NeoForge **21.1.255**, Java **21**.
Полный clean build и loader/datapack/headless checks проходят. Клиент автоматически не запускался;
готовность к ручному тесту не означает, что игровые сценарии уже проверены.

## Установка и готовая тестовая сборка

Production JAR: `build/libs/{jar['filename']}`.
SHA-256: `{jar['sha256']}`.

В установленном PolyMC уже создан отдельный профиль **Slavic-Myths-1.21.1-Testing**:
`{instance}`. Minecraft 1.21.1 / NeoForge 21.1.255 / Java 21.0.12, память 512–4096 MiB.
Открыть PolyMC вручную, выбрать этот профиль и запускать на **новом тестовом мире**.
При первом запуске launcher загрузит недостающие libraries. Старый профиль и миры 1.16.5 сохранены отдельно.

Обязательная core dependency: **Curios 9.5.1+1.21.1**. JEI опционален для core.
Установленный test pack также содержит JEI **19.51.0.418**, Jade **15.10.6**,
Xaero Minimap **26.5.0**, Xaero World Map **1.45.0**, FallingTree **1.21.1.11**.
В папке mods ровно семь top-level JAR. Не добавлять самостоятельный XaeroLib поверх embedded зависимости.
[Lock с точными файлами, SHA-256 и путями](packaging/test-pack-lock.json).

## Команды 1.21.1

Требуются cheats/OP level 2; generate выполняется игроком в Overworld.
Регистрация команд проходит на headless server; фактические игровые результаты проверять вручную.

```text
/slavicmyths dev yaga locate
/slavicmyths dev yaga generate
/slavicmyths dev yaga status
/give @s slavicmyths:baba_yaga_spawn_egg
/slavicmyths kurgan locate small
/slavicmyths kurgan locate warrior
/slavicmyths kurgan locate great
/slavicmyths kurgan generate small
/slavicmyths kurgan generate warrior
/slavicmyths kurgan generate great
/slavicmyths kurgan info
/slavicmyths dev clear_kurgan_curse
/slavicmyths dev clear_kurgan_curse PlayerName
/locate structure slavicmyths:kurgan_small
/locate structure slavicmyths:kurgan_warrior
/locate structure slavicmyths:kurgan_great
```

`yaga locate` различает запланированный X/Z и реально размещённый дом, не обещает координату Y=0.
`yaga generate` пробует ограниченное число площадок вокруг игрока, не дублирует существующий дом,
откатывает неудачное размещение. Исходный 0.9.3 fix сохранён и дополнен отрицательными высотами мира 1.21.1.
Размещение/отказы на Y=-60/-1/0/3/4/63 проверены headless; natural generation остаётся ручной QA.

## Сборка и проверки без клиента

Из `D:/slavic-myths`, PowerShell:

```powershell
$env:JAVA_HOME='C:/Program Files/Java/jdk-21.0.12'
.\gradlew.bat clean build kurganCheckClasses verifyPortCore verifyPortComponents verifyPortPayloads verifyPortGeometry --console=plain
.\gradlew.bat runRuntimeChecks -PportRuntimeCheck -PwithoutJei --console=plain
.\gradlew.bat runRuntimeChecks -PportRuntimeCheck -PportRuntimeCompanions --console=plain
python tools/verify_static_data_1211.py
python tools/verify_port_1211.py
git diff --check
```

`runRuntimeChecks` запускает только отдельный headless GameTest server в `run-port-checks/`,
загружает реальный NeoForge/mod/datapack, выполняет четыре tests и завершает сервер.
Это не `runClient`, не GUI PolyMC, не запуск сохранённого пользовательского мира.
Gradle aliases `verifyYaga verifyYagaPlacement verifyPortRecipes verifyPortDamage`
автоматически используют этот же loader; несколько aliases в одном вызове дают один server run.
Тестовые классы/resources и companion classes не попадают в production JAR.
Старые source audit/resource generators для 1.16.5 нельзя использовать для перезаписи target resources.

## Сохранённый контент и ограничения

385 items, 153 blocks, 47 entities; все существующие Slavic registry IDs сохранены.
213 recipes, 227 loot tables, 154 advancements действительно загружаются.
Curios slots сохранены; JEI содержит четыре прежние категории.
Для ItemStack используются persistent Data Components с миграцией прежнего custom NBT.
Сохранены охота, боссы, курганы, бандиты, водные/болотные механики, еда,
метла/ступа, мебель, Яга/избушка, уникальные screens/HUD и RPG.
Удалён только общий target HP overlay/config/packet, заменяемый Jade; NPC mace push и boss bars остаются.
547 исходных бинарных art/sound/template assets побайтно не менялись.

[Текущий PORT_STATUS и доказательства](docs/port/PORT_STATUS_0.9.4.md),
[parity всех исходных IDs](docs/port/REGISTRY_PARITY_0.9.4.md),
[QoL dedup](docs/port/QOL_DEDUP_0.9.4.md),
[payload boundaries](docs/port/NETWORK_PAYLOADS_0.9.4.md),
[ручной checklist](docs/MANUAL_QA_0.9.4_1.21.1.md).
AI/бой/анимации/JEI screens/полёт/реальный save-rejoin/генерация требуют ручного игрового теста.
Не заявляется прямая совместимость миров Forge 1.16.5; не открывать оригинальные старые saves новой версией.
Старый подробный контент и команды исходной платформы сохранены в
[архивном README 1.16.5](docs/README_LEGACY_1.16.5.md); это история, не команды текущего порта.
''')

p=root/'docs/port/QOL_DEDUP_0.9.4.md';s=p.read_text(encoding='utf-8')
s=s.replace('BLOCKED (port)','COMPILES; client MANUAL').replace('BLOCKED (full integration)','COMPILES; four categories retained; client MANUAL').replace('NOT STARTED (installation)','DONE (installed); client MANUAL');p.write_text(s,encoding='utf-8')
write('docs/port/FINALIZE_PROGRESS_0.9.4.md','''
# Finalization work completed — 2026-10-05

- DONE: all production APIs/registrars/data/component persistence migrated; no content excluded for build.
- DONE: complete clean build and pure/headless regressions; actual loader tests with and without JEI.
- DONE: gated independent PolyMC install with exact companion pins, SHA-256 and legacy preservation.
- DONE: current PORT_STATUS, README, project/roadmap/architecture, parity/dedup/network/manual QA documents.
- COMPILES: AI, visual/UI/JEI/Curios gameplay/worldgen callbacks and end-to-end persistence.
- BLOCKED: no known current compilation or loader blocker.
- NOT STARTED: manual client/gameplay QA; no Minecraft client auto-launch. No 0.9.5/redesign.

Detailed evidence, all six gates and exact artifact hash: PORT_STATUS_0.9.4.md and acceptance.json.
''')
print('Current documentation updated; historical checkpoints/README preserved separately.')
