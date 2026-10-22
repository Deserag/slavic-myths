# #87 пакет №32 — #121 canonical generator preservation

**Main baseline:** `4247c95ff8f8b82cb875161edf960686caaaa2af`, merged [PR #203](https://github.com/Deserag/slavic-myths/pull/203) into `f1b9756454becb2cb424d6da272a68a9ab34ff6d` on 22.10.2026.

## Source, isolation and actual result
The previous #87 `tools/verify_weapons87.py --generators --jar` relies on archived hashes from earlier packages; its flail checks were blocked on unchanged production, so a historical SHA cannot be used to decide whether the *current generator* has reverted owner-approved art. `tools/weapons87_generator_gate.py` captures the exact current `src/main/resources` snapshot; copies it, the Python source generators, the original weapon-art sources and the individual blueprints to a temporary directory; replays the actual canonical `weapons87_flails.py --apply` pipeline (including gripped poses, overlays, P20 unique art, P28 horizontal crossbow stock) **twice**. It compares the results to the live baseline and then destroys the sandbox, without changing the actual repo.

**CI [39776454779](https://github.com/Deserag/slavic-myths/actions/runs/39776454779) PASS**: 68 registered weapons, 16 protected relevant source paths; two generator executions exit 0, repeat twice with zero idempotency drift. 30 files have different raw file bytes (12 crossbow JSON files and 18 flail PNGs), but comparing JSON structures and RGBA image pixel arrays gives **zero actual model/texture content drift**. Original production files remained unchanged. All 294 JAR source assets passed a separate source→JAR check. JDK21 `clean build` PASS, compiled JAR version1.9.7.56 SHA256 `d920d52874db3d9d1d811756ff10177f0c9b94aec3db7c9edcf73eb34f86790f`.

A detailed `build/weapons87-generator-p32/report.json` is distributed in GitHub Actions artifact, not written to committed production manifests.

| REQUIREMENT | EVIDENCE | RESULT |
|---|---|---|
| exact baseline / no real source mutations | temp isolated sandbox SHA snapshot before/after | PASS |
| 68 current item IDs and model links | creative_catalog, actual source after replay | PASS source |
| 16 most recent protected assets stay geometrically/materially identical | actual JSON equality and PNG RGBA equality | PASS |
| prevent hidden source generator rollback | sequential canonical apply×2 | PASS scoped / 0 idempotency drift |
| detect byte-only archive differences without corrupting history | JSON parse/PNG pixel comparison, 30 byte-different vs0 actual differences | PASS diagnostic |
| Java21 clean build, current production JAR | CI run39776454779,294 assets | PASS |
| all legacy generator entrypoints / full#121 and owner Minecraft 1P/3P | not all standalone scripts replayed; client NOT_RUN | IN_PROGRESS / NOT_RUN |
| old #87 baseline mismatch | 109 archival SHA and51 older flail records unchanged | BLOCKED historical; not magically passed |

Never rewrite older source evidence to match new bytes or close Issue #121 without approval. No texture/material/art, Java gameplay or PolyMC updates in P32.
