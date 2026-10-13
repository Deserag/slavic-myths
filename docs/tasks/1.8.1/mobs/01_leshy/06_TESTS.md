# Моровой Леший — ТЕСТЫ
- Unit/state: normal->transforming->active, repeat splash idempotent, max health modifier occurs once, 50% HP remains 50%, after save no second apply. Old save no mor fields remains normal.
- Behavior headless: root telegraph 24t, target moves at t23 => miss, stays => 4 HP once + 30t root hold; three roots never triple hit; block wall/air/lava don't place roots.
- Wail: target inside radius5 takes 3HP and impulse; radius>5 no hit; no NPC/friendly fire; no through-solid-wall; CD360t; only one ability at once.
- Model gates: `mor_leshy.png` correct atlas size/UV, antlers/long claws/veins/glowing red rectangular eyes present, native bake/renderer resource references valid; ordinary resource bytewise or functionally unchanged.
- Perf: ≥10 nearby inactive normal leshies do not execute mor scans/sounds/abilities; no global scan tick.
- Manual user QA: creative flask in forest; compare normal/mor side by side at day/night, check walk/attacks/particles/audio, reload world; user tests only, Codex must NOT run the client.
