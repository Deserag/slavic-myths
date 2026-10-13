# SLAVIC MYTHS v1.7.2 — NON-NEGOTIABLE IMPLEMENTATION CONTRACT

## Status and authority
This package is the implementation specification. It is not a loose creative prompt or permission for the coding agent to improvise aesthetics, skill selection, plot, rewards or project refactoring. Authoritative sources, in descending order: (1) this contract and explicit character/system numeric specifications; (2) final approved character images; (3) technical orthographic/plan sheets; (4) earlier concept illustration panels as visual mood only. Where a concept image includes a wrong scale, clipping, inconsistent top view or mislabeled animation, use the written numbers and technical diagrams. A visual sheet is a depiction, not a turn-key model file.

## REQUIRED functional scope
- Dobrynya Nikitich as a summonable ally with unique gray/silver armor, white hair, SHORT FACIAL STUBBLE, broad silhouette, shield and 3.6-block spear; role is parrying, precision and anti-magic, not Ilya's permanent taunt/tank system.
- Marinka as a mobile 600 HP caster and boss, real clone challenge, magic barrier, curses, fire, magpie escape, blink, golden-horned aurochs curse for Dobrynya and fair reversal route.
- Rare deterministic ruin/quest structure placement through existing architecture; if a story/structure trigger already exists, integrate it. No uncontrolled natural world generation, no dimension migration and no teleport inside ungenerated terrain.
- Exterior witch yard roughly 60x60 with 40–45-block effective navigable center; visibly ruined mansion, dry well, outbuildings, carts, dead trees, runes, witch props; accessible below-ground ritual room unlocked only on boss defeat.
- Complete audiovisual events, skills/state machines, localized subtitles/tooltips, victory persistence, recovery paths, tests and downloadable usable resources.

## RESTRICTIONS FOR REPOSITORY WORK
- DO NOT recursively list/read every file, run global static analysis on unrelated code, reconstruct architecture from scratch, rewrite registries outside affected features, change build system, update unrelated libraries, modify other bosses, introduce novel accessory systems, regress 1.7.1, or rebuild 1.7.1 from this document.
- Allowed initial reads: settings.gradle / build.gradle / gradle.properties / loader metadata as necessary; exact registration patterns; existing bogatyr summoning, Ilya ally and personal battle lock code; an existing boss encounter; animation/rendering and sound conventions; quest/progress and structure/persistence helpers; targeted tests.
- Prefer existing packages, component systems, AI goal API, GeckoLib/renderer IF ALREADY USED, data pack and entity registration patterns. Do not assume named types or loader APIs exist. Confirm the repository's actual Minecraft version; previous project history included multiple eras, so never downgrade to Minecraft 1.16.5 or Forge solely from old memories.
- All new resources use namespace `slavicmyths` only when consistent with current repo. Identifiers are proposals in the integration map; re-map to current conventions without changing behavior.
- Avoid duplicated state in client and server. All damage, target choice, transforms, encounter-locks, save flags, item claims and loot are server authoritative. Clients render synced state.

## CONTENT RESTRICTIONS
- DO NOT distribute or cut film audio; cartoon clips are NOT proven to be free of rights. Ship only supplied synthesized original sound effects with declared source/provenance; spoken dialogue is a script until a properly licensed/recorded voice asset exists.
- DO NOT silently substitute random stock images, use generic armor cloned from Ilya, put a full white beard on Dobrynya, give him a sword instead of the spear, use smooth photorealistic rigs, 16x16 low-effort texture assets, generic purple fire everywhere, duplicate clones that visibly reveal the original, or place a visible marker over the real Marinka.
- DO NOT grant invulnerability by stacking clone vanish + shield + teleport indefinitely. Every invulnerable/flee state has finite bounds. Boss cannot cause unavoidable burst combos.
- DO NOT award new hero artifacts, boss weapons, new drop tables, advancements-as-rewards, or unique post-boss relics until the dedicated v1.7.6 rewards update. Ordinary vanilla loot in the basement is not the delayed unique reward.
- DO NOT spawn a second copy of a story ally during the fight; summon cap 1 specifically matched ally for personal battles, and 2 only for future final Kalin-Tsar content (NOT implemented now).

## IDENTITY AND NAMING
- Proper character Russian name: `Добрыня Никитич`; villain: `Маринка` / `Марина Игнатьевна` in lore dialogue. She is a witch/sorceress, not a generic necromancer.
- Required boss: 600 HP, 2.1-block visual height, fragile but hard to catch. Dobrynya: 230 HP, 2.4-block visual height, broad gray/silver armor, white hair and short stubble, spear length ~3.6 blocks.
- Boss arena center is the reference coordinate origin, not a hard-coded world position; all diagrams use local offsets and MUST be transformed by actual structure origin/rotation.

## STOP/REPORT rather than guessing
If a requested renderer or entity event cannot be achieved with the current project stack: implement the closest reliable equivalent, explain exact code/API constraint in the change report, and supply reproducible proof. Never silently deliver pseudo-code or an image instead of a working mod.
