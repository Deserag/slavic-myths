# Definition of done, output obligations and work order

## Work order
1. Read package all `.md` / included image files; index only directly relevant repository hooks.
2. Implement registrations, assets, models, renderers, reliable state sync, spawn/structure and summon integration.
3. Implement both combat state machines and all defined keyframes/events, not placeholders or unbound method names.
4. Hook sounds/subtitles and install actual OGG files; voice placeholders may remain optional only if they degrade cleanly into subtitle + nonverbal sound.
5. Add structure, gameplay/progression integration, localization and complete automated/manual checks.
6. Build and verify the produced actual `.jar` and test in the configured dev runtime.

## Acceptance gates
- [ ] Dobrynya 2.4 blocks, 230 HP, distinct wide shoulder silhouette + gray steel dominant palette; short white STUBBLE is visible on the face, no full beard. White hair is clearly visible from front/back; spear is longer than him, 3.6 blocks, held correctly without arm clipping; shield renders on other arm.
- [ ] Dobrynya has parry, three-hit spear sequence, thrust, rotating sweep, anti-magic shield, break ward, resolve resistance and 15% retreat/elixir recovery, each with event-bound animation, cooldown and client/host synchronization.
- [ ] Marinka 2.1 blocks, 600 HP, unique witch outfit and palette; flight/blink/vfx are legible; she has 1 real + 2 identical false bodies per shadow event, false ones die after 24 actual HP; no HUD/particle/AI/sound distinguishability until tested/defeated.
- [ ] Marinka barrier is finite (30% damage reduction), breaks via Dobrynya counter (8s vulnerability), magpie and blink have telegraphs and arrival space checking, cursed trace is avoidable, tour transformation is reversible through a ritual anchor.
- [ ] Yard is 60x60 target footprint, uncluttered 40–45-block playable center, meaningful prop spread, ruined house and basement with 3 functional subareas. Robust against terrain and chunk load edge cases.
- [ ] Localized subtitles and cues for spells, no invisible hit zones; server authority; no stuck entity/clone/infinite loop; fight clears on failure and doesn't duplicate.
- [ ] Boss melee/spells tuned against player 24 HP + full diamond armor; strong direct hit at least 6 actual HP before bonus defense and after armor; avoid double-hit ticks and unavoidable one-shots.
- [ ] Victory recorded once per relevant player and for world encounter; consumed/claimed ordinary chest loot cannot be cloned by relog; future v1.7.6 unique-reward eligibility recorded, no unique reward dispensed.
- [ ] No new summon cap restrictions outside story encounter; no unauthorized cinematic assets, no unrelated code rewrites, current target loader/build unchanged.
- [ ] Gradle build succeeds; dedicated server classloading and particle/resource checks pass; user receives real deployable JAR path and exact version identifier.

## Final delivery evidence
Produce `IMPLEMENTATION_REPORT_1.7.2.md` with: files changed and purpose, action/state tests pass/fail, structure/seed reproducibility, assets accounted, build path, warning list; include screenshots from runtime or tell honestly if not captured. If accepted reference is materially impossible, explain precisely why and do not call it fully done.
