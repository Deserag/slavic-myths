# Kuzya 1.6.8 — autonomy and Russian name

2026-10-11. User confirmed original cartoon speech and movement, requested autonomous household life and reported corrupted name text.

## Implemented

- Fixed the actual ru_ru entity-name key, SFX subtitles and repair messages. Root cause: a previous rewrite read the UTF-8 resource generator through Windows CP1251 and saved the mojibake. The five film dialogue subtitles used an explicit UTF-8 manifest and were already correct. The old 1.6.7 assertion covered those lines but missed the entity name; this release checks the exact name and encoding of every owned Russian key.
- First activity due in 20–60 ticks, checked once per second. Rest pauses 60–120 ticks. Player interaction is not required. Four fixed safe household spots vary inspection, sweeping and carrying/placing a bowl; existing table eating and physical stove rest/sleep remain.
- Saved schema 2 retains scene, age, queue, bounded route, waypoint, meal/rest state, last work spot and remaining timers. Loading resumes the physical route from the entity's saved position. Elapsed world game time ages sleep/idle cooldowns; unloaded motion is not simulated. Legacy schema 1 is immediately eligible for work, or resumes lying if saved atop the stove.
- Switching routes clears the previous scene queue. Unknown saved clips fall back to idle. Historical sound markers are skipped when restoring.
- All world interaction stays local: four anchors checked when choosing a chore; path recreation at most once per 40 route ticks; guest handling once per 20 ticks. No structure search, global world/player polling or forced chunk loading. Actual MSPT has not been measured.
- Art, five original film audio files, hut interior and registry IDs preserved. No automatic commit.

## Verification

`gradlew.bat clean build` with isolated offline fixtures: PASS. 471397 Kuzya native model checks and 73926 doghead regression assertions. Production resource/JAR verifier: 28007 checks PASS. 6452 old production entries byte-identical; unrelated localization keys preserved. Java 21, common/client isolation and absence of smoke/GameTest classes in production checked.

Four actual dedicated GameTest server launches, zero clients. First failed after scripted stove travel started from an already active stove scene; fixture corrected. Second exposed the stale animation queue when replacing a chore with a stove route. Queue cleared. Third and fourth passed. Final 2300-tick scenario observed autonomous work, actual stove ascent/rest/descent, autonomous sweeping at two distinct positions, canonical uniqueness, protection/loot, route/waypoint and sleep NBT roundtrips, elapsed unloaded rest and safe legacy/invalid-clip restoration. NBT roundtrip is not a real player relog. Accelerated test duration is not an MSPT measurement.

Resource regeneration: PASS, 5319 files, zero changes. JAR SHA-256: 0462212c2b18df1e75afa5f37ad60bd6dce5242dfe7f3a31d4af20d7a707aa2b.

Installed after gates in the existing Minecraft 1.21.1 / NeoForge 21.1.255 test profile. Backup 1.6.7 outside mods, 486 other profile files preserved. Old 1.16.5 profile and user worlds untouched. Receipt: polymc-installation.json.

## Manual acceptance still required

1. Confirm Russian overhead name/tooltip on the existing resident.
2. Enter the hut, watch household activity and stove/table poses; leave and return after chunks unload.
3. Check real player relog, multiplayer and client visual/audio mix. No client launched, no human audio listening or measured MSPT claimed.
