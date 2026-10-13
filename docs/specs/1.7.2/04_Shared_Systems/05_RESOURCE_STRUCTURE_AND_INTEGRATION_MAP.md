# Integration map (proposed, NOT assumed existing APIs)

## Discovery only for touched features
Search for current mod ID, chosen runtime (actual Minecraft/NeoForge/etc.), registry classes, ally capability, Ilya's exact summon/retreat/party lock implementation, entity rendering/animation conventions, existing boss controller, custom player progress and structure template loader. Read those files and their directly used dependencies. DO NOT scan whole source recursively looking for potential improvements.

## Suggested resource ID namespace/path pattern, adapt to current repo
- Entities: `dobrynya_nikitich`, `marinka`, `marinka_shadow`, `witch_aurochs`, `marinka_hex_focus`, `marinka_tur_minion` as necessary.
- Geo/model files: `geo/dobrynya_nikitich.geo.json`, `geo/marinka.geo.json`, `geo/magpie.geo.json`, `geo/golden_aurochs.geo.json` if geo animation library exists; otherwise use current runtime's JSON/model APIs.
- Texture files: `textures/entity/dobrynya_nikitich.png`, `textures/entity/dobrynya_spear.png`, `textures/entity/dobrynya_shield.png`, `textures/entity/marinka.png`, `textures/entity/marinka_magpie.png`, `textures/entity/dobrynya_aurochs.png`, `textures/particle/marinka_magic.png`.
- Animations: appropriate `animations/*.animation.json` or current animation controller specs; exact clips D01..D18 and M01..M19 all mapped to actual animations.
- Sounds: `assets/<modid>/sounds/dobrynya/*.ogg` and `.../marinka/*.ogg`, `.../arena/*.ogg` plus sound registry JSON mapping names in relevant character docs.
- Structure: `data/<modid>/structure/witch_yard` as template or current placement generation; underground integrated in template or separate placed module with atomic flags.
- Quest/progress: reuse current persistent story flag/capability APIs; do not create a parallel unsynced `Map<UUID,Boolean>` without save code.
- Loot: optional basic vanilla chest loot through standard loot tables, no unique reward item or trophy; v1.7.6 future flag only.

## Build/deployment
Compile with repo wrapper and its Java target, retain loader+dependencies, avoid unsupported imports or guessed signatures. After successful build, identify one actual built mod JAR and state its path. If repository already specifies PolyMC distribution copy location, comply; otherwise do not invent a specific Windows PC mods path. Change log must be honest about any incomplete assets or tests.

## Resource loader QA
Resource paths lower_snake_case ASCII, names case-sensitive, one ogg name -> sound event entry, correct namespace, include dedicated server-safe registries. Avoid using image concept infographic directly as a runtime texture atlas. It is a human art reference; new real game texture maps and model assets must be authored. Verify UVs, model visibility in mod inventory and in-world, hitbox collision, correct full-height spear/rig and head stubble.
