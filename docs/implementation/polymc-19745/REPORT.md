# PolyMC 1.9.7.45 · 20.10.2026

Installed by explicit owner request as test preview; IN_PROGRESS / OWNER_PENDING, not final release.
Exact installed path/version/SHA256, backup path/SHA, base commit and dirty source fingerprint: installed.json. Full source hashes: source-before.json. Source base69a2db12 plus uncommitted changes, no new commit. Existing armor work included from the stable current checkout; no armor edits in this installation task.

First build failed storing compilation result. Retry without build cache passed but source check detected concurrent PeruniteArmor/PolozArmor edits. Fresh source baseline and second no-cache clean build PASS in1m1s, immutable source checks PASS. Template neoforge.mods.toml checked against processed resources and parsed actual version, not raw placeholder bytes.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Java21 clean build | build.log | PASS |
| Stable source, ZIP CRC,7442 resources,1339 PNG,1742 Java21 classes, no smoke classes | jar-check.json | PASS |
| Rune shader standalone compile/link | shader.log | PASS |
| Installed JAR matches production SHA | installed.json | PASS |
| Backup44, one active Slavic JAR,6 other JAR SHA unchanged | installed.json | PASS |
| Minecraft/PolyMC/GameTest/server runtime | launches0 | NOT_RUN |
| Owner visual acceptance #104 / armor | pending | NOT_RUN |

#104 stays OPEN; #105 NOT_STARTED. No commit/push/issue closure.
