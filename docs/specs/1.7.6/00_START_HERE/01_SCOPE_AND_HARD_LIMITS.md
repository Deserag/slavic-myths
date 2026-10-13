# Hard rules, bounded implementation and forbidden work

1. This ZIP is an executable implementation TЗ; no preliminary plan, design workshop, generic TODOs, or request to re-review entire project.
2. Open the existing mod's items registry, resources, recipe registration, configured rune anvil integration, accessory capability/slot handling, boss kill events/victory history storage, player persistence, JEI integrations, localization and minimum necessary UI. Never indiscriminately read every file or git history.
3. Preserve the current release platform and existing ID conventions. Prefix example registry names in this package with the mod's real namespace. Check collisions only around listed item IDs.
4. Register exactly the 19 new items; no extra ore blocks, boss summoning items, test items, alternative currency, or new utility workbench.
5. Add crafting recipes to current recipe infrastructure; extend rune anvil preview and compatible slots using its own rules; preserve existing eight runes, their slots and stats.
6. Use existing accessory inventory. If two designs share the same slot, let them compete (do NOT create duplicate accessory slots or automatic stacking).
7. No new dimension/world generation, boss model, quest plot, arenas or boss attack rewrites. Surgical hook for defeating five existing bosses only.
8. User has approved 1.7.6 art in PNG reference sheets (see Approved folder): never substitute unrelated images. The files under Item_Textures_64 are actual starter alpha PNG icons at required minimum resolution; use or closely refine rather than recolor one generic template.
9. Mandatory: server-authoritative combat/item procs; client sends only actions, not damage multipliers, item ownership, victories or reward status.
10. Prevent exploit: exact-one-time grants, cooldown persistence across dimension changes and equipment swap, no reflection feedback loop, no infinite sweep farming, no replica loot on debug spawn eggs.
11. Every proc has one specific activation animation, VFX and SFX hook. Register provided original prototype OGG safely. No unlicensed soundtrack/audio copied from cartoons.
12. Keep direct numeric constants in configuration/constants near the respective mechanic; localized tooltip must reflect actual values. Do not hide changes in magic numbers.
13. Base difficulty uses player approx 24 HP (12 hearts) and full diamond armor; reward effects cannot accidentally negate boss balancing with indefinite invulnerability.
14. Version 1.7.6 is the reward patch: NO rewriting the challenge system or changing existing boss drops more than required to hook new reward logic.
15. Finish by compiling the project with its actual wrapper and perform targeted in-game checks. Do not consume time with unrelated tests; do not claim tests passed if not run.
