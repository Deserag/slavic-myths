# #241 — production reward policy, 24.10.2026

Natural assigned burial upyr fragment chance35→55%; RuneSummoned and missing kurgan UUID still excluded. Voevoda/volkhv soul50→70%; prince guaranteed1–2. Existing nav/drushinnik/relic/boss unique drops and roster counts unchanged.

All nine original burial/offering/important tables retain their original pools. At most three new stacks fit alongside the original two in five-slot coffins. Important coffins guarantee one practical stack: weighted iron6 / gold2 / emerald1. Iron count1–2 /2–4 /3–6, gold/emerald1 /1–2 /1–3 for small/warrior/great. Ordinary material chances35/50/65%.

Pool | Small | Warrior | Great
--- | --- | --- | ---
Fragment chance, ordinary |20%,1 |30%,1–2 |40%,1–3
Fragment chance, important |40%,1 |55%,1–2 |70%,1–3
Rare chance, ordinary |4% |8% |12%
Rare chance, important |10% |18% |28%

Rare choice weights: blank_rune_1=6, rune_resilience=1, rune_strength=1, diamond=2; great important also soul=1. The two ready runes are actual RuneBalance tierI with existing blank-I recipes. Different pools are independent, each rare pool produces at most one stack. No guaranteed diamonds.

Native V2 code audit found missing loot resources for six already-used suffixes: common_cache, warrior, treasury, ritual, secret, great_special. Added all18 aliases (three tiers) to existing offering/burial/important tables. Old unopened cofﬁns using those IDs can now resolve; opened inventories never reroll. Final room newly placed coffins explicitly use important (same geometry, footprint, seed, room IDs, population and coffins). Previously missing routes are treated as empty in the before simulation; native source exports retain both original and final-room-corrected routes.

Whole-dungeon comparison uses64 real KurganPlan/KurganRoster samples per tier and20,000 seeded trials per tier/version. Includes natural mobs and all modeled coffin routes, four fragments per soul equivalent. `balance-simulation.json` records mean, probability and observed minimum. Mathematical minimum for tracked materials is0 except guaranteed prince soul≥1 in great kurgans; guaranteed useful iron/gold/emerald is a separate important-coffin guarantee, not a guaranteed soul/diamond/rune. Layout sample extrema are not all-seed guarantees. No extra enemies or chest rerolls are introduced.
