# Winter Owl / Зимний филин — implementation WIP
Owner selected Frozen Watcher reference (grey-brown plumage, amber eyes, wooden frosted antlers and cyan runes). Existing integration branch only.
- Optional high-tier boss, 2000 HP, phase two at 1000 HP; no rewards, natural spawn or structures authorized. Test spawn egg: slavicmyths:winter_owl_spawn_egg.
- Ground recovery 140 ticks; low flight 100 ticks; collision-checked landing candidates. Timings, damage and armor are provisional pending playtest.
- 13 attacks: frost trail, falling icicles, dive, feathers, gust, runes, scream, grab, blizzard, summoned spirits, tornado, multi-dive, rune awakening.
- Dark blue belly signals frost trail using existing Auka frost and FrostCold; cyan signals falling FrostHazard. No permanent block edits.
- Synced action and frame drive articulated cuboid wings, tips, head, beak, feet and tail. Procedural indexed textures and emissive palette masks. Visual acceptance is pending actual client inspection.
- Build and native GameTests must pass; registration, health, phase, save/load, belly states and attack catalog tests are not substitutes for a full combat playtest.
