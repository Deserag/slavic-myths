# Slavic Myths 2.1.x — bosses: tasks and art sources only

**Status:** TWO OPEN IMPLEMENTATION ISSUES. **NO GAMEPLAY CODE CHANGES** relative to `hell` in the current branch tree.

## Tasks
- [#353 — Морова дева «Невеста мора»](https://github.com/Deserag/slavic-myths/issues/353)
- [#354 — Князь Последней Тризны «Пламенный князь»](https://github.com/Deserag/slavic-myths/issues/354)

## Included reference files
- `SHARED_CONTRACT_2.1X.md`: agreed rewards, universal legendary soul, runes, summon items.
- `MOROVA_DEVA_2.1X.md`: individual implementation requirements.
- `TRIZNA_PRINCE_2.1X.md`: individual implementation requirements.
- `models/morova_bride.bbmodel` + `.png`: editable draft geometry and texture sheet, not integrated into the game.
- `models/trizna_flaming_prince.bbmodel` + `.png`: editable draft geometry and texture sheet, not integrated into the game.

The user's approved visual reference remains the 2026-10-29 boss poster. Draft geometry is **not a validated replacement**. Final in-game appearance, animation/sound synchronization, phase attacks, mob AI, loot, recipes and rituals belong to the respective issues and require user acceptance.

## Scope correction (2026-10-29)
The assistant misread the request as authorization to start implementing gameplay. Early untested Java entity/renderer code and live entity textures were then created **only in this isolated branch**. Following clarification, all those runtime changes were removed from the working branch tree and the two pre-existing Java files restored to their exact `hell` blobs. The Git commit history still contains the discarded exploratory work; this document describes the current working tree only. No merges or pull requests were made, and `hell` was not changed.

Do not implement or test code, add active registration, or change `hell` without a new explicit user request.
