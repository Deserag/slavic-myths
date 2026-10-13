# Existing-UI additive layout / target pixel spec

Do not build a new full screen. Extend EXISTING GUI only where possible and do not alter unrelated widgets. For consistent scaling, specify coordinates relative to existing GUI's top-left origin and adjust to actual GUI layout margins.

**Rewards claim card** (if existing Book of Tales has an extensible rewards panel): size 176x86 GUI pixels; x0..175, y0..85. Inset left 6, top 6. Item icon in x8..39, y8..39 (32x32). Name text x46,y10 max 125px; if overflowing, two 9px lines. Rarity text x46,y28. Description x8,y45 max 160px split across up to 2 lines with measured font width. Claim affordance x90..168 y64..80, only when real server claim is possible; localized label 76px max. A claimed card shows small monochrome checkmark in x153..168 y64..80. Do not cut off input focus, background and scroll.

**Rune Anvil preview extension**: reserve compact details block 160x70 GUI pixels alongside existing rune selector if layout allows. Item icon at (8,8) 24px; name (38,8); category (38,23); effects from (8,36), 2–3 wrapped lines (<=145px). Invalid state color muted red; valid state neutral pale gold; never cover sockets.

Icon authored 64x64 RGBA but shown at 16x16 in item inventory and 32x32 in detailed preview with nearest-neighbor sampling. Textures must not blur.

Screen reading: show item names/text via tooltip so color is not the sole cue. Any adapted existing UI may deviate in overall coordinates if current dimensions require, but retain these inner component dimensions and readable no-overlap behavior.
