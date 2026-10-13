# UI/HUD + interaction hints, coordinates, accessibility

## Layout goals
Use standard Minecraft's built-in boss bar unless the mod already has its own stable boss HUD. Do not invent a new fullscreen UI. If custom scaling uses screen `W,H`, top bossbar center `(W/2, 12)` width `min(182, 0.48*W)` height 10 logical px, background dark `#26171D`, fill wine-red `#A32C40`, border light-gray; text `Маринка` centered above. The bossbar tracks 600HP original but never attaches to which body is real during clone phase. Ally mini-status at `(W-12, H-45)` anchor width 110px, height 9px, only if existing ally UI allows non-overlap with hotbar/other mods; relocate if collision detected. Render test 1280x720, 1920x1080, 2560x1440 and GUI scales 2/3/4.

## Contextual gameplay texts
- Quest start marker on interaction: `Начать испытание Маринки (нужен Добрыня Никитич)` [Start Marinka's Trial (Dobrynya Nikitich required)]. Interaction distance 4.5b, no automatic teleport without consent.
- Invalid other hero summon: `В этом сказании с вами может сражаться только Добрыня Никитич.`
- Three copies appear: `Маринка скрылась во тьме. Разбейте обманные образы ударами.` Subtitle must not reveal true one.
- Anchor appears: `Добрыня обращён в золоторогого тура! Разрушьте колдовскую печать.`
- Barrier broken: `Покров Маринки разрушен!` for max 2s.
- Stage 3: `Чары Маринки выходят из-под контроля!`
- Victory/hatch: `Колдовство рассеялось. Под печатью открылся тайный ход.`
- Dobrynya retreat: `Добрыня отступил, чтобы восстановить силы.`

## On-world signifiers
- Rune anchor: floating glyph geometry at 2–3b above ground with 80HP or 3 strike progress ring segments. For 3-strike form show three slotted runes gray out on each successful hit; no need new GUI.
- Ranged area attack: floor projected rectangles/wedges visibly aligned with collision AOE. Do NOT display only tiny smoke sparks while actual collision is a much larger invisible sphere.
- Major curse warnings communicate by **shape + sound + color**, not color alone. Red fire has point/arrow wedge; shadow spawn has three smoke funnels; golden track has a linked footprint chain; magic protection has circle-shell ring.
- Use existing mod status descriptions for hero abilities; don't create an unreadable new giant RPG skill tree interface just for v1.7.2.

## Accessibility
Contrast ratio adequate over day/night; reduce-flash option by respecting game particle setting and no strobe >2Hz; provide subtitle cue for all heavy casts, transform, shadow, ward break, hatch. Avoid unskippable camera effects; configurable low screen shake. Provide accessible fixed visual markers even when sound muted or reduced particles.
