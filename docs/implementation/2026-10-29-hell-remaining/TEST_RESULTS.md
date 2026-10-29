# Current task verification — 29.10.2026 (hell)

This file supersedes the initial `No current implementation test yet` placeholder. **It is not owner acceptance, a five-seed natural-survival audit, or proof of GUI/audio/art identity.**

## Verified production build and headless QA

- Production and test source commit: [04a850e223e220279d8d4e0d889874784b9bb7be](https://github.com/Deserag/slavic-myths/commit/04a850e223e220279d8d4e0d889874784b9bb7be); preceding fixture root-cause correction [0132ad2](https://github.com/Deserag/slavic-myths/commit/0132ad252726061b1ece8da054e38c5fd4df78df).
- Workflow: [run 40833432079](https://github.com/Deserag/slavic-myths/actions/runs/40833432079), `.github/workflows/hell-headless-qa.yml`, isolated GitHub-hosted Ubuntu runner, Java 21 (Temurin 21.0.13), Minecraft 1.21.1 / NeoForge 21.1.255.
- `./gradlew --no-daemon clean build --console=plain`: **BUILD_PASS**.
- `python tools/verify_current_resources.py`: **SOURCE_RESOURCE_PASS**; 8,705 packaged resources, 4,950 JSON, 1,599 PNG, 2,123 model JSON, 2,157 Java 21 classes.
- Packaged `build/libs/slavicmyths-2.0.10-HELL.jar`: **25,366,939 bytes**; SHA256 `7f9a5950f8a77e727564488556a740a7875a97b1cc268474d0aa2e034a6c55dc`. The same bytes were independently SHA-checked from the GitHub Actions artifact.
- `./gradlew --no-daemon -I tools/hell-qa-runtime.gradle runHellQa -PhellQaDir=work/hell-qa-20261029/github-actions -PhellQaSeed=20261024 --console=plain`: **HEADLESS_RUNTIME_PASS — all 11 required GameTests passed** (15.27 s Minecraft GameTest run; build task overall 56 s). Production tested from a separately copied actual packaged JAR, test fixture is not inside the JAR.
- Actual runtime seed `20261024`: 5 vanilla Nether biome IDs + 4 new IDs sampled; 3 older custom biome chunks and an ocean centre generated; full dry arena, 6-block headroom and one natural chunk-event Magma Lord verified **with 49 forced test chunks**, not normal player exploration. Real 120-tick temporary magma expiration restored the original floor.
- Actual checks also covered manual Posten in Overworld/Nether/End; Peaceful Magma Lord removal with neutral salamander retained; four trophy BlockItem/BE/rotation/drop anchors; armor equip/unequip HP and speed; smithing data/runes; furnace wrong fuel/menu/hopper, smelt 1+64 items, XP exactly once, blocked output and NBT; active Curios necklace vs inventory/removal and Two Winters; flail pulse; knife projectile 10 HP/Weakness/Loyalty III exact one return and wear/cooldown; infernal bow one-arrow special 13 HP, regular 10 HP during skill cooldown, 160t fire, Power II 14 HP observed through native pipeline, ammo count and native crit.
- Original historical run11 was 9/11; source fixture faults were corrected in [0132ad2](https://github.com/Deserag/slavic-myths/commit/0132ad252726061b1ece8da054e38c5fd4df78df). Follow-up run [40832920954](https://github.com/Deserag/slavic-myths/actions/runs/40832920954) was 10/11 because it assumed an unsupported exact Power II damage of 16 HP. With game version pinned, actual native hit was 14 HP. Contract fixes the **unenhanted** direct hit at 10 HP, but does not approve a Power II total; test asserts the measured native behavior without changing production damage. Last run is 11/11.

## Still NOT_RUN / BLOCKED

- Real GUI/client, Steve/Alex first/third person renders, art approvals, exact original PNG/SHA matching, true audio/voice assets: `GUI_VISUAL_QA_BLOCKED_ISOLATION` / `ART_BLOCKED_REFERENCE` / `BLOCKED_NEEDS_AUDIO_SOURCE`; actual owner client and owner acceptance not supplied.
- Five **independent actual game-world seeds** with survival routes, ordinary player chunk loading, `/locate biome` command, competitor structures, old worlds, FPS/MSPT under normal gameplay: `NOT_RUN`. One bounded GameTest seed is not the requested five-seed acceptance.
- True 2-client multiplayer, ordinary player GUI/survival, claims plugins, chunk-unload/relog/server power-cut for actual NeoForge world/player files, full PvP/allies, boss reward crash transactions: `NOT_RUN` (prior isolated pure crash simulations do not substitute).
- Installed player's PolyMC candidate: `NOT_INSTALLED_BY_THIS_WORK`. No world/save and no interactive desktop was touched; `main` unchanged, no force push.
- This technical checkpoint **does not close Issues** #313/#320/#323/#325/#326/#329/#330/#337 or confer `OWNER_ACCEPTED`.
