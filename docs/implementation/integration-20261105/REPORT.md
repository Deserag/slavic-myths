# Integration 2026-11-05 — technical result, owner gameplay acceptance pending

Working branch: `codex/416-posad-fixes-20261105`. Original base `c56c12e2`; preserved #416 checkpoint `0878c42a`. Only this working branch is published. No Minecraft launch during this task.

## Sources
- `fix/2-4-mytar-bauk` @ `9873dd67`: existing imported model/behavior changes retained; merged source ancestry (`b0addff4`).
- `2.4-agneya-ledeya` @ `3ad598fa`: existing imported Agneya/Ledeya code, enlarged bow and assets retained; ancestry merged (`29dd08bd`).
- `2.5-viy` @ `b7105af7`: cumulative Lomeya/Neveya implementation merged (`914f5712`). Viy itself is NOT implemented.
- `2.5-lomeya` @ `ab7a6202`: unique history commit only sets visible Russian name; identical change already present in cumulative source. Attempted cherry-pick was empty and skipped, no duplicate commit.
- `2.5-neveya` @ `b7105af7`: same tip as cumulative source; alternate history `1c0b5dc8` effect registration is already present. No duplicate merge needed.

## Preservation / conflicts
Backup: sibling `work/integration-backup-20261105-150326`, tracked HEAD binary patch, index patch, status and 87 untracked files; local backup ref `codex/backup-416-20261105-150326`. Checkpoint saves all 214 changed/new files before merges. No deleted files relative to checkpoint. Original master unfinished merge and main untouched. No force/reset/clean.
Language text conflicts resolved by three-way JSON key union; no conflicting values required choosing a side, all checkpoint key/value pairs retained. ClientSetup conflict resolved retaining Ledeya/FrostBolt AND Lomeya/Neveya/DeathBolt renderers. SistersContent conflict resolved retaining all entity registrations and attribute bindings. ModEffects gains Fracture/Death Mark; global effect bus registration occurs once. Existing entities/effects remain. Boss Java text equals cumulative source (line-ending normalization). Added explicit Creative catalog rows for both new eggs; existing spawn-egg fallback also remains.

## Verification
PASS Java 21.0.12.1 `clean build compileJava processResources` (29 seconds). Subsequent build after Creative catalog edit also checked. `test NO-SOURCE` is not a gameplay test.
PASS scoped `tools/verify_posad_resources.py`: 20 items, 63 referenced models, Java21 production JAR, no QA classes. Boss textures, egg JSONs, Russian labels, renderer and attribute registrations inspected. Source confirms Lomeya 2000 HP/two phases/three-stage Fracture; Neveya 2400 HP/two phases/air-ground actions/withering/self-heal/projectile/verdict, strict death threshold `< 4.0F`. These are source checks, NOT observed combat.
BLOCKED global `verify_current_resources.py`: preexisting kitchen malformed spicy_seasoning JSON, with further previously identified unregistered crops/dishes; owner explicitly deferred kitchen. Do not fix these as integration scope.
NOT_RUN Minecraft, combat, animation/visual comparison, multiplayer, actual egg use and save/reload after integration. No PolyMC update or profile launch in this task, as explicitly prohibited. Existing JAR version remains `2.1.6.2-TEST`; do not mistake version string for new release acceptance.

## Owner runtime checklist
Check all four source boss families, enlarged Ledeya bow, both new eggs in Creatures tab, HP and phase transitions, physical left-hand Fracture III, Neveya flight/scythe/heal/verdict warning circles and marked player boundary at 4 vs below4 HP. Verify existing Posad/NPC/ritual systems and dedicated server/MP/save compatibility. No Viy entity is claimed.

Resume #416 using [HANDOFF.md](HANDOFF.md). Issues remain open; no unfinished PR merged.
