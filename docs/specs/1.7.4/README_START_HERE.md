# Slavic Myths 1.7.4 — Finist / Kartaus — Codex implementation bundle

**START:** `00_START_HERE/00_CODEX_EXECUTE_EN.txt`

This is an actual implementation specification, NOT another request to draft a plan.

Includes: 4 deep spec files for Finist, 4 for Kartaus; falcon controller/model; summon items; minimal no-worldgen story encounter; RU+EN localizations; 46+ acceptance cases; 2 USER-APPROVED visual references; seven custom technical sheets plus 3 actual 64x64 RGBA sprites; **54 actual valid OGG** original non-vocal sound effects, manifest of origins/rights and sample sounds.json resource fragment.

**Crucial decision:** We DO NOT implement a large fortress or overworld generation; the Kartaus fight starts with a reusable seal on safe ground. Normal summon supports 2 heroes, Kartaus story fight exclusively Finist. No unique rewards before version 1.7.6. Last stand at 15% HP, never free replacement mid-story battle.

## Audio integration warning
Original sound EFFECTS are present but film voice recordings are NOT present. Existing Russian lines are scripts for optional independent voice recording. The 54 effects are synthetic, not human voice or film sound.

## Resource names
An indicative `sounds_174_fragment.json` maps each OGG to a vanilla sounds.json path after moving `07_Audio/<Character>/<event>.ogg` to `assets/<modid>/sounds/<character-lowercase>/<event>.ogg`; replace the example `slavicmyths` namespace with the actual current project namespace only if different. Merge fragment with existing `sounds.json` rather than replace existing sounds. Subtitles need corresponding language keys if exposed; descriptive keys are in character audio docs, extend for every event used.

The image sheets contain conceptual text panels but *precise mechanic values in Markdown override decorative labels.* Character proportions: Finist 2.25 blocks, Kartaus 2.7 blocks; vanilla player 2.0 blocks.

## Package quick map
- `00_START_HERE/`: hard scope, instructions, hierarchy and project reading limit
- `01_Finist/`: exact appearance, UV, bone dimensions, animations and abilities/audio
- `02_Kartaus/`: same for boss with all skills, phases, 650 HP and minions
- `03_Falcon/`: companion, transformation, cursed anchor, lifecycle
- `04_Summoning/`: invocations/items (nonconsumable hero feather, reusable challenge seal, creative egg)
- `05_Encounter/`: compact field event, no generated location; per-player state, boss mechanics
- `06_References/`: two approved artworks and technical sheet/actual game art
- `07_Audio/`: 54 game-ready OGG, origin manifest, sound mapping
- `08_Localization/`: RU/EN names and UI
- `09_Tests/`: acceptance and fast workflow

No background work required from ChatGPT. Send ZIP directly to Codex and point it at `00_CODEX_EXECUTE_EN.txt`. Zip contains complete local reference images and audio.
