# Чеклист файлов/ассетов (не список существующих классов, а список того, что должно быть)

Сверить фактические папки `src/main/...` и registration patterns, не создавать буквальные несуществующие package names. Необходимое для готовности:

- [ ] Boss entity + server AI/phase/trial/session + personal reward ledger + yield 5%.
- [ ] Boss 3D model, UV texture, render layers (glowing eyes/runic particles), animations idle/walk/cast/attack/yield.
- [ ] Raven entity flying AI, own voxel model with 3 views, textured animation rig, damage logic and bounds; allied and hostile faction modes.
- [ ] Waning server status & tiered offhand logic + decay + player/bogatyr variants + UI; no permanent loss of offhand item/totem.
- [ ] Fear server scale + client-only phantom rendering and hit response; local-night player-specific VFX and cleanup.
- [ ] Boss attack implementations including Hunger, Frost, Death, Last Hour, Dark Frost, Fear, Ravens, Scythe melee.
- [ ] Real item summon path via existing Baba Yaga / Bayun, no unexpected dimension/structure requirements.
- [ ] Scythe item 3D model/inventory icon, **custom 1st- and 3rd-person animations**, hit arc & CD, life steal, undead bonuses, 2 actives.
- [ ] Rune single `rune_of_waning` registered & insertable in current rune system, icon and tooltip.
- [ ] Amulet `black_wing` registered & usable in real accessory slot, spawns 2 allied ravens for finite duration and CD.
- [ ] Last Night shard item and persistent guarantee, not fake recipe for Nether in 1.9.2.
- [ ] Registered sound events + actual sound assets + `sounds.json`, each audible once per sensible action.
- [ ] `ru_ru` and `en_us` entries, achievement/Book of Tales as applicable, menu glyphs, controllable keymaps.
- [ ] Resource path validation: PNG loaded, entity render not invisible, item in hand not missing model, no `Missing texture`.
- [ ] Build/test pass, report no-game QA path, honest unresolved blockers.
