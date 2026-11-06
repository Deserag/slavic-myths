# Boss audit — IN PROGRESS, NOT a completion report

Owner selected release-2.5@5738a403 as baseline and authorized native test worlds and hosted visual runs. Production code/art/balance must remain unchanged. Audit branch: codex/qa-bosses-release25-20261106. No merge is authorized.

## Evidence so far

- Real GameTest registry export:68 candidates with bossbar fields; generic shared classes also contain non-boss/helper types.46 ranked encounters selected for preliminary inventory; Ataman follow-up pending. Construction errors0.
-1554 controlled synchronous damage probes across seven recent bosses: actual health loss in1554, immediate action resets0. Controlled private entry and sampled frames do NOT prove natural attack eligibility, full transitions, critical-hit conditions or live-fight resilience. Raw work/boss-audit/evidence/synchronous-interruptions.json.
-610-tick accelerated GameTest AI trial completed in2.24wall seconds, NOT a30-second test. Several entities left fixture coverage; those rows cannot establish AI failures or PASS.
- Real20TPS dedicated series completed:12 bosses,610ticks and30.4–30.5wall seconds each. This uses repeated native bare-hand attacks, not the requested sword/critical/projectile matrix. Tryaseya only ticked396times after leaving the loaded fixture; that case cannot establish30seconds of active boss AI. Nine-Mor Serpent received only one accepted close-range attack. Forced chunks and proxy Survival players are fixture controls, not network multiplayer. Raw dedicated-stress.json remains separate from accelerated GameTest results.
- Authorized QA branch published at e5b57d2e2c252a344dc0fc52d72fe4e153346cbc. Hosted run [42166613024](https://github.com/Deserag/slavic-myths/actions/runs/42166613024) completed8/8 client jobs successfully, producing712 framebuffer captures for47 IDs. This is capture completion, NOT model/animation acceptance. Local downloaded artifacts are under work/boss-audit/artifact-*.
-56 captures are invalid because actor.removed_at_capture=true: magma_lord13,karachun15,marinka13,morozko15. Karachun/Morozko require sessions; Marinka requires a player-list owner; Magma Lords register persistent encounter anchors, so replacing actors at the same anchor is rejected. These are QA fixture failures, not proven production defects. Follow-up fixture setup and actual renderer/model metadata collection are being prepared in QA-only code.
- Dedicated server performance collection is running:47IDs×counts0/1/5/10,40warmup+100measuredticks per case. Report alive/ticking counts alongside MSPT; absent actors invalidate the requested load. No client FPS benchmark, prior-release comparison or repeated statistical performance claim has been completed.

## Matrices

BOSSES.csv contains registry/source and renderer/model candidates. ATTACK_MATRIX.csv is an initial state/damage index with NOT_RUN cells, including states whose eligibility must still be checked; it is NOT certified exhaustive attack coverage. MODEL_SCALES.csv lists source assignments, not proven defects. All full gameplay/visual/hitbox claims remain NOT_RUN until supported.

## Next work

Resolve generic registry variants and inherited controllers; native damage/interrupt probes and30-second stress; real projectile/part hitboxes; per-attack/phase and partner events; restart; hosted views and dynamic model review;1/5/10server and client performance; final defect reproduction and fix order. No production fixes in this branch.
