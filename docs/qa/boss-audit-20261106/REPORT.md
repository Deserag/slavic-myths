# Boss audit — IN PROGRESS, NOT a completion report

Owner selected release-2.5@5738a403 as baseline and authorized native test worlds and hosted visual runs. Production code/art/balance must remain unchanged. Audit branch: codex/qa-bosses-release25-20261106. No merge is authorized.

## Evidence so far

- Real GameTest registry export:68 candidates with bossbar fields; generic shared classes also contain non-boss/helper types.46 ranked encounters selected for preliminary inventory; Ataman follow-up pending. Construction errors0.
-1554 controlled synchronous damage probes across seven recent bosses: actual health loss in1554, immediate action resets0. Controlled private entry and sampled frames do NOT prove natural attack eligibility, full transitions, critical-hit conditions or live-fight resilience. Raw work/boss-audit/evidence/synchronous-interruptions.json.
-610-tick accelerated GameTest AI trial completed in2.24wall seconds, NOT a30-second test. Several entities left fixture coverage; those rows cannot establish AI failures or PASS.
-Real20TPS dedicated30-second-per-boss series on12recent bosses is running. Forced test chunks/proxy Survival player are explicit fixture controls, not network multiplayer. Partial raw results in work/boss-audit/evidence/dedicated-stress.partial.json.
-Hosted-only visual QA compiles locally. It preserves the GitHub-hosted-only guard; actual framebuffer capture pending publication. Models are not marked PASS merely because capture finishes.

## Matrices

BOSSES.csv contains registry/source and renderer/model candidates. ATTACK_MATRIX.csv is an initial state/damage index with NOT_RUN cells, including states whose eligibility must still be checked; it is NOT certified exhaustive attack coverage. MODEL_SCALES.csv lists source assignments, not proven defects. All full gameplay/visual/hitbox claims remain NOT_RUN until supported.

## Next work

Resolve generic registry variants and inherited controllers; native damage/interrupt probes and30-second stress; real projectile/part hitboxes; per-attack/phase and partner events; restart; hosted views and dynamic model review;1/5/10server and client performance; final defect reproduction and fix order. No production fixes in this branch.
