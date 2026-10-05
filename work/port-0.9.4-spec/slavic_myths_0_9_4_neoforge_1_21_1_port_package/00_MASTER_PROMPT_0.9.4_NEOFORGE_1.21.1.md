# SLAVIC MYTHS — MASTER CODEX PORT SPECIFICATION
## Version target: 0.9.4 — Minecraft 1.21.1 / NeoForge
## Current source: Minecraft 1.16.5 / Forge 36.2.42 / Java 8
## Target source: Minecraft 1.21.1 / NeoForge / Java 21
## Mod ID MUST remain: `slavicmyths`
## Repository: https://github.com/Deserag/slavic-myths
## Status: MIGRATION-ONLY RELEASE — NO CONTENT REDESIGN

---

# 0. КЛЮЧЕВОЕ РЕШЕНИЕ ПРОЕКТА

Начиная с 0.9.4 развитие Slavic Myths на Minecraft 1.16.5 прекращается.

После выполнения этого задания:
- единственная рабочая ветка развития проекта — Minecraft 1.21.1 + NeoForge;
- все последующие багфиксы, переработка курганов, болота, структур, предметов, UI, зачарований, AI и баланса выполняются ТОЛЬКО на 1.21.1;
- поддерживать параллельно две версии НЕ нужно;
- публичный финальный GitHub release будет готовиться позже, когда продукт станет релизным.

Это означает:
- не создавать параллельный `src_1_21`;
- не сохранять две сборочные системы в одном проекте;
- не делать dual-loader;
- не пытаться одновременно поддерживать Forge 1.16.5 и NeoForge 1.21.1;
- текущий проект переписывается ВНУТРИ существующего рабочего дерева под новую платформу.

Git:
- НЕ делать `git push`;
- НЕ создавать GitHub Release;
- НЕ создавать теги;
- НЕ переписывать историю;
- НЕ удалять `.git`;
- можно использовать `git status`, `git diff`, `git log` только для анализа изменений.

---

# 1. ЦЕЛЬ 0.9.4

Цель этой версии НЕ «улучшить мод».

Цель:
> получить функционально эквивалентный текущему Slavic Myths проект, который собирается и работает на Minecraft 1.21.1 + NeoForge + Java 21.

Нужно сохранить существующий контент и поведение настолько близко, насколько позволяет новый API.

В этой версии ЗАПРЕЩЕНО намеренно исправлять или перерабатывать:
- дизайн курганов;
- частоту структур;
- пустой loot структур;
- баланс старых мобов;
- слабые текстуры;
- интерфейсы;
- баню;
- болото;
- разбойничьи базы;
- зачарования;
- мебель;
- HUD здоровья;
- TreeCapitator/FallingTree;
- новые мод-интеграции кроме тех, которые уже нужны текущему контенту;
- механики, которые помечены на будущую переработку.

Исключение:
если старый код нельзя напрямую перенести из-за удаления API, допустимо сделать современный эквивалент с тем же игровым результатом.

---

# 2. ИСХОДНОЕ СОСТОЯНИЕ, КОТОРОЕ УЖЕ ПОДТВЕРЖДЕНО

Текущий `build.gradle`:
- ForgeGradle `5.1.77`;
- Forge `1.16.5-36.2.42`;
- official mappings `1.16.5`;
- Java toolchain 8;
- Curios Forge 1.16.5;
- JEI 1.16.5;
- version `0.9.3`.

Текущий проект также имеет:
- headless verify tasks для kurgan/hunt/Yaga;
- client/server dev runs;
- мод ID `slavicmyths`.

README 0.9.3 подтверждает существование:
- Baba Yaga;
- Yaga Hut controlled placement;
- Yaga favor/progression;
- Yaga quests/exchange/cauldron;
- Putevodny Klubok;
- existing broom/mortar integration;
- Hunt 0.9.0–0.9.1;
- Likho and Tugarin world bosses 0.9.2;
- Kurgan/Hunt verification tools;
- Curios accessories;
- JEI categories;
- custom SavedData / world anchors;
- custom commands.

НЕ полагайся только на этот список.
Перед переносом необходимо сделать реальный inventory по исходникам/ресурсам.

---

# 3. ОФИЦИАЛЬНАЯ ЦЕЛЕВАЯ ПЛАТФОРМА

Minecraft:
`1.21.1`

Loader:
`NeoForge`

Java:
`21`

Использовать официальный NeoForge MDK/рекомендуемую современную Gradle-конфигурацию для 1.21.1 на момент выполнения.

НЕ фиксировать случайный старый beta NeoForge, если доступен стабильный 1.21.1 release.

Официальные документы, на которые следует ориентироваться:
- https://docs.neoforged.net/docs/1.21.1/gettingstarted/
- https://docs.neoforged.net/docs/1.21.1/gettingstarted/modfiles/
- https://docs.neoforged.net/docs/1.21.1/items/
- https://docs.neoforged.net/docs/1.21.1/items/datacomponents/
- https://docs.neoforged.net/docs/1.21.1/datastorage/attachments/
- https://docs.neoforged.net/docs/1.21.1/datastorage/saveddata/
- https://docs.neoforged.net/docs/1.21.1/networking/
- https://docs.neoforged.net/docs/1.21.1/gui/screens/
- https://docs.neoforged.net/docs/1.21.1/worldgen/biomemodifier/
- https://docs.neoforged.net/primer/docs/

Если старый код конфликтует с памятью модели:
НЕ угадывать API.
Смотреть фактический NeoForge 1.21.1 API / sources / official docs.

---

# 4. ОБЯЗАТЕЛЬНАЯ СТРАТЕГИЯ: PORT BY GATES

Не пытайся за один шаг хаотично заменять тысячи импортов.

Работать gate-by-gate:

GATE 1 — inventory and build system.
GATE 2 — registries + resources + plain blocks/items.
GATE 3 — persistent item state + accessories + recipes/loot/advancements.
GATE 4 — entities + AI + models/renderers + effects.
GATE 5 — networking + menus/screens + commands.
GATE 6 — SavedData/world anchors/worldgen/structures.
GATE 7 — Curios + JEI + integration parity.
GATE 8 — headless validation + dedicated-server smoke + final parity audit.

После каждого gate:
- compilation must not become knowingly worse;
- update `docs/port/PORT_STATUS_0.9.4.md`;
- record migrated systems;
- record blockers;
- do not mark a system DONE merely because class compiles.

---

# 5. ТОКЕН-ЭКОНОМИЯ ДЛЯ CODEX

Пользователь хочет ограничить расход модели.

Поэтому:

1. Не делай длинный prose-анализ репозитория.
2. Сначала используй file search / grep:
   - registries;
   - Forge imports;
   - capabilities;
   - SimpleChannel/network;
   - CompoundNBT/ItemStack NBT;
   - SavedData;
   - worldgen;
   - screens/containers;
   - Curios;
   - JEI.
3. Не перечитывай большие файлы без необходимости.
4. Не открывай PNG как бинарный контент, если перенос не требует изменения изображения.
5. Не пересоздавай ассеты, которые уже валидны.
6. Не генерируй новые концепты/текстуры.
7. Не переписывай README до тех пор, пока основные gates не завершены.
8. Не исправляй backlog bugs, если они не мешают порту.
9. Делай минимальные современные эквиваленты старой логики.
10. Не создавай альтернативную архитектуру просто потому, что она «красивее».

---

# 6. ОБЯЗАТЕЛЬНЫЙ INVENTORY ПЕРЕД ИЗМЕНЕНИЯМИ

Создать:
`docs/port/REGISTRY_INVENTORY_1.16.5.md`

Автоматически/скриптом собрать ВСЕ public IDs из:
- items;
- blocks;
- block entities;
- entity types;
- mob effects;
- sound events;
- menu types;
- particles;
- enchantments, если есть;
- recipes / custom recipe types/serializers;
- structures;
- biome/worldgen entries;
- custom data/saved-data file names;
- commands roots/subcommands;
- advancements;
- loot tables;
- tags;
- Curios slot identifiers;
- custom JEI categories.

Для каждой строки:
`OLD ID | TARGET ID | STATUS | NOTES`

Главное правило:
PUBLIC REGISTRY IDS сохранять без изменения, если новый Minecraft не делает ID технически невозможным.

Пример:
`slavicmyths:baba_yaga` -> `slavicmyths:baba_yaga`
`slavicmyths:tugarin_zmey` -> same
`slavicmyths:putevodny_klubok` -> same

НЕ делать массовое переименование на английские красивые IDs во время порта.

---

# 7. BUILD SYSTEM — ПОЛНАЯ ЗАМЕНА 1.16.5 FORGE

Удалить старую зависимость от:
- ForgeGradle 5.1.x;
- `net.minecraftforge:forge:1.16.5-36.2.42`;
- Java 8 toolchain;
- `reobfJar`-специфичную старую конфигурацию, если она не существует в новом toolchain.

Перейти на официальный NeoForge 1.21.1 MDK style.

Требования:
- Java toolchain = 21;
- wrapper/Gradle version совместим с выбранным NeoForge 1.21.1 MDK;
- `gradle.properties` содержит нормальные mod metadata variables;
- build produces normal NeoForge production JAR;
- UTF-8 retained;
- server/client development configurations retained where useful.

Версия проекта:
`0.9.4`

Artifact:
желательно:
`slavicmyths-0.9.4+mc1.21.1.jar`
или другой чистый versioned name, если существующий workflow проще сохранить.

---

# 8. MOD METADATA

Старый:
`META-INF/mods.toml`

Новый:
`META-INF/neoforge.mods.toml`

Сохранить:
- modId = `slavicmyths`;
- display name;
- author/description where currently present;
- version from Gradle properties.

Dependencies:
- Minecraft exactly/compatible with 1.21.1;
- NeoForge target version;
- Curios if it remains required;
- JEI should remain OPTIONAL unless current runtime design truly requires it.

No old Forge-only dependency declaration.

---

# 9. CURIO / JEI TARGET DEPENDENCIES

Current 0.9.3 uses Curios and JEI.

For 1.21.1:
- use NeoForge build of Curios;
- use NeoForge build of JEI.

Known stable 1.21.1 Curios exists (e.g. 9.5.1+1.21.1).
Known current JEI 1.21.1 NeoForge builds exist.

DO NOT:
- use Forge artifact accidentally;
- use `-api.jar` as runtime implementation;
- shade Curios or JEI into Slavic Myths;
- require JEI for core gameplay.

Preserve current behavior:
- Curios-backed accessories remain Curios-backed;
- JEI categories work when JEI is installed;
- mod still loads without JEI if it previously did.

---

# 10. PACKAGE/IMPORT MIGRATION RULE

Replace old:
`net.minecraftforge.*`

with correct:
- `net.neoforged.*`
and/or modern Mojang/NeoForge equivalents.

Do NOT global string-replace blindly.

Search all Forge imports, categorize:
- event bus;
- registries;
- networking;
- capabilities;
- client events;
- common events;
- worldgen;
- item handlers;
- config;
- dist/client handling.

Migrate API by API.

---

# 11. REGISTRIES

Use modern NeoForge `DeferredRegister` / specialized helpers where appropriate.

Preserve IDs.

Categories:
- items;
- blocks;
- entity types;
- sounds;
- effects;
- menus;
- particles;
- serializers/components/attachments as needed.

Do NOT rely on static-init registration order hacks.

Add all registers to correct mod event bus.

---

# 12. RESOURCELOCATION MIGRATION

Minecraft 1.21 changed `ResourceLocation` construction.

Replace old constructors with valid modern methods:
- `ResourceLocation.fromNamespaceAndPath(namespace, path)`
- `ResourceLocation.parse(...)`
or correct 1.21.1 equivalents.

Do not use reflection hacks.

---

# 13. RESOURCE/DATAPACK DIRECTORY MIGRATION

Minecraft 1.21 uses singular registry folder names for multiple resource/data paths.

Audit and migrate at least:

- `tags/blocks` -> `tags/block`
- `tags/items` -> `tags/item`
- `tags/entity_types` -> `tags/entity_type`
- `tags/fluids` -> `tags/fluid`
- `advancements` -> `advancement`
- `recipes` -> `recipe`
- `structures` -> `structure`
- `loot_tables` -> `loot_table`

Also inspect:
- predicates;
- worldgen directories;
- neoforge biome modifiers;
- custom registry data.

DO NOT leave old duplicate folders active «на всякий случай».
After migration, use one valid 1.21.1 layout.

---

# 14. RESOURCES / TEXTURES / MODELS

Migration-only rule:

Existing art:
- copy/preserve exact PNGs;
- preserve high-resolution textures;
- preserve model JSONs where format remains valid;
- fix paths/names only where 1.21.1 resource format requires.

DO NOT:
- redraw items;
- reduce 512/256 textures;
- upscale old assets during this port;
- alter palettes;
- redesign mobs.

Visual redesign happens in later versions.

If an old item/block model schema is invalid:
port JSON format with identical visible intent.

---

# 15. ITEMSTACK STATE: NBT -> DATA COMPONENTS

Minecraft 1.21.1 ItemStack state is component-oriented.

Search source for:
- `getOrCreateTag`
- `getTag`
- `CompoundNBT`
- `CompoundTag`
- item stack serialization
- arbitrary NBT keys on items.

Categorize each state.

Known likely stateful systems include:
- Hunting Horn selected target;
- Trophy Pouch contents;
- Broom/Mortar stored data;
- other container-like/utility items;
- cooldown/service metadata;
- ritual state stored on stacks.

Preferred migration:
- define custom `DataComponentType` for structured persistent stack state;
- use proper Codec and StreamCodec;
- immutable record-like component values where practical.

Use `CustomData` only for truly generic legacy data where a real component brings no value.

Do not keep writing arbitrary legacy item NBT through obsolete API.

Preserve gameplay semantics.

---

# 16. TROPHY POUCH / ITEM INVENTORIES

If a portable inventory is stored inside ItemStack:
- migrate state to a reliable 1.21.1 component representation;
- preserve slot count;
- preserve item filters;
- prevent nesting/dupes;
- preserve contents through drop, clone, save/load;
- network only what client GUI needs.

Do not redesign its capacity in port version.

---

# 17. PLAYER-SPECIFIC DATA

Search for Forge capabilities / custom player state.

Typical project states:
- Baba Yaga favor/progress;
- active hunt/boss ownership;
- cooldown/progression flags;
- other persistent player mechanics.

Target choices:
- NeoForge Data Attachments for entity/player-specific data;
- SavedData for level/world global state.

Rules:
- if state belongs to player -> attachment or existing suitable modern system;
- if state belongs to world -> SavedData;
- do not put everything in one global static map.

For player attachments:
- configure persistence;
- configure copy-on-death only when old gameplay expects it;
- client sync via networking only when screen/client actually needs it.

---

# 18. SAVEDDATA MIGRATION

Preserve current world-level file names where possible.

Known examples include:
- Yaga hut anchor/progress world state;
- hunt/world boss anchors;
- kurgan state;
- other generated-location state.

Use 1.21.1 `SavedData` + `SavedData.Factory` / current API.

Every mutation MUST mark data dirty.

Do not silently change serialization keys unless required.

Create version field inside complex SavedData if useful:
`data_version: 1`

But do not invent full DataFixerUpper migration framework in this patch.

---

# 19. SAVE COMPATIBILITY POLICY

Do not promise that a heavily modded 1.16.5 world can be perfectly upgraded to 1.21.1.

Goal:
- preserve Slavic Myths registry IDs;
- preserve resource IDs;
- preserve saved-data file names/fields where practical;
- avoid unnecessary incompatibility.

Primary validation world for 0.9.4:
NEW 1.21.1 test world.

Optional:
attempt opening a COPY of an upgraded old world only after fresh-world validation.

Never test migration on the user's only save.

Document:
`docs/port/SAVE_COMPATIBILITY_0.9.4.md`

with:
- expected compatibility;
- known unsupported migration;
- what old custom data can/cannot be read.

---

# 20. NETWORKING

Old Forge SimpleChannel patterns must be replaced with NeoForge 1.21.1 payload system.

Use:
`RegisterPayloadHandlersEvent`
and typed `CustomPacketPayload` / StreamCodec.

Inventory all old packets.

For each:
`OLD PACKET | DIRECTION | NEW PAYLOAD | STATUS`

Server authority remains mandatory.

Especially:
- Yaga UI actions;
- quest submit/exchange;
- cauldron;
- custom item GUI;
- hunt selection/actions if networked;
- furniture/seat interactions if networked;
- any transport input packets.

Never trust arbitrary client item counts/progress.

---

# 21. GUI / MENUS / SCREENS

Port existing screens without redesign.

Use modern:
- MenuType registration;
- AbstractContainerMenu;
- AbstractContainerScreen;
- `RegisterMenuScreensEvent`;
- modern GuiGraphics/render signatures.

Screens include at least whatever exists in source, especially:
- Baba Yaga UI;
- cauldron;
- Trophy Pouch;
- Book/other custom interfaces;
- crafting/ritual interfaces;
- furniture-related UI if any.

Migration rule:
layout and behavior remain as current version.

Do not «improve» UI yet.

---

# 22. CLIENT/SERVER SEPARATION

Audit all client-only references.

No client classes on dedicated server common code.

Move/register client-only:
- screens;
- renderers;
- model layers;
- key mappings;
- HUD overlays;
- client events.

Dedicated server must start without ClassNotFound / Dist errors.

---

# 23. ENTITY TYPES / ATTRIBUTES / SPAWN

Port all entity registrations.

Preserve:
- IDs;
- dimensions/hitboxes;
- HP/damage/speed;
- AI timing;
- loot;
- spawn eggs;
- boss bars.

Register attributes via correct NeoForge 1.21.1 events.

Spawn restrictions:
register via current spawn placement event where natural spawn uses vanilla rules.

DO NOT rebalance entities during migration.

---

# 24. ENTITY MODELS / RENDERERS

Preserve detailed 0.9.x models:
- Likho;
- Tugarin;
- Fire Serpent;
- Podvey;
- Ovinnik;
- Volkolak;
- Baba Yaga;
- earlier mobs.

Rendering API changed significantly between 1.16.5 and 1.21.1.

Port:
- ModelPart/LayerDefinition patterns as appropriate;
- entity renderer registration;
- RenderType usage;
- emissive layers;
- translucent entities;
- custom held/accessory models.

Do not simplify model geometry because porting is difficult.

---

# 25. ANIMATION BEHAVIOR

Preserve current custom animation/state logic.

No GeckoLib migration in 0.9.4 unless:
- current source already depends on it, OR
- an existing animation is literally impossible to port without replacing the framework.

Default:
do NOT add GeckoLib during migration.

Later audit may decide.

---

# 26. MOB EFFECTS / ATTRIBUTES

Port custom effects like:
- Durnaya Dolya;
- kurgan-related effects/curses;
- any existing custom effects.

Preserve durations/math.

Use modern effect registration.

Audit attribute modifier API changes.

---

# 27. DAMAGE SOURCES

Minecraft damage source/type APIs changed significantly since 1.16.5.

For custom damage:
- use correct 1.21.1 DamageType/DamageSource patterns;
- prefer vanilla damage types where semantic match exists;
- custom damage types must be data-driven if required by current API.

Preserve:
- fire behavior;
- magic behavior;
- shield logic;
- armor interaction;
- boss damage semantics.

---

# 28. COMMANDS

Port all existing `/slavicmyths` commands.

Preserve subcommand names unless technically impossible.

Known current examples:
- `/slavicmyths dev yaga locate`
- `/slavicmyths dev yaga generate`
- `/slavicmyths dev yaga status`
- boss dev commands;
- hunt/kurgan dev commands.

Port Brigadier API/event registration.

Migration-only:
do NOT yet replace them with `/locate`.
That redesign is post-port backlog.

Commands must:
- register correctly;
- provide valid syntax errors;
- not appear in suggestions if unusable due missing permission where avoidable.

---

# 29. YAGA 0.9.3 PARITY GATE

0.9.4 is NOT DONE if Baba Yaga is lost.

Must preserve:
- `slavicmyths:baba_yaga`;
- 40 HP / 0.23 speed unless source differs;
- canonical hut NPC concept;
- hut saved anchor;
- deterministic candidates;
- first interaction;
- favor statuses;
- quest chain;
- exchange;
- cauldron;
- Putevodny Klubok;
- punishment;
- repeatable contracts;
- JEI cauldron integration;
- saved progress.

Known current quest chain:
- Не с пустыми руками;
- Жар и ветер;
- Докажи силу.

Preserve current IDs and behavior.

DO NOT fix hut placement bug in this migration unless old algorithm cannot function at all in 1.21.1.
Port bug-for-bug where feasible, then fix in next dedicated worldgen version.

---

# 30. WORLD BOSSES 0.9.2 PARITY GATE

Preserve:
- Likho One-Eyed;
- Tugarin Zmey;
- boss bars;
- phases;
- ritual summons;
- controlled regional anchors;
- respawn/cooldown state;
- DashFacing orientation fix;
- loot;
- spawn eggs;
- Hunter Charm integration.

No rebalance.

---

# 31. HUNT 0.9.0–0.9.1 PARITY GATE

Preserve 4 Hunt targets:
- Ovinnik;
- Volkolak;
- Fire Serpent;
- Podvey.

Preserve:
- Hunting Horn selection;
- summon conditions;
- encounter anchors/state;
- Hunter Charm detection;
- Trophy Pouch;
- loot;
- recipes;
- accessories;
- spawn eggs;
- natural spawn behavior as currently implemented.

Do not fix rarity here.

---

# 32. KURGAN PARITY GATE

Port existing kurgan generation exactly enough to preserve current content.

Must preserve:
- tiers/types;
- current labyrinth generation;
- custom blocks;
- rooms;
- encounters;
- curse;
- boss/miniboss integration;
- debug/generation commands;
- saved state.

DO NOT:
- add chests now;
- redesign labyrinth;
- add slabs now;
- redo block textures now;
- change spawn frequency now.

Those are post-port tasks.

---

# 33. BANDIT / STRUCTURE PARITY GATE

Port all currently implemented bandit:
- mobs;
- camps;
- larger bases/settlements;
- tower/structures;
- Solovey-related content;
- loot as currently defined.

Do not recolor/rebuild them yet.

---

# 34. WATER / SWAMP / FISHING PARITY GATE

Port current:
- fishing overhaul;
- fish;
- nets;
- water/swamp mobs;
- water/swamp structures/POI;
- plants;
- foods;
- related worldgen.

Do not decide whether custom swamp biome should be removed in 0.9.4.

That is later redesign.

---

# 35. FOOD / COOKING / OTHER EXISTING CONTENT

Port all currently registered:
- food;
- flour/mill;
- berries;
- kitchen/cooking systems;
- herbs;
- crafting stations;
- accessories;
- tools;
- weapons;
- armor;
- artifacts;
- decorative items;
- mobility items.

Inventory source code to find exact list.

No item may disappear silently.

---

# 36. RECIPES

Minecraft 1.21 recipe API and resource folder changed.

Port:
- vanilla JSON recipes;
- custom serializers/types;
- cooking/ritual/Yaga recipes.

Use modern `RecipeInput` API where custom recipe code needs it.

Ensure every 1.16.5 craftable item that should still be craftable has equivalent 1.21.1 recipe.

Create a comparison verifier if practical:
old resource IDs vs new resource IDs.

---

# 37. LOOT TABLES

Move resources to correct 1.21 path.

Preserve existing loot output.

Port custom/global loot modifiers if project has them.

Do not enrich empty structures yet.

Validation:
all referenced item IDs resolve.

---

# 38. ADVANCEMENTS

Move to singular `data/.../advancement/`.

Port triggers/criteria.

Preserve IDs and progression where possible.

Do not reorganize categories yet.
The PDF complaint about advancement screen is post-port backlog.

---

# 39. ENCHANTMENTS

If current repository contains custom enchantments:
port only those already implemented.

Minecraft 1.21 uses datapack registry enchantments / modern effect system.

Do not add the previously planned missing enchantments yet.

Do not fix broad vanilla/modded compatibility yet unless required for an existing enchantment to retain old behavior.

Future dedicated enchantment rework follows after port.

---

# 40. WORLDGEN

This is a migration, not rework.

For each current worldgen feature:
identify whether best 1.21.1 equivalent is:
- configured/placed feature;
- NeoForge biome modifier;
- vanilla structure registry;
- custom runtime controlled anchor;
- saved encounter system.

Use data-driven NeoForge systems where equivalent.

Do NOT force all custom controlled systems into vanilla Structure registry during the port if that would redesign behavior.

Examples:
- normal ore/tree/plant/spawn injection -> biome modifiers/data driven;
- Yaga/world bosses may remain controlled saved anchors for parity;
- kurgans/large structures should keep gameplay behavior first.

---

# 41. BIOME MODIFIERS

Where old code uses 1.16 biome loading events for:
- ores;
- plants;
- mob spawns;
- features;

migrate to NeoForge 1.21.1 biome modifier JSON/datagen when appropriate.

Directory:
`data/slavicmyths/neoforge/biome_modifier/`

Avoid feature cycle violations.

Use semantic biome tags where possible.

No spawn-frequency redesign in this patch.

---

# 42. STRUCTURE NBT / TEMPLATE DATA

Audit `.nbt` structure templates.

Preserve files.

If data version conversion is needed:
- use vanilla/official mechanisms where possible;
- do not hand-corrupt NBT;
- test template load in 1.21.1.

Record incompatible templates.

Do not redraw buildings.

---

# 43. CONFIG

Port current config if exists.

If project has no proper user config:
do NOT invent a giant one during migration.

Only migrate existing knobs.

Later versions can expose generation frequency.

---

# 44. HEADLESS VERIFY TASKS

Current project has custom verify tasks:
- verifyKurgan;
- verifyHunt;
- verifyYaga.

Port or replace them so equivalent non-client checks still exist.

Preferred:
retain task names if possible:
`verifyKurgan`
`verifyHunt`
`verifyYaga`

Update source/test class imports/API.

They must remain pure JVM/headless where possible.

---

# 45. RESOURCE VERIFICATION

Preserve/adapt existing Python resource verification tools.

Update expected paths:
- recipe singular;
- advancement singular;
- loot_table singular;
- tags singularized.

Add new verifier:
`tools/verify_port_1211.py`

Checks at least:
- no `data/*/recipes` remains;
- no `data/*/advancements` remains;
- no `data/*/loot_tables` remains;
- no `tags/items`, `tags/blocks`, `tags/entity_types`;
- no unresolved `1.16.5` text in build metadata except documentation/history;
- no Forge 36.2.42 dependency;
- Java target is 21;
- neoforge.mods.toml exists;
- mods.toml old active file absent;
- public registry IDs inventory has target mapping.

---

# 46. DEDICATED SERVER VALIDATION

User does NOT want automatic GUI game windows.

NEVER run `runClient`.

Allowed after compile/build:
- headless dedicated server smoke, only if it does not open a GUI and can be automatically terminated;
- resource/datagen/headless tests.

Dedicated server smoke goal:
- mod loads;
- registries freeze correctly;
- no client-class crash;
- datapacks load;
- recipes/loot/advancements parse.

If server requires EULA/setup that would complicate task:
do not waste large time.
Record exact limitation.

---

# 47. DATAGEN

Use datagen where it clearly reduces invalid repetitive JSON:
- tags;
- loot;
- recipes;
- biome modifiers;
- blockstates/models where already suitable.

But migration goal is parity, not datagen rewrite.

Do not convert every hand-written JSON just for architecture purity.

---

# 48. CLIENT RENDER VALIDATION WITHOUT RUNCLIENT

Because client cannot be auto-launched:
- compile client source;
- validate model/resource references;
- verify model layer registrations programmatically where possible;
- keep manual QA list.

Do not claim visual runtime success purely from compilation.

---

# 49. MANUAL QA DOCUMENT

Create:
`docs/MANUAL_QA_0.9.4_PORT_1.21.1.md`

Organize by:

A. Boot/basic.
B. Items/blocks.
C. Curios.
D. JEI.
E. Hunt mobs.
F. World bosses.
G. Kurgans.
H. Bandits/structures.
I. Swamp/water/fishing.
J. Broom/Mortar.
K. Baba Yaga/Hut/UI.
L. Commands.
M. Furniture/known bugs.
N. Dedicated server/multiplayer.

Do not hide known old bugs.
Mark:
- `EXPECTED LEGACY BUG`
- `PORT REGRESSION`
- `PASS`

This distinction is critical.

---

# 50. POST-PORT BUGS — DO NOT FIX NOW

The following are KNOWN future tasks, not 0.9.4 scope:

- Yaga planned anchor may show `Y=0` / placed=false and lead to empty candidate location;
- Yaga hut placement conditions need redesign;
- structures too rare/far;
- kurgans need much more loot;
- kurgan labyrinth is too linear/downward;
- kurgan blocks need redesign + slabs/stairs/walls;
- minimum source texture target for redesigned/new items/blocks later = 256×256;
- bandit structures need palette + interiors + loot;
- swamp needs visual/worldgen rethink;
- old mobs need stronger AI, sounds, animations and balance;
- furniture sitting crashes/unfinished;
- waystones/path stones unclear;
- creative usage should not require XP;
- custom structure locate commands should move toward vanilla `/locate`;
- achievement UI/text needs cleanup;
- custom HP HUD should later be replaced/integrated with established mods;
- planned enchantments/runes missing;
- vanilla items compatibility with mod enchantments needs later rework;
- item grouping/creative inventory needs reorganization;
- TreeCapitator/FallingTree integration comes later;
- item/block art overhaul comes later.

If one of these literally prevents the 1.21.1 project from starting:
apply minimal compatibility fix only and document it as `PORT BLOCKER FIX`.

---

# 51. NO SILENT CONTENT LOSS

Before deleting any old Java/resource file because “API obsolete”:
find its modern replacement or document why content is intentionally unsupported.

Create:
`docs/port/REMOVED_OR_DEFERRED_CONTENT.md`

At successful completion this file should ideally say:
`No intentional content removals during 0.9.4 migration.`

If not:
list exact registry ID + reason.

---

# 52. COMPILE-STUB POLICY

Temporary stubs are allowed ONLY during intermediate gates.

By final build:
- no placeholder item replacing real system;
- no entity registered with no AI merely to compile;
- no empty menu replacing UI;
- no disabled worldgen just commented out;
- no `TODO port later` for existing 0.9.3 functionality.

If a system cannot be ported:
report incomplete; do not lie.

---

# 53. LOGGING

Migration logs should be useful, not spam.

Add only:
- initialization summary in dev/debug;
- critical load errors;
- explicit migration diagnostics.

Do not log every entity tick.

---

# 54. README

After successful migration:
top section becomes:

`0.9.4 — NeoForge 1.21.1 migration`

State clearly:
- Minecraft 1.21.1;
- NeoForge;
- Java 21;
- port goal;
- current dependencies;
- build command;
- no automatic client QA.

Keep historical README sections beneath, unless user project convention says otherwise.

Do not rewrite historical feature descriptions unnecessarily.

---

# 55. BUILD COMMANDS

Use actual Gradle wrapper generated/required by target MDK.

Final expected flow conceptually:
- clean;
- compile/build;
- custom verify tasks;
- resource verifier.

Do not assume exact old command survives if Gradle task names differ.

Document actual commands in README and `PORT_STATUS`.

---

# 56. POLYMC TEST INSTANCE

If known local test instance path is discoverable from project/docs:
prepare/update a dedicated 1.21.1 NeoForge test instance only.

Do NOT overwrite a user's 1.16.5 instance silently.

Recommended:
new instance name:
`Slavic-Myths-1.21.1-Testing`

If Codex cannot automate PolyMC creation safely:
do not guess.
Provide exact JAR path + required dependencies.

---

# 57. REQUIRED RUNTIME DEPENDENCIES FOR TESTING

At minimum account for:
- NeoForge 1.21.1;
- Curios NeoForge 1.21.1 if required by current mod;
- JEI NeoForge 1.21.1 optional integration/test companion.

Do NOT add:
- Jade;
- Patchouli;
- AppleSkin;
- FallingTree;
- GeckoLib;
unless already required by current source.

Those decisions are future cleanup versions.

---

# 58. PORT ACCEPTANCE GATES

GATE 1:
- Java 21/NeoForge build shell succeeds.

GATE 2:
- all registry IDs compile/register;
- resources parse.

GATE 3:
- stateful items/accessories/recipes/loot/advancements ported.

GATE 4:
- all existing entities compile/register/render source compiles;
- attributes and AI ported.

GATE 5:
- networking/menus/screens/commands compile and use modern API.

GATE 6:
- SavedData/world anchors/worldgen systems ported.

GATE 7:
- Curios/JEI integrations compile and are optional/required correctly.

GATE 8:
- clean build;
- resource verification;
- headless tests;
- dedicated server smoke if feasible;
- no old Forge dependency in production graph;
- no intentional content loss.

---

# 59. DEFINITION OF DONE

The port is DONE only if:

- [ ] target is Minecraft 1.21.1;
- [ ] loader is NeoForge;
- [ ] Java 21;
- [ ] version is 0.9.4;
- [ ] mod ID remains `slavicmyths`;
- [ ] ForgeGradle 5 / Forge 36.2.42 removed;
- [ ] neoforge.mods.toml used;
- [ ] correct 1.21 resource/data folders;
- [ ] public registry IDs inventoried and preserved;
- [ ] blocks/items ported;
- [ ] stateful ItemStacks use modern component/state approach;
- [ ] Curios ported;
- [ ] JEI integration ported;
- [ ] entities ported;
- [ ] entity attributes/AI ported;
- [ ] render/client source compiles;
- [ ] mob effects ported;
- [ ] networking uses modern NeoForge payloads;
- [ ] menus/screens ported;
- [ ] SavedData/player state ported;
- [ ] hunt systems ported;
- [ ] world bosses ported;
- [ ] kurgans ported;
- [ ] water/swamp/fishing ported;
- [ ] bandit structures/content ported;
- [ ] Yaga 0.9.3 systems ported;
- [ ] Broom/Mortar existing systems ported;
- [ ] recipes/loot/advancements parse;
- [ ] old verify tasks migrated or replaced;
- [ ] no `runClient` executed;
- [ ] clean production JAR built;
- [ ] manual QA document created;
- [ ] known old bugs remain listed separately instead of hidden;
- [ ] no deliberate content redesign performed.

---

# 60. FINAL CODEX RESPONSE FORMAT

Keep final answer compact.

Return:

## Платформа
Minecraft / NeoForge / Java / mod version.

## Перенесено
Subsystem checklist with actual status.

## Registry parity
Count old IDs, count preserved, list any changed/removed IDs.

## Dependencies
Actual NeoForge/Curios/JEI versions used.

## Проверки
Exact build/headless/server commands and results.

## JAR
Exact output path.

## PolyMC
Actual status/path.

## Известные legacy-баги
Only existing pre-port bugs, not new port regressions.

## Port regressions / не выполнено
Anything truly incomplete.

DO NOT say “полностью перенесено” unless all gates actually pass.
