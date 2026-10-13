# START HERE — Slavic Myths 1.7.2 complete implementation specification

## What this archive IS
This is a ZIP for Codex: precise **implementation instructions**, current character reference PNGs, supplementary technical diagrams, original playable OGG sound effects, original voiceline script, structure layout and executable QA checklist. **Do not respond with a second theoretical plan.** You must implement and build changes in the existing repository.

## Folders and read order
1. `00_START_HERE/00_CODEX_ENTRY_PROMPT_EN.txt` — paste to Codex verbatim.
2. `00_START_HERE/01_SCOPE_RULES_AND_PROHIBITIONS.md` — strict don't-scan-whole-project, no scope creep, no early unique drops.
3. `00_START_HERE/02_DEFINITION_OF_DONE.md` and `03_VALUES_AND_DECISIONS.md` — checkable goals and numeric locks.
4. `01_Dobrynya/01..04` — model, rig/keyframe animations, all combat mechanics, sounds.
5. `02_Marinka/01..04` — model, animations, state machine/clone/barrier spells, sound.
6. `03_Arena/01..03` — full yard grid and physical layout, underground rooms and victory opening, event staging.
7. `04_Shared_Systems/01..05` — quest summon locks, true damage, persistence/server rules, HUD, integration.
8. `05_Visual_References` — 3 prior concept sheets plus detailed schematics: use approved newest Dobrynya design only.
9. `06_Audio` — original 39 Vorbis OGG, PCM-encoded sound-event prototypes, manifest.
10. `07_QA`, `09_Localization`, `08_Legal_and_Resources` — thorough test criteria, strings, rights/provenance.

## Fixed designs
**Dobrynya**: 230HP, 2.4 blocks, BROADER silhouette, predominantly gray/silver steel, WHITE long hair, SHORT pale stubble (NO beard), shield, 3.6-block SPEAR (NO sword). Agile anti-magic parry fighter, retreat at 15% HP.

**Marinka**: 600HP, 2.1 blocks, witch, barrier 30%, mobility, actual three identical forms (one original + two 24HP false), transforms Dobrynya into friendly golden-horned aurochs, rescuable by breaking spell anchor. Her spells are dangerous in full diamond with 12 player hearts, but all have fair telegraphs.

**Yard**: ~60x60 ruined witch homestead, 40–45b open center, detailed manor/well/barn/fence, ritual core; victory reveals hidden three-zone cellar with modest *vanilla* loot. **NO unique rewards until 1.7.6**; progress is saved immediately.

## Numeric nuance
Some timing/cooldown/texture coordinates are professional implementation baselines that were not individually voted on by the user; use them as defaults and test. The accepted facts — identity, visuals, 600HP witch, 3 versions, witch yard, story limits and delayed rewards — are NOT optional.

## Important last-minute conflict handling
- The final user-approved Dobrynya image is ONLY the one explicitly named `APPROVED...STUBBLE...png`. All prior full-beard versions are superseded and intentionally NOT included.
- In a few concept panels, a top-view or weapon silhouette may be illustrative, not an exact geometric blueprint. Follow explicit rig and numeric specs and supplemented technical art.
- Story battle lock is local and narrative, does not permanently nerf normal 2-bogatyr player ability.
- Audio contains no voiced cartoon clips, even if a character mood was inspired by a film.
