# Savanna presentation changes — 2026-11-27

Owner requested ordinary creature eggs before boss eggs sorted by danger level, and numeric current/max HP on boss bars.

- Creative mob tab sorted by peaceful, neutral, hostile/elite, other ordinary mobs, then established danger rank ascending. Within each rank stable catalog order is retained. All 22 previously uncatalogued egg model IDs now have entries. Hidden family variants remain hidden. No spawn behavior or creature stats change.
- Source of truth for established levels: BossBarPalette. Pharaohka leader uses existing provisional V in its entity code. Jackal and hyena pack use documented candidate VI; these are display-only, not a balance approval. Bosses without a recorded level are grouped at the end rather than assigned invented ranks: auka, winter_owl, northern_wolverine, ocean_zmey, stratim_bird, zhelva, sand_king, indrik_zmey, gluheya. Kalin war horse is ordinary.
- Div and Polkan use shared CreatureBossBar. Jackal pack uses saved surviving member total / 900; black hyena uses leader HP / 400. Existing viewer and progress rules preserved. Other boss classes and tabs unchanged.
- Added two isolated presentation GameTests. Build and live-client verification pending.

Confirmed additional existing design ranks: Tsar Pike V (`docs/ocean/tsar-pike/DECISIONS_AND_BALANCE_RU.md`), Deep Sea Serpent VI (`docs/ocean/deep-sea-serpent/IMPLEMENTATION_20261119_RU.md`). The remaining nine unranked bosses stay after ranked entries.
