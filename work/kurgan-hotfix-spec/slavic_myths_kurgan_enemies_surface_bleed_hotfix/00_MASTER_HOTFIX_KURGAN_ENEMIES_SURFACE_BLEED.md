# SLAVIC MYTHS — KURGAN ENCOUNTER + SURFACE + BLEED HOTFIX
## STRICT IMPLEMENTATION ADDENDUM
### Minecraft 1.21.1 / NeoForge / Java 21

This package is a mandatory correction on top of the current branch.

It addresses THREE concrete issues found during manual testing:

1. The rebuilt Kurgans look good, but the enemies designed specifically for Kurgans are absent from actual generated dungeons.
2. The exterior mound is visually too uniform because too much of the visible surface is plain dirt.
3. The `Cut / Порез` intrinsic ability wording was ambiguous. Its exact timer/stack behavior is locked here.

Codex is the implementer, not the designer.

Do NOT:
- replace Kurgan enemies with generic vanilla mobs;
- rely on ordinary biome natural spawning inside Kurgans;
- make enemies optional enough that a normal generated Kurgan can be empty;
- turn the mound into random block noise;
- change Cut into unrelated independent DoTs;
- change the five-second duration or five-stack cap.

Read all files in this package before editing.
