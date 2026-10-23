# Trophy jaw clearance вЂ” 22.10.2026

Source baseline: 1192e229 + local uncommitted patch. No commit/push/main merge.

The isolated mob jaws rotated below the placed block support. The trophy-only pose now retracts each affected jaw upward while opening, keeping its previous angular amplitude, neutral placement, texture, UV and original mob model. The full pose reset restores both rotation and translation. Runtime work is constant per visible head; no new world polling, allocations or geometry traversal per frame.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Actual animation stays inside reserved block cells | heads-rework-native2.log; six native subtrees, 600 consecutive times each | PASS |
| UV, geometry count, neutral server bounds and reset | same native fixture | PASS |
| Before/after source and actual same-scale timed UV renders | before.json, after.json, *.java.txt, tugarin.png, volkolak.png, podzemnik.png, source-manifest.json | PASS |
| Existing P42вЂ“P52 source regressions | existing verify scripts | PASS |
| Real production Finist steering regression | tools/finist-tests/FinistChecks.java, finist-latest-native.log | PASS |
| Java21 clean build / production JAR resources | head-clearance-clean-build.log: BUILD SUCCESSFUL 1m30s; 68 IDs /302 assets, P49–P52 --jar | PASS |
| Owner visual/game/MP acceptance | no Minecraft/server/GameTest launch | NOT_RUN |
| Separate Skim pupil motion, complete player arm rig and #191 | outside this jaw-clearance patch, not claimed implemented | NOT_RUN |

Earlier geometry failures and stale fixture timing assertions remain in the old logs. Fixture timing assertions now reflect the current P50 64-tick blink / stronger whiskers, mane and brows; geometric assertions were retained and expanded from 100 sampled poses to 600 per head. CI now includes the actual native trophy and production Finist tests.

All Issues remain OPEN / IN_PROGRESS. These source renders do not establish in-game acceptance.

JAR: `build\libs\slavicmyths-1.9.7.56.jar`, SHA256 `4bc71e94da5e8529584d9aff3ffd1623f5d236aaebb91cdc4819dae076cb3269`. Test fixture classes absent from production JAR. POLYMC_NOT_INSTALLED: remaining executor scope (Skim pupils/player rig) is incomplete.


Historical intermediate report; superseded for #140 by docs/implementation/trophy140-rework/REPORT.md (full six-head changes and installed test candidate 1.9.7.57).
