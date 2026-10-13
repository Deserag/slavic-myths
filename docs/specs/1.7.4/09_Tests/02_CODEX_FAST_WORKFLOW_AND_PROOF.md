# Work quickly, verify narrowly, ship actual JAR

Stage 1 (~light targeted reading, no audit): Confirm current MC/NeoForge version, one registry wiring entry, hero-summon item (Sivka/Burka), boss handling and persistent data interface, sound resource style, renderer/animation approach. Record precise API names in code comments/test README only if necessary. Stop exploration.
Stage 2: Implement server logic, registers, items and encounter ownership. Get gameplay compile before decorative visuals. Keep methods single responsibility, minimal touch existing code.
Stage 3: Implement exact player-approved designs, falcon model, animations and item sprites; directly copy PNG/OGG assets from package to mod resource paths. No extra new concept search.
Stage 4: Add localized strings + sound mappings; quick dedicated server validation and client item render if tool supports.
Stage 5: Run scoped automated compile/test (`./gradlew build` or repo wrapper on local platform). One JAR. Do not run broad expensive custom integration passes unrelated to changed components; fail transparently when automated game playtesting unavailable. Present concise outcome including actual paths & failed tests, not a new architecture plan.

## Required completion note (one page)
- Exact resource/class files created/changed with purpose; final `modid` and MC/loader version observed.
- The exact 1.7.4 visual references used and whether body models + companion / Kartaus appearances match.
- Gameplay entry points with command usage / creative tab.
- Actually run tests in `01_DETAILED_ACCEPTANCE_CASES` (short PASS/FAIL/NOT RUN table), plus observed post-armor severe attack values when testable.
- Actual JAR absolute path, location copied to verified mods folder (if any).
- Explicit leftover limitations; no false claims about original voice audio. If audio is generated effect only, state so.

No indefinite iteration. Prioritize a working version with accurate scope over extending into 1.7.5+. Do not request a separate design phase; all decisions are already in this package.
