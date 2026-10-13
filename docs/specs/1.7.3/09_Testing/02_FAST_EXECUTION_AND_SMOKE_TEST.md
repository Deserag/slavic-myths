# Do it fast — target only necessary files, smoke-test and ship

1. Read 00_START_HERE/*. (under 10 minutes of reading is NOT a reason for scanning full project). Grep for `Sivka`, `Burka`, `summon`, `hero`, `boss bar`, `ModItems`, `ModEntities`, `sounds.json` / known IDs; stop at first authoritative patterns. **No recursive model design analysis unrelated to v1.7.3.**
2. Pre-register item ids and entities; run compile early to surface registry/API mismatch. Establish renderers with supplied reference and simple test instance.
3. Add Alyosha combat state & Skim states, one deterministic encounter anchor. Prefer direct finite-state timers over 20 generalized AI frameworks.
4. Add smallest compatibility changes for old summon items + creative eggs. Verify right-click events exactly once server side.
5. Audio map from manifest -> `sounds.json`, 2 languages, 2–3 short test invocations.
6. Use debug creative world and perform ~10 crucial checks: hero call no consume, duplicate prevention, boss egg, horn at marker, taunt and dodge, stamina, diamond 6HP, den unlock, saved victory, dimension/relog.
7. Build jar, report exact path, fail if build/critical checks incomplete. Avoid doing broad code cleanup or optimization of unrelated systems.

Suggested search query bounds: 10–15 key files initially. If no known pattern exists, search **the relevant package only**; do not run general source inventory. A full build will naturally compile entire repo; ban concerns *analysis and unrelated edits*, not compilation.

## Reproduction commands — adapt actual source identifiers
- `./gradlew build` (Windows `gradlew.bat build`), target MC/loader whatever repo specifies. No forced refresh dependencies if not required.
- `/give @s slavicmyths:alyosha_call_whistle`
- `/give @s slavicmyths:beast_challenge_horn`
- `/give @s slavicmyths:skim_beast_spawn_egg`
- `/give @s slavicmyths:idolishche_poganoe_spawn_egg`
- `/give @s slavicmyths:marinka_spawn_egg`
- `/summon slavicmyths:skim_beast`
- Optional existing dev command place valley; only if implemented safely.

The above identifiers are a requested naming contract, not an assertion these commands already exist. Your job is to make them valid. If engine/source conventions force renaming, document exact final commands without breaking existing names.
