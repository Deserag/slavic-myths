# SLAVIC MYTHS — CODEX EXECUTION ORDER — FINAL 1.6 PATCH

YOU ARE TO IMPLEMENT THIS PATCH IN THE EXISTING SOURCE REPOSITORY. This archive is an executable specification, not a brainstorming outline. Do not deliver only a plan, mock-up, or new prompt. Work in the actual user's checkout and leave a buildable production artifact.

## Scope — IMPLEMENT IN THIS PATCH
1. Add Kot Bayun as a persistent, invulnerable, highly detailed mythical cat entity with its encounter, physical gifts, sleep-song compulsion, fully animated kill, personal trust and working non-placeholder interaction screens.
2. Repair/rebuild the existing Book of Tales (Книга сказаний) as a functioning, once-granted, expandable in-game guide with real content extracted from every existing Slavic Myths subsystem. Provide data-backed rumor/observed/verified stages, readable and usable GUI, real discoveries, optional approximate search regions for actually known targets and built-in achievements view.
3. Audit, repair and organize ALL CURRENT Slavic Myths advancements, preserve their already earned progress and existing registry IDs, create meaningful tree connections and working translations; integrate them into book without forging progress.
4. Preserve existing Baba Yaga behavior, quests, rewards, existing relations and assets. Make only NON-DESTRUCTIVE compatibility adjustments that the book needs to display her existing character and optional existing quest status. Do not rewrite Baba Yaga or combine her questline with Bayun in this patch.
5. Ship all actual textures, models, clips, OGG sound assets, RU/EN locale entries, tests, guide entries and production JAR as described in these files.

## Explicit OUT OF SCOPE
- No joint Bayun/Baba-Yaga quest graph; no shared retroactive quest completion/reward logic in this patch. Follow-up specification is at `11_NEXT_MINIPATCH/BOUNDARY.md` ONLY for design boundaries.
- No bogatyrs, new boss roster, hell dimension, new RPG reputation across the entire world, world-spanning faction dynamics, universal location scanner or unrelated remaster.
- NO empty place-holder menu choices such as “Coming soon” presented as a working feature. Disable nonexistent actions; create usable content instead.

## Execution in strict order
A. Read actual `AGENTS.md`, `docs/PROJECT_STATUS.md`, `docs/ROADMAP.md`, `docs/ARCHITECTURE.md`, and git status. Examine existing Book item, Book GUI, all SlavicMyths entity registrations, Yaga and quest implementation, worldgen, music/sound and advances, existing tests. Write a concise `docs/verification/bayun-book-final/AUDIT.md` of REAL IDs and integration points; do not extrapolate from possibly stale public repo. Reuse rather than duplicate. Check release version in active files; the colloquial label “final 1.6” does not supersede user’s current actual version.
B. Implement persistent book grant/data migration, catalog backend, click navigation, discovery triggers and RU display; then ensure category coverage via a generated/verified manifest.
C. Implement Bayun entity, safe and performant world placement, geometry/UV, material and pixel art, animation rig and clips, voice/foley, encounter, per-player trust and menu; integrate with Book.
D. Fix and connect advancements and book UI; retain existing advancement keys and rewards and historical completions.
E. Add only compatibility bridges for Baba Yaga, build automated tests, compile, validate packaged resources, attempt authorized runtime tests. Report precisely what has been verified.

## Non-negotiable Russian-language gate — PRIOR INCIDENT
English is permitted **only for instructions to Codex**. In the actual game **Russian is primary**. All player-facing text MUST be correct, natural Russian in UTF-8, complete `ru_ru.json`: titles, category headers, hover tips, tooltips, subtitles, boss rumors, quest labels, dialogs, timeouts, log/chat messages, advancement titles/descriptions and death message. Keep `en_us.json` present and complete but never replace Russian with English, mojibake or raw keys. Do not encode Cyrillic as question marks and do not break pre-existing Russian assets. Explicitly diff existing vs resulting `ru_ru.json`; run JSON, Unicode and cross-locale key checks. Open the actual GUI with Russian language setting if runtime is available; screenshot and inspect. **If Russian quality or rendering is not verified, do NOT mark release complete.** See `08_LORE_AND_LOCALIZATION/RU_TEXTS.md` and `06_ENGINEERING/LOCALIZATION_AND_MIGRATION.md`.

## Mandatory resource fidelity
The PNG files in `09_ASSETS/references/` are approved VISUAL REFERENCES, not screen-ready sprites. Build functional art matching silhouette, composition and colors. DO NOT merely include the references in a mod JAR and pretend models or textures exist. Cat model parts need independent movable geometry; animation timelines are defined in `09_ASSETS/animation/*.csv` and `03_BAYUN/ANIMATION_SPEC.md`; satisfy world pixel/voxel density and use natural feline joints. Audio in `09_ASSETS/audio/` is ORIGINAL READY-TO-DECODE OGG and must be integrated, not merely listed. Some are synthesized prototypes; integrate thoughtfully and do not mislabel synthetic prototypes as field recordings. If there is a real quality gap, explicitly disclose; improve audio yourself rather than leaving silent calls.

## Repository rules
- Minecraft 1.21.1 / NeoForge / Java 21 if corroborated by active checkout; preserve mod id `slavicmyths` and all existing registry IDs. `AGENTS.md` supersedes guessed API names; no code relying on unverified 1.16 Forge patterns.
- Reuse actual player persistent data handling, capabilities/attachments, sync and interactions. Server is authoritative for granting, trust, gift consumption, flags, advancement criteria, hypnotic movement and kill. No client-side gift duplication or uncontrolled client packets.
- Strict optimization: no chunk forcing, no global per-tick locate scan, no linear scan of every player on every world tick. Nearby player list once every few ticks, cached target/anchor, cooldown and bounded work; do not place a Bayun in unsuitable terrain. No duplicate generation.
- No automatic git commit or pushing. Do not damage current user files or worlds. Build using existing gradle wrapper and project's JDK, verify final jar in `build/libs`; if the project's process requires copying jar into a specific existing PolyMC **1.21.1** test profile, copy only after passing all gates; never modify old 1.16.5 profiles.
- If something cannot be completed, state exact unresolved blocker, affected files, skipped test and why; do not claim a mock-up is implementation. Never fill gaps by inventing APIs.

## File reading order
`00_START_HERE/SCOPE_AND_DECISIONS_RU.md` → `01_GAMEPLAY/PLAYER_FLOWS.md` → `02_BOOK/*` → `03_BAYUN/*` → `04_BABA_YAGA/*` → `05_ADVANCEMENTS/*` → `06_ENGINEERING/*` → `07_QA/*` → `08_LORE_AND_LOCALIZATION/*` → audio/animation manifests → references. Every file is normative; example artwork text is NOT authoritative when it conflicts with requirements.

## Definition of DONE
Every acceptance criterion in `07_QA/TEST_MATRIX.md` passes or is clearly called out as blocked; 100% of current entity ids (not guessed enumeration) appears in content coverage; book grant survives quit/rejoin/death/loss without duplication; Bayun physically executes correct warning/song/close-range strike with robust cancel paths; sounds and animations run; RU localization is valid; Yaga old quests continue; advancement historical progress preserved; production JAR builds, contains resource files and is placed only in the correct test setup as authorized. Report build command/result, test counts, screenshots, asset inventory, registry ID diff and exact JAR path.
