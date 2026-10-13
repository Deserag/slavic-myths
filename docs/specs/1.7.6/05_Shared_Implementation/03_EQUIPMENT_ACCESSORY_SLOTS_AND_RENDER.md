# Wearables, existing slots and model rendering

## Gifts vs trophy equipment
- Ilya Shield: standard offhand shield, unique heraldic face with correct blocking pose and first-person shield raising. DO NOT simply recolor existing shield; provide distinctive front geometry using 64px UV.
- Dobrynya Ring: already-available ring slot; no second ring slot.
- Alyosha Belt: existing belt slot; cloth tassels visible on avatar only if current accessory renderer already supports wearables; otherwise produce matching inventory art and minimal model path using current system, no new renderer.
- Finist Amulet: existing amulet slot; bird emblem visible in icon and optional wearable pendant.
- Necklace of Fangs: SAME existing amulet slot as Finist charm. Mutually exclusive: do not stack both on one character.
- Idolishe chestplate, Marinka cloak, Kartaus mantle: each consumes vanilla chest equipment slot. Existing netherite chestplate cannot also be worn at the same time. Do not silently give protection without occupying slot.
- Kalin Saber: main hand; vanilla-style slash animation with special sweeping proc.

## Armor geometry + UV targets
- Use Minecraft player model alignment; head/body/limbs rotations inherited from base renderer.
- Textures: 64x64 minimum per item sprite; armor set sheet recommended 64x64 or 128x128 matching current implementation, no 16x16 placeholders.
- Idolishe bone shoulder plates protrude no more than 3 px at original 64 atlas projection; protrusions should not occlude first-person camera.
- Marinka cloak: split tail panels, dark burgundy lining, dynamic cape cloth at walking/jumping; if advanced physics unavailable use 3-keyframe torso+tail animation, no broken hanging rigid plane.
- Kartaus mantle: stiff armor collar and heavy cloth back piece with central vertical red insignia; visually distinct from Marinka cloak: squared military shoulders, no red crystal smoke.
- All item icons must preserve 1–2 pixels transparency around bounds and alpha channel fully transparent outside silhouette.

## Rendering checks
Test idle, walking, sprinting, crouching, swimming, riding, item use, damage pose and inventory preview. No duplicate displaced hats or floating wearable geometry (previous bug class). Avoid Z-fighting with other accessory renders. Item model should not vanish when equipped or held offhand.
