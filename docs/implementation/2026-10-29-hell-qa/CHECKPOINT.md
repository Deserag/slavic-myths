# QA checkpoint — ongoing

Production implementation stagesD/G1/G2/G3/debts are committed. Owner's new explicit isolated-QA authorization merged from origin/hell (aff7ff64 and6557aae1). This supersedes earlier historical no-runtime notes for this narrow QA only.

- GUI BLOCKED_ISOLATION: no proven independent display/input/OpenGL; owner desktop/PolyMC/saves untouched.
- Production headless GameTest run1:3/3 PASS, seed0, exact packaged JAR SHA f00cefdba325129f342f6cb0695363c590f325b165413157647c048833fd9764.
- run2:5/6 PASS. Failed test assumed blackstone-only arena; basalt/obsidian are explicitly intended. Fixture corrected.
- run3: actual full arena/provenance120t TTL checks reached; natural boss absent after49 loaded test tickets. Async fixture AssertionError crashed test server; changed to native GameTestAssertException. This is not a gameplay crash.
- Production bounded queued retry added because Chunk.Load precedes entities-ready. Clean build/resources PASS, JAR3b7212c5e74c5cecb26842a3381ba768bec5f7c92ab625e1ac28b97c469f5116.
- run4:5/6 PASS, natural boss still absent. Retry alone not sufficient; do NOT call it fixed. run5 adds loaded/entity/biome/bridge/pending/journal diagnostics. Continue until root cause understood and fixed.
- Native sampler and actual chunk generation have found all9 Nether biome IDs at seed1; three old mod-biome chunks and ocean centre generated. This is diagnostic generation, not survival or5-seed frequency acceptance. Logs show requested heavy generation causes test-tick catch-up warnings; no performance PASS.

Remaining: resolve natural boss spawn failure; headless5 seeds/natural terrain and extended effects/XP/Curios/save tests; final current offline/regression/resource checks, two requirement review passes, owner checklist, exact artifact manifest; publish. Missing original PNG/genuine voices stay blocked. Owner acceptance absent; no Issues closed.

Follow-up: run6 diagnostic said BAD_FLOOR because the test itself placed temporary magma before natural-spawn validation. Test was split: natural boss on clean arena first, then magma expiry. run7-baseline used the exact f00cef… production JAR copied at run1, and still failed on a clean arena (no boss after49 forced chunks). run9 current retry produced exactly one natural boss on seed1 and restored120t magma. Additional real tests proved64 input→64 iron, XP45 once, active Curios necklace vs inventory/removal and original Two Winters; knife check initially expected negative armor below native zero clamp, corrected target armor to6 and expected4. This is a fixture correction, not a production balance change.

run10 seed0:9/9 required GameTests PASS on current packaged JAR. All9 Nether biome IDs sampled; real3 old-biome chunks + ocean full dry arena/6headroom, one natural chunk-event boss and120t magma restore. Heavy diagnostic chunk requests used49 test tickets (removed), not ordinary player loading or survival. GUI/true clients remain blocked/not run. This supersedes the earlier unresolved-spawn checkpoint, but implementation/QA as a whole remains IN_PROGRESS.

## 29.10.2026 — additional source-only QA repair and automated verification path

Run11 expanded to11 tests, **9/11**: failures in `nativebowskillordinaryandpower` and `actualknifeprojectileoneitemandloyalty`. Both contain proven *fixture mistakes* (bow overwrote arrows in selected slot; knife fixture compared against AIR after shrinking source stack). Corrected at [0132ad2](https://github.com/Deserag/slavic-myths/commit/0132ad252726061b1ece8da054e38c5fd4df78df) without modifying production gameplay. Trace and remaining assertion gates: [PROJECTILE_FIXTURE_TRIAGE_20261029.md](PROJECTILE_FIXTURE_TRIAGE_20261029.md).

Scoped Linux Java21 CI [c7034ea](https://github.com/Deserag/slavic-myths/commit/c7034ea58220c28f13b5fe1467d7fb92ccfafe70) publishes clean-build/packaged GameTest logs, JAR and SHA256: [first workflow run](https://github.com/Deserag/slavic-myths/actions/runs/40832920954). **No new build or runtime PASS may be claimed until that run's actual result is inspected.** Prior run11 FAIL is preserved, not retroactively changed. No owner GUI/PolyMC/saves were touched; remaining original-art, full survival, crash/relog and multi-client QA retain their prior restrictions.

## GitHub Actions proof added after the preceding handoff

The scoped QA workflow ran twice. [40832920954](https://github.com/Deserag/slavic-myths/actions/runs/40832920954) compiled/packed successfully and passed **10/11** actual headless tests: the knife was validated, and the bow now launched; the remaining assertion guessed 16 HP for Power II where NeoForge 1.21.1 measured 14 HP. The contract fixes unenchanted 10 HP but no exact enchanted total, so **no production balance was changed**. Source test fix [04a850e](https://github.com/Deserag/slavic-myths/commit/04a850e223e220279d8d4e0d889874784b9bb7be) anchors this measured native response.

**FINAL SCOPE OF THIS QA REPAIR:** [40833432079](https://github.com/Deserag/slavic-myths/actions/runs/40833432079) produced `BUILD_PASS`, `SOURCE_RESOURCE_PASS` and **HEADLESS_RUNTIME_PASS 11/11** on actual seed `20261024` against a copied production `2.0.10-HELL` JAR SHA256 `7f9a5950f8a77e727564488556a740a7875a97b1cc268474d0aa2e034a6c55dc`. Exact evidence and remaining gates: [TEST_RESULTS.md](../2026-10-29-hell-remaining/TEST_RESULTS.md). Original-art, GUI visual, normal survival five seeds, true two-client MP, old world and full owner acceptance remain `BLOCKED`/`NOT_RUN`. Earlier failures retained for traceability.
