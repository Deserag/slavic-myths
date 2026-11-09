# Slavic Myths: 2D Sister Artifacts

Five inventory relics use actual transparent 64x64 pixel-art textures with `minecraft:item/generated`, not cuboid item models.
Artwork is procedural (Pillow) and is reproduced from `tools/generate_sister_artifacts_2d.py`. No AI art is shipped.
The set consists of ring, necklace, belt, crown, and brooch, with three insets each.

Each of six sisters has a distinct gemstone design as well as a specific palette and etched sign:
- Tryaseya: violet wind spiral
- Gneteya: carved bone-colored seal
- Ledeya: icy cyan snowflake
- Agneya: orange flame
- Neveya: deep lilac soul crescent
- Lomeya: grey stone sigil

A client-only `slavicmyths:sisters_visual` item property returns a 6-bit mask derived from persisted ItemStack blessings.
Each of the five item models selects from 42 variants: empty, 6 single, 15 double, 20 triple.
Visual appearance does not depend on the order powers were inserted. Gameplay abilities, recipe materials,
Curios two-item restriction and bonus for six unique blessings are not changed by this art-only patch.

Generate with `python tools/generate_sister_artifacts_2d.py --preview` (Pillow); verify with `--check`.
Generated PNG/JSON files are committed by the branch-scoped workflow. Preview files stay in build output.
Automated build is not Minecraft client QA; check actual 16px inventory appearance and item upgrade states in-game.
