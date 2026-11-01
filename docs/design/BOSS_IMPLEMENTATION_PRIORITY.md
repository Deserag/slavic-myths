# Boss implementation order — owner rule, 2026-11-01

Applies to new and revised bosses in Slavic Myths, beginning with Issue #389.

1. **Gameplay identity:** each boss has distinguishable, documented attack roles and readable tells. A normal melee weapon must not quietly inherit chain/hook abilities.
2. **Working mechanics:** prove throw -> flight -> physical hit -> latch -> multi-tick pull -> return, including blocked/missed shots, safe terrain, multiplayer, reload and cooldowns.
3. **Clean readable model:** validate bone hierarchy, anatomical transitions, silhouette, weapon grip/scale, head UVs and overlapping faces at idle, walking and attack poses. Fix z-fighting before adding ornamentation.
4. **Polish and decoration:** cloth accents, runes, extra horn/fur pieces, particle density and material flourishes come only after steps 1–3 pass.

Do not turn a targeted correction into a new visual design. Preserve approved boss identities, IDs, recipes, gameplay parameters and old-world compatibility.

**Acceptance rule:** a successful build or static asset audit is not proof of in-game mechanics or visual quality. Keep unresolved runtime screenshots, client/server tests and owner approval marked **NOT_RUN / BLOCKED**, and do not close the issue or merge on this evidence alone.
